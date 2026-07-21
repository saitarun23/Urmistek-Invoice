import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Building2, Loader2, ArrowLeft, Lock } from "lucide-react";
import { listCompanies } from "../api/authApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import "./LoginPage.css";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [companies, setCompanies] = useState([]);
  const [loadingCompanies, setLoadingCompanies] = useState(true);
  const [selected, setSelected] = useState(null); // the chosen company option
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    listCompanies()
      .then(setCompanies)
      .catch(() => setError("Couldn't reach the server. Is the backend running?"))
      .finally(() => setLoadingCompanies(false));
  }, []);

  const handleSelect = (company) => {
    setSelected(company);
    setError("");
  };

  const handleBack = () => {
    setSelected(null);
    setUsername("");
    setPassword("");
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const nextSession = await login(selected.code, username, password);
      navigate(nextSession.role === "ADMIN" ? "/admin/approvals" : "/invoices/new");
    } catch (err) {
      setError(err?.response?.data?.message || "Invalid username or password.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="login-header">
          <h1>Invoice</h1>
          <p>{selected ? `Sign in to ${selected.name}` : "Choose your company to continue"}</p>
        </div>

        {!selected && (
          <div className="company-picker">
            {loadingCompanies && (
              <div className="company-loading">
                <Loader2 className="spin" size={20} /> Loading companies...
              </div>
            )}

            {!loadingCompanies &&
              companies.map((c) => (
                <button key={c.code} className="company-tile" onClick={() => handleSelect(c)}>
                  <Building2 size={28} />
                  <span className="company-tile-name">{c.name}</span>
                  <span className="company-tile-cta">Continue &rarr;</span>
                </button>
              ))}

            {!loadingCompanies && companies.length === 0 && !error && (
              <p className="company-empty">No companies configured yet.</p>
            )}

            {error && <p className="login-error">{error}</p>}
          </div>
        )}

        {selected && (
          <form className="login-form" onSubmit={handleSubmit}>
            <button type="button" className="back-link" onClick={handleBack}>
              <ArrowLeft size={14} /> Choose a different company
            </button>

            <label className="field">
              <span>Username</span>
              <input
                autoFocus
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. admin"
                required
              />
            </label>

            <label className="field">
              <span>Password</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="********"
                required
              />
            </label>

            {error && <p className="login-error">{error}</p>}

            <button className="login-submit" type="submit" disabled={submitting}>
              {submitting ? <Loader2 size={16} className="spin" /> : <Lock size={16} />}
              {submitting ? "Signing in..." : `Sign in to ${selected.name}`}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
