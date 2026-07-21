package backend.com.entity;

public enum InvoiceStatus {
    PENDING_APPROVAL,  // staff submitted it, waiting for an admin to review
    APPROVED,          // admin accepted it - invoice number assigned, PDF generated, buyer can be billed
    REJECTED           // admin sent it back - staff can see the reason and resubmit as a new invoice
}
