import api from "./apiClient.js";

// Public - powers the two login tiles (URMISTEK / UB Industries)
export const listCompanies = () => api.get("/api/companies").then((res) => res.data);

// companyCode: "URMISTEK" | "UB_INDUSTRIES"
export const login = (companyCode, username, password) =>
  api.post("/api/auth/login", { companyCode, username, password }).then((res) => res.data);
