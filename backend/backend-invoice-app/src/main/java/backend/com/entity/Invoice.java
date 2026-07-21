package backend.com.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Null until an admin approves - assigned then, per company per financial year (e.g. "18/2025-26")
    @Column(unique = true)
    private String invoiceNumber;

    // Which financial year (Apr-Mar) bucket this invoice's number was drawn from, e.g. "2025-26"
    private String financialYearLabel;

    private LocalDateTime invoiceDate;

    // e.g. "Being raised for the month of AUGUST '2025" - shown as a
    // section header above the line items, matching the sample invoice
    private String billingNote;

    // Which tenant issued this invoice (URMISTEK / UB Industries / ...)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Who was logged in when it was submitted
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    // Which admin approved/rejected it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private AppUser reviewedBy;

    private LocalDateTime reviewedAt;

    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String amountInWords;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.PENDING_APPROVAL;

    private String pdfPath;

    private LocalDateTime createdAt = LocalDateTime.now();
}
