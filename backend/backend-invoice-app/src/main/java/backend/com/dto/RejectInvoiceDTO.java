package backend.com.dto;

import lombok.Data;

@Data
public class RejectInvoiceDTO {
    private String reason; // optional, shown to the staff member who submitted it
}
