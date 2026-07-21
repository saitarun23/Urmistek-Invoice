import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8181",
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT (if we have one) to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("b2b_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token has expired/is invalid, bounce back to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem("b2b_token");
      localStorage.removeItem("b2b_session");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default api;
