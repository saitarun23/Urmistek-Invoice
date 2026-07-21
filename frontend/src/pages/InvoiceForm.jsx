import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Loader2, LogOut } from "lucide-react";
import { createInvoice } from "../api/invoiceApi.js";
import { getMyCompany } from "../api/companyApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import InvoicePreview from "../components/InvoicePreview.jsx";
import TopNav from "../components/TopNav.jsx";
import "./InvoiceForm.css";

const emptyItem = () => ({
  id: crypto.randomUUID(),
  description: "",
  category: "",
  quantity: 1,
  rate: 0,
});

const today = () =>
  new Date().toLocaleDateString("en-GB", { day: "2-digit", month: "2-digit", year: "numeric" });

export default function InvoiceForm() {
  const navigate = useNavigate();
  const { session } = useAuth();

  const [company, setCompany] = useState(null);
  const [companyError, setCompanyError] = useState("");

  const [customer, setCustomer] = useState({ name: "", email: "", phone: "", address: "", gstin: "" });
  const [items, setItems] = useState([emptyItem()]);
  const [taxPercent, setTaxPercent] = useState(18);
  const [billingNote, setBillingNote] = useState("");
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");

  useEffect(() => {
    getMyCompany()
      .then(setCompany)
      .catch(() => setCompanyError("Couldn't load your company details."));
  }, []);

  const updateCustomer = (field, value) => setCustomer((c) => ({ ...c, [field]: value }));
  const updateItem = (id, field, value) =>
    setItems((list) => list.map((it) => (it.id === id ? { ...it, [field]: value } : it)));
  const addItem = () => setItems((list) => [...list, emptyItem()]);
  const removeItem = (id) =>
    setItems((list) => (list.length > 1 ? list.filter((it) => it.id !== id) : list));

  const validate = () => {
    const e = {};
    if (!customer.name.trim()) e.name = "Customer name is required";
    if (customer.email && !/^\S+@\S+\.\S+$/.test(customer.email)) e.email = "Enter a valid email";
    if (items.some((it) => !it.description.trim() || it.quantity <= 0 || it.rate <= 0))
      e.items = "Every line item needs a description, quantity, and rate";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError("");
    if (!validate()) return;

    setSubmitting(true);
    try {
      const invoice = await createInvoice({
        customer,
        items: items.map(({ id, ...rest }) => rest),
        taxPercent: Number(taxPercent),
        billingNote,
      });
      navigate("/invoices/mine", { state: { justSubmittedId: invoice.invoiceId } });
    } catch (err) {
      setServerError(
        err?.response?.data?.message || "Something went wrong submitting the invoice. Please try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="invoice-page">
      <TopNav />

      <form className="invoice-form-shell" onSubmit={handleSubmit}>
        <div className="form-intro">
          <h2>New invoice</h2>
          <p>
            Fill in the highlighted fields below - everything else is your company's fixed
            letterhead detail. Submitting sends it to an admin for approval; the invoice number
            is assigned once it's accepted.
          </p>
        </div>

        {companyError && <p className="field-error">{companyError}</p>}

        <InvoicePreview
          mode="edit"
          company={company}
          invoiceMeta={{ invoiceDate: today() }}
          customer={customer}
          onCustomerChange={updateCustomer}
          items={items}
          onItemChange={updateItem}
          onAddItem={addItem}
          onRemoveItem={removeItem}
          taxPercent={taxPercent}
          onTaxPercentChange={setTaxPercent}
          billingNote={billingNote}
          onBillingNoteChange={setBillingNote}
        />

        {errors.name && <p className="field-error">{errors.name}</p>}
        {errors.email && <p className="field-error">{errors.email}</p>}
        {errors.items && <p className="field-error">{errors.items}</p>}
        {serverError && <p className="field-error">{serverError}</p>}

        <button className="primary-button" type="submit" disabled={submitting || !company}>
          {submitting && <Loader2 size={16} className="spin" />}
          {submitting ? "Submitting..." : "Submit for approval"}
        </button>
      </form>
    </div>
  );
}
