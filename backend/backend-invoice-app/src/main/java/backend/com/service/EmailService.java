package backend.com.service;

import backend.com.entity.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
        if (invoice.getCustomer().getEmail() == null || invoice.getCustomer().getEmail().isBlank()) {
            return; // B2B buyer may only have given a phone/address - nothing to email
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromAddress);
            helper.setTo(invoice.getCustomer().getEmail());
            helper.setSubject(invoice.getCompany().getName() + " - Invoice " + invoice.getInvoiceNumber());
            helper.setText(
                    "Hi " + invoice.getCustomer().getName() + ",\n\n" +
                    "Please find attached invoice " + invoice.getInvoiceNumber() +
                    " from " + invoice.getCompany().getName() + " for " + invoice.getTotal() + ".\n\n" +
                    "Thank you for your business!"
            );
            helper.addAttachment(invoice.getInvoiceNumber() + ".pdf", new File(invoice.getPdfPath()));
            mailSender.send(message);
        } catch (MessagingException e) {
            // Don't fail invoice generation if email fails - log it and let the user
            // still download the PDF from the success page.
            System.err.println("Failed to email invoice: " + e.getMessage());
        }
    }
}
