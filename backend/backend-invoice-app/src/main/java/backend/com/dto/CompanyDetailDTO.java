package backend.com.dto;

// Full branding detail for the logged-in user's own company - used only to
// display the fixed (non-editable) parts of the invoice preview: company
// name/address/GSTIN/HSN/tagline and the bank/remittance box. Never
// editable from the frontend; the source of truth is the companies table.
public record CompanyDetailDTO(
        String code,
        String name,
        String address,
        String gstin,
        String hsn,
        String taglineTabs,
        String currencyLabel,
        String bankAccountName,
        String bankName,
        String bankAccountNumber,
        String bankIfsc
) {
}
