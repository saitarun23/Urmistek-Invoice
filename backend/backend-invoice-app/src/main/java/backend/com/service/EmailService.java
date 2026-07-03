package backend.com.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import backend.com.entity.Invoice;

import java.io.File;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoice(Invoice invoice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromAddress);
            helper.setTo(invoice.getCustomer().getEmail());
            helper.setSubject("Your invoice " + invoice.getInvoiceNumber());
            helper.setText(
                    "Hi " + invoice.getCustomer().getName() + ",\n\n" +
                    "Thanks for your payment. Your invoice " + invoice.getInvoiceNumber() +
                    " for " + invoice.getTotal() + " is attached.\n\nThank you!"
            );
            helper.addAttachment(invoice.getInvoiceNumber() + ".pdf", new File(invoice.getPdfPath()));

            mailSender.send(message);
        } catch (MessagingException e) {
            // Don't fail the payment flow if email fails - log it and let the user
            // still download the PDF from the success page / invoice endpoint.
            System.err.println("Failed to email invoice: " + e.getMessage());
        }
    }
}

