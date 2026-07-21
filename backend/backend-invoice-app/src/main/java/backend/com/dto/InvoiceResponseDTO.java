package backend.com.dto;

import java.math.BigDecimal;

// Returned right after a staff member submits the form - status is always
// PENDING_APPROVAL here, invoiceNumber is null (assigned only on approval).
public record InvoiceResponseDTO(
        Long invoiceId,
        String status,
        String companyName,
        String customerName,
        BigDecimal subtotal,
        BigDecimal taxPercent,
        BigDecimal taxAmount,
        BigDecimal total,
        String amountInWords,
        String currency
) {
}
