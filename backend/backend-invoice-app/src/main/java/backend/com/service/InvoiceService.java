package backend.com.service;

import backend.com.dto.*;
import backend.com.entity.*;
import backend.com.repository.AppUserRepository;
import backend.com.repository.CompanyRepository;
import backend.com.repository.CustomerRepository;
import backend.com.repository.InvoiceRepository;
import backend.com.security.CompanyPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final AppUserRepository appUserRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                           CustomerRepository customerRepository,
                           CompanyRepository companyRepository,
                           AppUserRepository appUserRepository,
                           PdfGeneratorService pdfGeneratorService,
                           EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.appUserRepository = appUserRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.emailService = emailService;
    }

    /**
     * Step 1: a staff (or admin) user submits the form. Totals are
     * calculated server-side (never trust the frontend's math) and the
     * invoice is saved as PENDING_APPROVAL. No invoice number and no PDF
     * yet - those are only assigned once an admin approves it, so a
     * rejected invoice never burns a number out of the yearly sequence.
     */
    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO request, CompanyPrincipal principal) {
        Company company = companyRepository.findById(principal.companyId())
                .orElseThrow(() -> new IllegalStateException("Unknown company"));
        AppUser submittedBy = appUserRepository.findByCompany_IdAndUsername(company.getId(), principal.username())
                .orElse(null);

        Customer customer = new Customer();
        customer.setName(request.getCustomer().getName());
        customer.setEmail(request.getCustomer().getEmail());
        customer.setPhone(request.getCustomer().getPhone());
        customer.setAddress(request.getCustomer().getAddress());
        customer.setGstin(request.getCustomer().getGstin());
        customer = customerRepository.save(customer);

        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setCustomer(customer);
        invoice.setCreatedBy(submittedBy);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setBillingNote(request.getBillingNote());
        invoice.setStatus(InvoiceStatus.PENDING_APPROVAL);

        List<InvoiceItem> items = request.getItems().stream().map(dto -> {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setDescription(dto.getDescription());
            item.setCategory(dto.getCategory());
            item.setQuantity(dto.getQuantity());
            item.setRate(dto.getRate());
            item.setAmount(dto.getRate().multiply(BigDecimal.valueOf(dto.getQuantity())));
            return item;
        }).collect(Collectors.toList());
        invoice.setItems(items);

        BigDecimal subtotal = items.stream()
                .map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = subtotal
                .multiply(request.getTaxPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount);

        invoice.setSubtotal(subtotal);
        invoice.setTaxPercent(request.getTaxPercent());
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
        invoice.setAmountInWords(AmountToWordsUtil.convert(total));

        invoiceRepository.save(invoice);

        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getStatus().name(),
                company.getName(),
                customer.getName(),
                subtotal,
                request.getTaxPercent(),
                taxAmount,
                total,
                invoice.getAmountInWords(),
                company.getCurrencyLabel()
        );
    }

    /** Admin dashboard - everything waiting on a decision for this company. */
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> listPending(Long companyId) {
        return invoiceRepository.findByCompany_IdAndStatusOrderByCreatedAtDesc(companyId, InvoiceStatus.PENDING_APPROVAL)
                .stream().map(this::toSummary).toList();
    }
    
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> listForCompany(Long companyId, InvoiceStatus statusFilter) {
        List<Invoice> invoices = statusFilter != null
                ? invoiceRepository.findByCompany_IdAndStatusOrderByCreatedAtDesc(companyId, statusFilter)
                : invoiceRepository.findByCompany_IdOrderByCreatedAtDesc(companyId);
        return invoices.stream().map(this::toSummary).toList();
    }

    /** Staff's own submissions, whatever their status. */
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDTO> listMine(Long companyId, Long userId) {
        return invoiceRepository.findByCompany_IdAndCreatedBy_IdOrderByCreatedAtDesc(companyId, userId)
                .stream().map(this::toSummary).toList();
    }

    /**
     * Step 2: an admin accepts it. This is the moment the invoice number is
     * drawn (per company, per financial year, e.g. "18/2025-26") and the
     * PDF is generated - after this point the invoice is final.
     */
    @Transactional
    public InvoiceSummaryDTO approve(Long invoiceId, CompanyPrincipal admin) {
        Invoice invoice = requirePendingInvoiceForCompany(invoiceId, admin);

        AppUser reviewer = appUserRepository.findByCompany_IdAndUsername(admin.companyId(), admin.username()).orElse(null);

        String fyLabel = financialYearLabel(invoice.getInvoiceDate());
        long nextSeq = invoiceRepository.countByCompany_IdAndFinancialYearLabel(invoice.getCompany().getId(), fyLabel) + 1;
        invoice.setFinancialYearLabel(fyLabel);
        invoice.setInvoiceNumber(nextSeq + "/" + fyLabel);
        invoice.setStatus(InvoiceStatus.APPROVED);
        invoice.setReviewedBy(reviewer);
        invoice.setReviewedAt(LocalDateTime.now());

        String pdfPath = pdfGeneratorService.generate(invoice);
        invoice.setPdfPath(pdfPath);

        invoiceRepository.save(invoice);
        emailService.sendInvoice(invoice);

        return toSummary(invoice);
    }

    /** Step 2 (alternate outcome): admin sends it back with an optional reason. */
    @Transactional
    public InvoiceSummaryDTO reject(Long invoiceId, CompanyPrincipal admin, String reason) {
        Invoice invoice = requirePendingInvoiceForCompany(invoiceId, admin);

        AppUser reviewer = appUserRepository.findByCompany_IdAndUsername(admin.companyId(), admin.username()).orElse(null);

        invoice.setStatus(InvoiceStatus.REJECTED);
        invoice.setReviewedBy(reviewer);
        invoice.setReviewedAt(LocalDateTime.now());
        invoice.setRejectionReason(reason == null || reason.isBlank() ? "No reason given" : reason);

        invoiceRepository.save(invoice);
        return toSummary(invoice);
    }

    private Invoice requirePendingInvoiceForCompany(Long invoiceId, CompanyPrincipal admin) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalStateException("Invoice not found"));
        if (!invoice.getCompany().getId().equals(admin.companyId())) {
            throw new SecurityException("Not authorized for this company's invoices");
        }
        if (invoice.getStatus() != InvoiceStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("This invoice has already been reviewed");
        }
        return invoice;
    }

    // Indian financial year: April -> March. An invoice dated Aug 2025 falls in FY "2025-26".
    private String financialYearLabel(LocalDateTime date) {
        int year = date.getYear();
        int fyStart = date.getMonthValue() >= 4 ? year : year - 1;
        return fyStart + "-" + String.valueOf(fyStart + 1).substring(2);
    }

    private InvoiceSummaryDTO toSummary(Invoice invoice) {
        List<InvoiceItemViewDTO> items = invoice.getItems().stream()
                .map(it -> new InvoiceItemViewDTO(it.getDescription(), it.getCategory(), it.getQuantity(), it.getRate(), it.getAmount()))
                .toList();

        return new InvoiceSummaryDTO(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus().name(),
                invoice.getCustomer().getName(),
                invoice.getCustomer().getAddress(),
                invoice.getCustomer().getGstin(),
                invoice.getCustomer().getEmail(),
                invoice.getCustomer().getPhone(),
                invoice.getBillingNote(),
                items,
                invoice.getSubtotal(),
                invoice.getTaxPercent(),
                invoice.getTaxAmount(),
                invoice.getTotal(),
                invoice.getAmountInWords(),
                invoice.getCompany().getCurrencyLabel(),
                invoice.getCreatedBy() != null ? invoice.getCreatedBy().getUsername() : null,
                invoice.getCreatedAt(),
                invoice.getRejectionReason(),
                invoice.getStatus() == InvoiceStatus.APPROVED && invoice.getReviewedBy() != null
                ? invoice.getReviewedBy().getDisplayName() : null,
                invoice.getStatus() == InvoiceStatus.APPROVED && invoice.getReviewedBy() != null
                ? invoice.getReviewedBy().getId() : null
        );
    }
}
