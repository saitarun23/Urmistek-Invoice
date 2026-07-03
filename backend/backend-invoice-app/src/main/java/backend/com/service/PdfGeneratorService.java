package backend.com.service;

import backend.com.entity.Invoice;
import backend.com.entity.InvoiceItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;


/**
 * Renders the invoice PDF to match the URMISTEK sample invoice layout:
 * logo -> tagline tabs -> black bar -> company name -> address ->
 * invoice no/date -> bill-to -> item table -> subtotal/GSTIN/total ->
 * amount in words -> remittance details -> signatory -> black bar.
 *
 * Company details are read from application.properties so this same
 * class can be reused for any client, not just URMISTEK.
 */
@Service
public class PdfGeneratorService {

    @Value("${invoice.pdf.storage-path}")
    private String storagePath;

    @Value("${company.name}")
    private String companyName;

    @Value("${company.address}")
    private String companyAddress;

    @Value("${company.gstin}")
    private String companyGstin;

    @Value("${company.hsn}")
    private String companyHsn;

    @Value("${company.logo-path}")
    private String logoPath;

    @Value("${company.tagline-tabs}")
    private String taglineTabs;

    @Value("${company.bank.account-name}")
    private String bankAccountName;

    @Value("${company.bank.name}")
    private String bankName;

    @Value("${company.bank.account-number}")
    private String bankAccountNumber;

    @Value("${company.bank.ifsc}")
    private String bankIfsc;

    @Value("${invoice.currency-label}")
    private String currencyLabel;

    private final ResourceLoader resourceLoader;

    private static final BaseColor BLACK_BAR = new BaseColor(20, 20, 20);
    private static final BaseColor HEADER_GREY = new BaseColor(191, 191, 191);
    private static final BaseColor LIGHT_GREY = new BaseColor(242, 242, 242);

