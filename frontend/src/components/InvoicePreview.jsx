import { useEffect, useState } from "react";
import { Plus, Trash2, Minus } from "lucide-react";
import { amountToWords } from "../utils/amountToWords.js";
import { fetchSignatureUrl } from "../api/usersApi.js";
import "./InvoicePreview.css";

const GST_STEP = 0.5;

/**
 * One component renders the invoice three ways:
 *  - mode="edit"   -> InvoiceForm: fixed company data is plain text,
 *                     customer/items/GST are yellow-highlighted inputs
 *                     (matching the yellow cells on the original Word doc)
 *  - mode="view"    -> everything read-only (admin approval queue, my invoices)
 *
 * `company` is always the fixed, non-editable branding block.
 */
export default function InvoicePreview({
  mode = "view",
  company,
  invoiceMeta, // { invoiceNumber, status, invoiceDate }
  reviewedBy,  // { displayName, signatureUserId } - only meaningful when status === "APPROVED"
  customer,
  onCustomerChange,
  items,
  onItemChange,
  onAddItem,
  onRemoveItem,
  taxPercent,
  onTaxPercentChange,
  billingNote,
  onBillingNoteChange,
}) {
  const editable = mode === "edit";
  const status = invoiceMeta?.status;

  const subtotal = items.reduce(
    (sum, it) => sum + (Number(it.quantity) || 0) * (Number(it.rate) || 0),
    0
  );
  const tax = subtotal * (Number(taxPercent) / 100);
  const total = subtotal + tax;

  const adjustTax = (delta) =>
    onTaxPercentChange(Math.min(100, Math.max(0, Number((Number(taxPercent) + delta).toFixed(2)))));

  // The signature image only exists to show once an admin has approved this
  // exact invoice - never for a draft being edited, and never for a
  // rejected one, even if that same admin has a signature on file.
  const [signatureUrl, setSignatureUrl] = useState(null);
  useEffect(() => {
    let objectUrl = null;
    if (status === "APPROVED" && reviewedBy?.signatureUserId) {
      fetchSignatureUrl(reviewedBy.signatureUserId).then((url) => {
        objectUrl = url;
        setSignatureUrl(url);
      });
    } else {
      setSignatureUrl(null);
    }
    return () => {
      if (objectUrl) window.URL.revokeObjectURL(objectUrl);
    };
  }, [status, reviewedBy?.signatureUserId]);

  return (
    <div className="invoice-doc">
      <div className="doc-black-bar" />

      <div className="doc-header">
        {company?.taglineTabs && (
          <div className="doc-tagline">
            {company.taglineTabs.split(",").map((t) => (
              <span key={t}>{t.trim()}</span>
            ))}
          </div>
        )}
        <h1 className="doc-company-name">{company?.name || "..."}</h1>
        <p className="doc-company-address">{company?.address || "-"}</p>
      </div>

      <div className="doc-meta-row">
        <div className="doc-meta-badge">INVOICE</div>
        <div className="doc-meta-fields">
          <div className="doc-meta-field">
            <span className="doc-label">Invoice No</span>
            <span className="doc-fixed-value">
              {invoiceMeta?.invoiceNumber || (
                <em className="doc-pending">Assigned on approval</em>
              )}
            </span>
          </div>
          <div className="doc-meta-field">
            <span className="doc-label">Date</span>
            <span className="doc-fixed-value">{invoiceMeta?.invoiceDate}</span>
          </div>
        </div>
      </div>

      <div className="doc-billto">
        <span className="doc-label">To,</span>
        {editable ? (
          <div className="doc-billto-edit">
            <input
              className="yellow-field"
              placeholder="Customer / company name"
              value={customer.name}
              onChange={(e) => onCustomerChange("name", e.target.value)}
            />
            <textarea
              className="yellow-field"
              rows={2}
              placeholder="Billing address"
              value={customer.address}
              onChange={(e) => onCustomerChange("address", e.target.value)}
            />
            <div className="doc-billto-row">
              <input
                className="yellow-field"
                placeholder="Email"
                value={customer.email}
                onChange={(e) => onCustomerChange("email", e.target.value)}
              />
              <input
                className="yellow-field"
                placeholder="Phone "
                value={customer.phone}
                onChange={(e) => onCustomerChange("phone", e.target.value)}
              />
              <input
                className="yellow-field"
                placeholder="GSTIN"
                value={customer.gstin}
                onChange={(e) => onCustomerChange("gstin", e.target.value)}
              />
            </div>
          </div>
        ) : (
          <div className="doc-billto-view">
            <strong>{customer.name}</strong>
            {customer.address && <span>{customer.address}</span>}
            <span className="doc-billto-contact">
              {[customer.email, customer.phone].filter(Boolean).join("  |  ")}
              {customer.gstin ? `  |  GSTIN: ${customer.gstin}` : ""}
            </span>
          </div>
        )}
      </div>

      <table className="doc-item-table">
        <thead>
          <tr>
            <th style={{ width: "6%" }}>SI.No</th>
            <th style={{ width: "34%" }}>DESCRIPTION</th>
            <th style={{ width: "16%" }}>Category</th>
            <th style={{ width: "10%" }}>Qty</th>
            <th style={{ width: "16%" }}>Rate</th>
            <th style={{ width: "18%" }}>Total ({company?.currencyLabel || "INR"})</th>
          </tr>
        </thead>
        <tbody>
          <tr className="doc-billing-note-row">
            <td colSpan={6}>
              {editable ? (
                <input
                  className="yellow-field yellow-field-note"
                  placeholder="Billing note (optional) - e.g. Being raised for the month of August 2026"
                  value={billingNote}
                  onChange={(e) => onBillingNoteChange(e.target.value)}
                />
              ) : (
                billingNote || <em className="doc-pending">No billing note</em>
              )}
            </td>
          </tr>
          {items.map((it, idx) => (
            <tr key={it.id ?? idx}>
              <td className="doc-center">{idx + 1}</td>
              <td>
                {editable ? (
                  <input
                    className="yellow-field"
                    value={it.description}
                    onChange={(e) => onItemChange(it.id, "description", e.target.value)}
                  />
                ) : (
                  it.description
                )}
              </td>
              <td>
                {editable ? (
                  <input
                    className="yellow-field"
                    value={it.category}
                    onChange={(e) => onItemChange(it.id, "category", e.target.value)}
                  />
                ) : (
                  it.category || "-"
                )}
              </td>
              <td className="doc-center">
                {editable ? (
                  <input
                    className="yellow-field doc-qty-input"
                    type="number"
                    min={1}
                    value={it.quantity}
                    onChange={(e) => onItemChange(it.id, "quantity", e.target.value)}
                  />
                ) : (
                  it.quantity
                )}
              </td>
              <td className="doc-right">
                {editable ? (
                  <input
                    className="yellow-field doc-qty-input"
                    type="number"
                    min={0}
                    value={it.rate}
                    onChange={(e) => onItemChange(it.id, "rate", e.target.value)}
                  />
                ) : (
                  Number(it.rate).toFixed(2)
                )}
              </td>
              <td className="doc-right">
                {(it.amount ?? (Number(it.quantity) || 0) * (Number(it.rate) || 0)).toFixed(2)}
              </td>
              {editable && (
                <td className="doc-remove-cell">
                  <button type="button" className="icon-button" onClick={() => onRemoveItem(it.id)}>
                    <Trash2 size={15} />
                  </button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      {editable && (
        <button type="button" className="ghost-button doc-add-item" onClick={onAddItem}>
          <Plus size={15} /> Add item
        </button>
      )}

      <div className="doc-totals-block">
        <div className="doc-totals-left">
          <p>GSTIN No:- {company?.gstin || "-"}</p>
          <p>HSN SAC:- {company?.hsn || "-"}</p>
        </div>
        <div className="doc-totals-right">
          <div className="doc-totals-row">
            <span>SUB TOTAL</span>
            <span>{subtotal.toFixed(2)}</span>
          </div>
          <div className="doc-totals-row">
            <span className="doc-gst-label">
              Tax
              {editable ? (
                <span className="gst-stepper">
                  <button type="button" onClick={() => adjustTax(-GST_STEP)} aria-label="Decrease GST">
                    <Minus size={12} />
                  </button>
                  <input
                    type="number"
                    min={0}
                    max={100}
                    step={0.5}
                    value={taxPercent}
                    onChange={(e) => onTaxPercentChange(Number(e.target.value))}
                  />
                  <button type="button" onClick={() => adjustTax(GST_STEP)} aria-label="Increase GST">
                    <Plus size={12} />
                  </button>
                </span>
              ) : (
                ` (${taxPercent}%)`
              )}
            </span>
            <span>{tax.toFixed(2)}</span>
          </div>
          <div className="doc-totals-row doc-totals-final">
            <span>Total</span>
            <span>{total.toFixed(2)}</span>
          </div>
        </div>
      </div>

      <p className="doc-amount-words">
        Amount In Words: <em>{amountToWords(total)} {company?.currencyLabel || "INR"} only</em>
      </p>

      <div className="doc-remittance">
        <p className="doc-remittance-title">Remittance Details (for reference)</p>
        <div className="doc-remittance-grid">
          <span>Beneficiary Account Name</span>
          <span>{company?.bankAccountName || "-"}</span>
          <span>Beneficiary Bank</span>
          <span>{company?.bankName || "-"}</span>
          <span>Beneficiary Account Number</span>
          <span>{company?.bankAccountNumber || "-"}</span>
          <span>IFSC Code</span>
          <span>{company?.bankIfsc || "-"}</span>
        </div>
      </div>

      <div className="doc-signatory">
        {status === "APPROVED" ? (
          <>
            {signatureUrl && <img className="doc-signature-img" src={signatureUrl} alt="Signature" />}
            <p>for {company?.name}</p>
            <p>{reviewedBy?.displayName || "Authorized Signatory"}</p>
            <p className="doc-thankyou">We Thank You For Your Business!</p>
          </>
        ) : status === "REJECTED" ? (
          <p className="doc-rejected-note">This invoice was rejected - no signature applies.</p>
        ) : (
          <p className="doc-pending">Signature will appear here once an admin approves this invoice.</p>
        )}
      </div>

      <div className="doc-black-bar" />
    </div>
  );
}
