package backend.com.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * One row per tenant (URMISTEK, UB INDUSTRIES, ...). Everything the PDF
 * needs to render that tenant's invoice header/footer lives here instead
 * of in application.properties, so adding a new company is a DB row, not
 * a code change.
 */
@Entity
@Table(name = "companies")
@Data
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short code used on the login screen and in the JWT, e.g. "URMISTEK", "UB_INDUSTRIES"
    @Column(nullable = false, unique = true)
    private String code;

    // Display name printed on the invoice, e.g. "URMISTEK", "UB Industries"
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    private String gstin;

    private String hsn;

    // classpath:static/logo/<file>.png or an absolute path
    private String logoPath;

    // Comma-separated tabs under the logo, e.g. "DEVELOPMENT,IT SERVICES,OUTSOURCING"
    private String taglineTabs;

    private String bankAccountName;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfsc;

    // "INR" / "AUD" / "AUD/INR" etc, printed next to amounts
    private String currencyLabel = "INR";

    // Where generated PDFs for this company are written
    private String pdfStoragePath;
}
