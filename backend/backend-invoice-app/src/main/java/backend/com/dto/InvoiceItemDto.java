package backend.com.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemDto {

    @NotBlank
    private String description;

    private String category;

    @Positive
    private Integer quantity;

    @Positive
    private BigDecimal rate;
}
