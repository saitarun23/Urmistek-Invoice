import { NavLink } from "react-router-dom";
import { LogOut } from "lucide-react";
import { useAuth } from "../context/AuthContext.jsx";
import "./TopNav.css";

export default function TopNav() {
  const { session, logout } = useAuth();
  const isAdmin = session?.role === "ADMIN";

  return (
    <div className="top-nav">
      <div className="top-nav-identity">
        <span className="top-nav-company">{session?.companyName}</span>
        <span className="top-nav-user"> &middot; {session?.displayName || session?.username}</span>
      </div>

      <div className="top-nav-links">
        {/* Staff submit and track their own invoices - admins review everyone's,
            so they get a different set of links instead of both sets at once. */}
        {!isAdmin && (
          <>
            <NavLink to="/invoices/new" className={({ isActive }) => (isActive ? "nav-link nav-link-active" : "nav-link")}>
              New invoice
            </NavLink>
            <NavLink to="/invoices/mine" className={({ isActive }) => (isActive ? "nav-link nav-link-active" : "nav-link")}>
              My invoices
            </NavLink>
          </>
        )}

        {isAdmin && (
          <>
            <NavLink to="/admin/approvals" className={({ isActive }) => (isActive ? "nav-link nav-link-active" : "nav-link")}>
              Invoices
            </NavLink>
            <NavLink to="/admin/users" className={({ isActive }) => (isActive ? "nav-link nav-link-active" : "nav-link")}>
              Users
            </NavLink>
          </>
        )}

        <button className="logout-link" onClick={logout}>
          <LogOut size={14} /> Sign out
        </button>
      </div>
    </div>
  );
}
