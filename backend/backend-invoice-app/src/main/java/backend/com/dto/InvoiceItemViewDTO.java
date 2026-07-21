package backend.com.dto;

import java.math.BigDecimal;

// Read-only view of a line item (includes the computed amount), used in
// InvoiceSummaryDTO - InvoiceItemDto is the input-side DTO and has no amount.
public record InvoiceItemViewDTO(
        String description,
        String category,
        Integer quantity,
        BigDecimal rate,
        BigDecimal amount
) {
}
