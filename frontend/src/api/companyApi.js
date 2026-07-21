import api from "./apiClient.js";

// Read-only - company name/address/GSTIN/HSN/tagline/bank details for
// whichever company the current JWT belongs to. Shown on the invoice
// preview but never editable from the UI.
export const getMyCompany = () => api.get("/api/companies/me").then((res) => res.data);