    private static final Font BRAND_TITLE = new Font(Font.FontFamily.HELVETICA, 30, Font.BOLD);
    private static final Font TAB_LABEL = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(120, 120, 120));
    private static final Font ADDRESS = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
    private static final Font LABEL = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font NORMAL = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
    private static final Font TABLE_HEADER = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
    private static final Font BOLD_SMALL = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

    public PdfGeneratorService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String generate(Invoice invoice) {
        try {
            Files.createDirectories(Path.of(storagePath));
            String fileName = invoice.getInvoiceNumber() + ".pdf";
            String fullPath = Path.of(storagePath, fileName).toString();

            Document document = new Document(PageSize.A4, 30, 30, 20, 30);
            PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            document.open();

            // Column widths mirror the sample: SI.No | Description | Category | Qty | Rate | Total
            float[] widths = {0.6f, 3f, 1.4f, 1f, 1.5f, 1.5f};

            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);

            addLogoRow(table);
            addTaglineTabsRow(table);
            addBlackBar(table, 6);
            addCompanyNameRow(table);
            addAddressRow(table);
            addInvoiceMetaRows(table, invoice);
            addBillToRow(table, invoice);
            addItemTableHeader(table);
            addBillingNoteRow(table, invoice);
            addItemRows(table, invoice);
            addSubtotalRow(table, invoice);
            addGstinAndTotalRows(table, invoice);
            addAmountInWordsRow(table, invoice);
            addRemittanceSection(table);
            addSignatoryRow(table);
            addBlackBar(table, 6);

            document.add(table);
            document.close();
            return fullPath;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    private void addLogoRow(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setColspan(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(8);
        cell.setPaddingBottom(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        try {
            Resource resource = resourceLoader.getResource(logoPath);
            if (resource.exists()) {
                Image logo = Image.getInstance(resource.getURL());
                logo.scaleToFit(70, 70);
                logo.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(logo);
            } else {
                cell.addElement(new Paragraph(" "));
            }
        } catch (Exception e) {
            // No logo configured yet - leave the row blank rather than fail PDF generation
            cell.addElement(new Paragraph(" "));
        }
        table.addCell(cell);
    }

    private void addTaglineTabsRow(PdfPTable table) {
        if (taglineTabs == null || taglineTabs.isBlank()) return;
        String[] tabs = taglineTabs.split(",");
        String joined = String.join("        ", tabs); // wide spacing, matches the ribbon look
        PdfPCell cell = new PdfPCell(new Phrase(joined, TAB_LABEL));
        cell.setColspan(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingBottom(8);
        table.addCell(cell);
    }

    private void addBlackBar(PdfPTable table, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setColspan(colspan);
        cell.setFixedHeight(10f);
        cell.setBackgroundColor(BLACK_BAR);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addCompanyNameRow(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase(companyName, BRAND_TITLE));
        cell.setColspan(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingTop(14);
        cell.setPaddingBottom(14);
        table.addCell(cell);
    }

    private void addAddressRow(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase(companyAddress, ADDRESS));
        cell.setColspan(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addInvoiceMetaRows(PdfPTable table, Invoice invoice) {
        PdfPCell invoiceLabel = new PdfPCell(new Phrase("INVOICE", LABEL));
        invoiceLabel.setColspan(3);
        invoiceLabel.setRowspan(2);
        invoiceLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        invoiceLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        invoiceLabel.setPadding(8);
        table.addCell(invoiceLabel);

        table.addCell(labeledCell("Invoice No", 1));
        table.addCell(valueCell(invoice.getInvoiceNumber(), 2));

        table.addCell(labeledCell("Date", 1));
        table.addCell(valueCell(
                invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 2));
    }

    private void addBillToRow(PdfPTable table, Invoice invoice) {
        StringBuilder sb = new StringBuilder("To,\n");
        sb.append(invoice.getCustomer().getName()).append("\n");
        if (invoice.getCustomer().getAddress() != null) sb.append(invoice.getCustomer().getAddress()).append("\n");
        sb.append(invoice.getCustomer().getEmail()).append("  |  ").append(invoice.getCustomer().getPhone());

        PdfPCell cell = new PdfPCell(new Phrase(sb.toString(), NORMAL));
        cell.setColspan(6);
        cell.setMinimumHeight(60);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addItemTableHeader(PdfPTable table) {
        String[] headers = {"SI.No", "DESCRIPTION", "Category", "No Of\nResources", "Amount\n(" + currencyLabel + ")", "TOTAL (" + currencyLabel + ")"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER));
            cell.setBackgroundColor(HEADER_GREY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private void addBillingNoteRow(PdfPTable table, Invoice invoice) {
        String note = invoice.getBillingNote() != null
                ? invoice.getBillingNote()
                : "Order placed on " + invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        PdfPCell cell = new PdfPCell(new Phrase(note, BOLD_SMALL));
        cell.setColspan(6);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addItemRows(PdfPTable table, Invoice invoice) {
        int si = 1;
        for (InvoiceItem item : invoice.getItems()) {
            table.addCell(dataCell(String.valueOf(si++), Element.ALIGN_CENTER));
            table.addCell(dataCell(item.getDescription(), Element.ALIGN_LEFT));
            table.addCell(dataCell(item.getCategory() == null ? "-" : item.getCategory(), Element.ALIGN_CENTER));
            table.addCell(dataCell(String.valueOf(item.getQuantity()), Element.ALIGN_CENTER));
            table.addCell(dataCell(item.getRate().toString(), Element.ALIGN_RIGHT));
            table.addCell(dataCell(item.getAmount().toString(), Element.ALIGN_RIGHT));
        }
    }

    private void addSubtotalRow(PdfPTable table, Invoice invoice) {
        PdfPCell blank = new PdfPCell(new Phrase(" "));
        blank.setColspan(4);
        blank.setBorder(Rectangle.NO_BORDER);
        table.addCell(blank);

        PdfPCell label = new PdfPCell(new Phrase("SUB TOTAL", LABEL));
        label.setBackgroundColor(LIGHT_GREY);
        table.addCell(label);

        PdfPCell value = new PdfPCell(new Phrase(invoice.getSubtotal().toString(), LABEL));
        value.setBackgroundColor(LIGHT_GREY);
        value.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(value);
    }

    private void addGstinAndTotalRows(PdfPTable table, Invoice invoice) {
        PdfPCell gstin = new PdfPCell(new Phrase("GSTIN No:- " + emptyDash(companyGstin), NORMAL));
        gstin.setColspan(4);
        gstin.setPadding(6);
        table.addCell(gstin);

        PdfPCell taxLabel = new PdfPCell(new Phrase("Tax (" + invoice.getTaxPercent() + "%)", NORMAL));
        table.addCell(taxLabel);
        PdfPCell taxValue = new PdfPCell(new Phrase(invoice.getTaxAmount().toString(), NORMAL));
        taxValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(taxValue);

        PdfPCell hsn = new PdfPCell(new Phrase("HSN SAC:- " + emptyDash(companyHsn), NORMAL));
        hsn.setColspan(4);
        hsn.setPadding(6);
        table.addCell(hsn);

        PdfPCell totalLabel = new PdfPCell(new Phrase("Total", LABEL));
        totalLabel.setBackgroundColor(LIGHT_GREY);
        table.addCell(totalLabel);
        PdfPCell totalValue = new PdfPCell(new Phrase(invoice.getTotal().toString(), LABEL));
        totalValue.setBackgroundColor(LIGHT_GREY);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalValue);
    }

    private void addAmountInWordsRow(PdfPTable table, Invoice invoice) {
        PdfPCell cell = new PdfPCell(new Phrase(
                "Amount In Words: " + AmountToWordsUtil.convert(invoice.getTotal()) + " " + currencyLabel + " only",
                NORMAL));
        cell.setColspan(6);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addRemittanceSection(PdfPTable table) {
        PdfPCell title = new PdfPCell(new Phrase("Remittance Details (for reference)", LABEL));
        title.setColspan(6);
        title.setPadding(6);
        table.addCell(title);

        String[][] rows = {
                {"Beneficiary Account Name", bankAccountName},
                {"Beneficiary Bank", bankName},
                {"Beneficiary Account Number", emptyDash(bankAccountNumber)},
                {"IFSC Code", bankIfsc}
        };
        for (String[] row : rows) {
            PdfPCell label = new PdfPCell(new Phrase(row[0], NORMAL));
            label.setColspan(3);
            label.setPadding(5);
            table.addCell(label);

            PdfPCell value = new PdfPCell(new Phrase(row[1], NORMAL));
            value.setColspan(3);
            value.setPadding(5);
            table.addCell(value);
        }
    }

    private void addSignatoryRow(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase(
                "\nfor " + companyName + "\nAuthorized Signatory\n\nWe Thank You For Your Business!", NORMAL));
        cell.setColspan(6);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private PdfPCell labeledCell(String text, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL));
        cell.setColspan(colspan);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell valueCell(String text, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BOLD_SMALL));
        cell.setColspan(colspan);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell dataCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL));
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        return cell;
    }

    private String emptyDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
