import api from "./apiClient.js";

export const listUsers = () => api.get("/api/users").then((res) => res.data);
export const createUser = (payload) => api.post("/api/users", payload).then((res) => res.data);

export const uploadMySignature = (file) => {
  const form = new FormData();
  form.append("file", file);
  return api.post("/api/users/me/signature", form, {
    headers: { "Content-Type": "multipart/form-data" },
  }).then((res) => res.data);
};

export const fetchSignatureUrl = async (userId) => {
  try {
    const res = await api.get(`/api/users/${userId}/signature`, { responseType: "blob" });
    return window.URL.createObjectURL(res.data);
  } catch {
    return null;
  }
};