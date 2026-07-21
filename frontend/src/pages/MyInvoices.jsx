import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { ChevronDown, ChevronUp, Download, Loader2 } from "lucide-react";
import { listMyInvoices, downloadInvoicePdf } from "../api/invoiceApi.js";
import { getMyCompany } from "../api/companyApi.js";
import InvoicePreview from "../components/InvoicePreview.jsx";
import TopNav from "../components/TopNav.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import "./InvoiceList.css";

export default function MyInvoices() {
  const location = useLocation();
  const justSubmittedId = location.state?.justSubmittedId;

  const [company, setCompany] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [expandedId, setExpandedId] = useState(justSubmittedId || null);
  const [downloadingId, setDownloadingId] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([listMyInvoices(), getMyCompany()])
      .then(([inv, comp]) => {
        setInvoices(inv);
        setCompany(comp);
      })
      .catch(() => setError("Couldn't load your invoices."))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleDownload = async (inv) => {
    setDownloadingId(inv.invoiceId);
    try {
      await downloadInvoicePdf(inv.invoiceId, inv.invoiceNumber);
    } finally {
      setDownloadingId(null);
    }
  };

  return (
    <div className="invoice-page">
      <TopNav />
      <h2>My invoices</h2>
      <p className="list-sub">Everything you've submitted, and where it stands.</p>

      {loading && (
        <div className="list-loading">
          <Loader2 className="spin" size={18} /> Loading...
        </div>
      )}
      {error && <p className="field-error">{error}</p>}
      {!loading && invoices.length === 0 && <p className="list-empty">No invoices submitted yet.</p>}

      <div className="invoice-list">
        {invoices.map((inv) => {
          const expanded = expandedId === inv.invoiceId;
          return (
            <div key={inv.invoiceId} className="invoice-list-card">
              <button className="invoice-list-row" onClick={() => setExpandedId(expanded ? null : inv.invoiceId)}>
                <div className="invoice-list-main">
                  <span className="invoice-list-number">
                    {inv.invoiceNumber || `Draft #${inv.invoiceId}`}
                  </span>
                  <span className="invoice-list-customer">{inv.customerName}</span>
                </div>
                <div className="invoice-list-meta">
                  <span className="invoice-list-total">
                    {inv.currency} {Number(inv.total).toFixed(2)}
                  </span>
                  <StatusBadge status={inv.status} />
                  {expanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                </div>
              </button>

              {expanded && (
                <div className="invoice-list-detail">
                  {inv.status === "REJECTED" && inv.rejectionReason && (
                    <p className="rejection-note">Rejected: {inv.rejectionReason}</p>
                  )}
                  <InvoicePreview
                    mode="view"
                    company={company}
                    invoiceMeta={{
                      invoiceNumber: inv.invoiceNumber,
                      invoiceDate: new Date(inv.createdAt).toLocaleDateString("en-GB"),
                    }}
                    customer={{
                      name: inv.customerName,
                      address: inv.customerAddress,
                      email: inv.customerEmail,
                      phone: inv.customerPhone,
                      gstin: inv.customerGstin,
                    }}
                    items={inv.items}
                    taxPercent={inv.taxPercent}
                    billingNote={inv.billingNote}
                  />
                  {inv.status === "APPROVED" && (
                    <button
                      className="primary-button detail-download"
                      onClick={() => handleDownload(inv)}
                      disabled={downloadingId === inv.invoiceId}
                    >
                      {downloadingId === inv.invoiceId ? (
                        <Loader2 size={16} className="spin" />
                      ) : (
                        <Download size={16} />
                      )}
                      Download PDF
                    </button>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
