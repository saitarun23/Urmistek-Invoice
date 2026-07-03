package backend.com.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.com.dto.PaymentVerificationDTO;
import backend.com.entity.Invoice;
import backend.com.service.InvoiceService;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final InvoiceService invoiceService;

    public PaymentController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Step 2: called from the frontend's Razorpay success handler
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody PaymentVerificationDTO verification) {
        try {
            Invoice invoice = invoiceService.confirmPayment(verification);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "invoiceId", invoice.getId(),
                    "invoiceNumber", invoice.getInvoiceNumber()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("status", "failed", "message", e.getMessage()));
        }
    }
}
