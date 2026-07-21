import { useEffect, useState } from "react";
import { ChevronDown, ChevronUp, Check, X, Loader2, Download } from "lucide-react";
import { listCompanyInvoices, approveInvoice, rejectInvoice, downloadInvoicePdf } from "../api/invoiceApi.js";
import { getMyCompany } from "../api/companyApi.js";
import InvoicePreview from "../components/InvoicePreview.jsx";
import TopNav from "../components/TopNav.jsx";
import StatusBadge from "../components/StatusBadge.jsx";
import "./InvoiceList.css";

// Which tabs show up, and which backend status each one asks for.
// "All" passes no status filter at all, so nothing is ever unreachable.
const TABS = [
  { key: "PENDING_APPROVAL", label: "Pending" },
  { key: "APPROVED", label: "Approved" },
  { key: "REJECTED", label: "Rejected" },
  { key: "ALL", label: "All" },
];

export default function AdminApprovals() {
  const [activeTab, setActiveTab] = useState("PENDING_APPROVAL");
  const [company, setCompany] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [expandedId, setExpandedId] = useState(null);
  const [actingId, setActingId] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);

  const load = (tab = activeTab) => {
    setLoading(true);
    setError("");
    const statusParam = tab === "ALL" ? undefined : tab;
    Promise.all([listCompanyInvoices(statusParam), getMyCompany()])
      .then(([inv, comp]) => {
        setInvoices(inv);
        setCompany(comp);
      })
      .catch(() => setError("Couldn't load invoices."))
      .finally(() => setLoading(false));
  };

  useEffect(() => load(activeTab), [activeTab]);

  const switchTab = (tabKey) => {
    setActiveTab(tabKey);
    setExpandedId(null);
  };

  const handleApprove = async (id) => {
    setActingId(id);
    try {
      await approveInvoice(id);
      // It doesn't vanish from the app - it just moves out of this tab.
      setInvoices((list) => list.filter((inv) => inv.invoiceId !== id));
    } catch (err) {
      setError(err?.response?.data?.message || "Couldn't approve this invoice.");
    } finally {
      setActingId(null);
    }
  };

  const handleReject = async (id) => {
    const reason = window.prompt("Reason for rejecting this invoice (optional):") || "";
    setActingId(id);
    try {
      await rejectInvoice(id, reason);
      setInvoices((list) => list.filter((inv) => inv.invoiceId !== id));
    } catch (err) {
      setError(err?.response?.data?.message || "Couldn't reject this invoice.");
    } finally {
      setActingId(null);
    }
  };

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
      <h2>Invoices</h2>
      <p className="list-sub">Everything submitted by your team - pending, approved, or rejected.</p>

      <div className="admin-tabs">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            className={activeTab === tab.key ? "admin-tab admin-tab-active" : "admin-tab"}
            onClick={() => switchTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading && (
        <div className="list-loading">
          <Loader2 className="spin" size={18} /> Loading...
        </div>
      )}
      {error && <p className="field-error">{error}</p>}
      {!loading && invoices.length === 0 && <p className="list-empty">Nothing here yet.</p>}

      <div className="invoice-list">
        {invoices.map((inv) => {
          const expanded = expandedId === inv.invoiceId;
          const acting = actingId === inv.invoiceId;
          return (
            <div key={inv.invoiceId} className="invoice-list-card">
              <button className="invoice-list-row" onClick={() => setExpandedId(expanded ? null : inv.invoiceId)}>
                <div className="invoice-list-main">
                  <span className="invoice-list-number">
                    {inv.invoiceNumber || inv.customerName}
                  </span>
                  <span className="invoice-list-customer">
                    {inv.invoiceNumber ? `${inv.customerName} \u00b7 ` : ""}
                    Submitted by {inv.createdByUsername || "unknown"} &middot;{" "}
                    {new Date(inv.createdAt).toLocaleDateString("en-GB")}
                  </span>
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
                      status: inv.status,
                      invoiceDate: new Date(inv.createdAt).toLocaleDateString("en-GB"),
                    }}
                    reviewedBy={{
                      displayName: inv.reviewedByDisplayName,
                      signatureUserId: inv.reviewedBySignatureUserId,
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

                  {inv.status === "PENDING_APPROVAL" && (
                    <div className="review-actions">
                      <button className="approve-button" disabled={acting} onClick={() => handleApprove(inv.invoiceId)}>
                        {acting ? <Loader2 size={15} className="spin" /> : <Check size={15} />}
                        Approve
                      </button>
                      <button className="reject-button" disabled={acting} onClick={() => handleReject(inv.invoiceId)}>
                        {acting ? <Loader2 size={15} className="spin" /> : <X size={15} />}
                        Reject
                      </button>
                    </div>
                  )}

                  {inv.status === "APPROVED" && (
                    <button
                      className="primary-button detail-download"
                      onClick={() => handleDownload(inv)}
                      disabled={downloadingId === inv.invoiceId}
                    >
                      {downloadingId === inv.invoiceId ? <Loader2 size={16} className="spin" /> : <Download size={16} />}
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
