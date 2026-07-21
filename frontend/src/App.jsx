import {  Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import InvoiceForm from "./pages/InvoiceForm.jsx";
import MyInvoices from "./pages/MyInvoices.jsx";
import AdminApprovals from "./pages/AdminApprovals.jsx";
import AdminUsers from "./pages/AdminUsers.jsx";
import "./styles/theme.css";

export default function App() {
  return (
    <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/invoices/new"
            element={
              <ProtectedRoute>
                <InvoiceForm />
              </ProtectedRoute>
            }
          />
          <Route
            path="/invoices/mine"
            element={
              <ProtectedRoute>
                <MyInvoices />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/approvals"
            element={
              <ProtectedRoute adminOnly>
                <AdminApprovals />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute adminOnly>
                <AdminUsers />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
    </AuthProvider>
  );
}
