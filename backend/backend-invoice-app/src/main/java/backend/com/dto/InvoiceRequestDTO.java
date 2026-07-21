package backend.com.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceRequestDTO {
    @Valid
    @NotNull
    private CustomerDTO customer;

    @Valid
    @NotEmpty
    private List<InvoiceItemDto> items;

    // GST % - user can increase/decrease on the form (e.g. 0, 5, 12, 18, 28)
    @NotNull
    private BigDecimal taxPercent;

    // Optional - defaults to "Order placed on {date}" if left blank
    private String billingNote;
}
