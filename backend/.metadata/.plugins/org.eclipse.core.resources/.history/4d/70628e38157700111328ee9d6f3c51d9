package backend.com.controller;


import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import backend.com.dto.InvoiceRequestDTO;
import backend.com.dto.InvoiceResponseDTO;
import backend.com.entity.Invoice;
import backend.com.entity.InvoiceStatus;
import backend.com.repository.InvoiceRepository;
import backend.com.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
    }

    // Step 1: user submits the form -> we create a PENDING invoice + Razorpay order
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceService.createPendingInvoice(request));
    }

    // Step 3: user (or their email link) downloads the PDF once paid.
    // Scoped by invoice id + email so only the person who placed the order can fetch it.
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadInvoice(@PathVariable Long id, @RequestParam String email) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (!invoice.getCustomer().getEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.status(403).body("Not authorized to view this invoice");
        }
        if (invoice.getStatus() != InvoiceStatus.PAID || invoice.getPdfPath() == null) {
            return ResponseEntity.status(409).body("Invoice is not paid yet");
        }

        FileSystemResource file = new FileSystemResource(invoice.getPdfPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}
