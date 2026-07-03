package backend.com.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceResponseDTO {
    private Long invoiceId;
    private String invoiceNumber;
    private String razorpayOrderId;
    private BigDecimal amount;      // in rupees, frontend converts to paise if needed
    private String currency;
    private String razorpayKeyId;   // public key, safe to expose to frontend
}
