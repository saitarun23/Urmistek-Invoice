import { createContext, useContext, useState, useCallback } from "react";
import { login as loginApi } from "../api/authApi.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => {
    const raw = localStorage.getItem("b2b_session");
    return raw ? JSON.parse(raw) : null;
  });

  const login = useCallback(async (companyCode, username, password) => {
    const data = await loginApi(companyCode, username, password);
    const nextSession = {
      username: data.username,
      displayName: data.displayName,
      companyCode: data.companyCode,
      companyName: data.companyName,
      role: data.role,
    };
    localStorage.setItem("b2b_token", data.token);
    localStorage.setItem("b2b_session", JSON.stringify(nextSession));
    setSession(nextSession);
    return nextSession;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("b2b_token");
    localStorage.removeItem("b2b_session");
    setSession(null);
  }, []);

  return (
    <AuthContext.Provider value={{ session, login, logout, isAuthenticated: !!session }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
}
