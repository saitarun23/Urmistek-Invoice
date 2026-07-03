import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8181",
  headers: { "Content-Type": "application/json" },
});

// Step 1: submit the form -> backend creates a PENDING invoice + Razorpay order
export const createInvoice = (payload) =>
  api.post("/api/invoices", payload).then((res) => res.data);

// Step 2: called after Razorpay's checkout succeeds, backend verifies the signature
export const verifyPayment = (payload) =>
  api.post("/api/payments/verify", payload).then((res) => res.data);

// Step 3: scoped download link - only works if the email matches the invoice's customer
export const invoiceDownloadUrl = (invoiceId, email) =>
  `${api.defaults.baseURL}/api/invoices/${invoiceId}/download?email=${encodeURIComponent(email)}`;

export default api;
