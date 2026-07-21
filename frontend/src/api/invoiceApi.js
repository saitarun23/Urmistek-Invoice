import api from "./apiClient.js";

// Step 1 - submit the form. Comes back PENDING_APPROVAL, no invoice number yet.
export const createInvoice = (payload) =>
  api.post("/api/invoices", payload).then((res) => res.data);

// Everything the current user has submitted, with its current status
export const listMyInvoices = () => api.get("/api/invoices/mine").then((res) => res.data);

// Admin-only: everything waiting on a decision for this company
export const listPendingInvoices = () => api.get("/api/invoices/pending").then((res) => res.data);

// Step 2a - admin accepts it: invoice number assigned, PDF generated
export const approveInvoice = (id) => api.post(`/api/invoices/${id}/approve`).then((res) => res.data);

// Step 2b - admin sends it back, optionally with a reason
export const rejectInvoice = (id, reason) =>
  api.post(`/api/invoices/${id}/reject`, { reason }).then((res) => res.data);

export const listCompanyInvoices = (status) =>
  api.get("/api/invoices/company", { params: status ? { status } : {} }).then((res) => res.data);

// Download needs the Authorization header, so it's fetched as a blob
// rather than a plain <a href> link (which can't carry the JWT).
export const downloadInvoicePdf = async (invoiceId, invoiceNumber) => {
  const res = await api.get(`/api/invoices/${invoiceId}/download`, { responseType: "blob" });
  const url = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", `${(invoiceNumber || String(invoiceId)).replace("/", "-")}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

export default api;
