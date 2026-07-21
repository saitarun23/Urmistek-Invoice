package backend.com.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private String description;
    private String category;
    private Integer quantity;
    private BigDecimal rate;
    private BigDecimal amount; // quantity * rate, computed server-side
}
