import { useState } from 'react'
import { Routes, Route } from "react-router-dom";
import InvoiceForm from "./components/InvoiceForm.jsx";
import PaymentSuccess from "./components/PaymentSuccess.jsx";
import PaymentFailed from "./components/PaymentFailed.jsx";
import './App.css'

function App() {
  return (
     <Routes>
      <Route path="/" element={<InvoiceForm />} />
      <Route path="/payment-success" element={<PaymentSuccess />} />
      <Route path="/payment-failed" element={<PaymentFailed />} />
    </Routes>
  );
}

export default App
