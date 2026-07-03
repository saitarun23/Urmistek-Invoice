import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Trash2, Loader2 } from "lucide-react";
import { createInvoice, verifyPayment } from "../api/invoiceApi.js";
import { loadRazorpayScript } from "../utils/loadRazorpay.js";

const emptyItem = () => ({
  id: crypto.randomUUID(),
  description: "",
  category: "",
  quantity: 1,
  rate: 0,
});

export default function InvoiceForm() {
  const navigate = useNavigate();

  const [customer, setCustomer] = useState({
    name: "",
    email: "",
    phone: "",
    address: "",
    gstin: "",
  });
  const [items, setItems] = useState([emptyItem()]);
  const [taxPercent, setTaxPercent] = useState(18);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");

  const updateCustomer = (field, value) =>
    setCustomer((c) => ({ ...c, [field]: value }));

  const updateItem = (id, field, value) =>
    setItems((list) =>
      list.map((it) => (it.id === id ? { ...it, [field]: value } : it))
    );

  const addItem = () => setItems((list) => [...list, emptyItem()]);
  const removeItem = (id) =>
    setItems((list) => (list.length > 1 ? list.filter((it) => it.id !== id) : list));

  const subtotal = items.reduce(
    (sum, it) => sum + (Number(it.quantity) || 0) * (Number(it.rate) || 0),
    0
  );
  const tax = subtotal * (Number(taxPercent) / 100);
  const total = subtotal + tax;

  const validate = () => {
    const e = {};
    if (!customer.name.trim()) e.name = "Required";
    if (!/^\S+@\S+\.\S+$/.test(customer.email)) e.email = "Enter a valid email";
    if (!customer.phone.trim()) e.phone = "Required";
    if (items.some((it) => !it.description.trim() || it.quantity <= 0 || it.rate <= 0))
      e.items = "Every line item needs a description, quantity, and rate";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    setServerError("");
    if (!validate()) return;

    setSubmitting(true);
    try {
      // Step 1: create the order on the backend, get back a Razorpay order id
      const order = await createInvoice({
        customer,
        items: items.map(({ id, ...rest }) => rest),
        taxPercent: Number(taxPercent),
      });

      // Step 2: load Razorpay and open the checkout popup
      await loadRazorpayScript();

      const rzp = new window.Razorpay({
        key: order.razorpayKeyId,
        amount: Math.round(order.amount * 100), // paise
        currency: order.currency,
        name: "Your Company Name",
        description: `Invoice ${order.invoiceNumber}`,
        order_id: order.razorpayOrderId,
        prefill: {
          name: customer.name,
          email: customer.email,
          contact: customer.phone,
        },
        handler: async (response) => {
          // Step 3: hand the payment result to the backend for signature verification
          try {
            await verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            navigate(
              `/payment-success?invoiceId=${order.invoiceId}&invoiceNumber=${order.invoiceNumber}&email=${encodeURIComponent(customer.email)}`
            );
          } catch (err) {
            navigate("/payment-failed");
          }
        },
        modal: {
          ondismiss: () => setSubmitting(false),
        },
        theme: { color: "#2563eb" },
      });

      rzp.on("payment.failed", () => navigate("/payment-failed"));
      rzp.open();
    } catch (err) {
      setServerError(
        err?.response?.data?.message || "Something went wrong creating your order. Please try again."
      );
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-6 text-neutral-800">
      <div>
        <h2 className="text-lg font-medium">Billing details</h2>
        <div className="grid grid-cols-2 gap-3 mt-3">
          <Field label="Full name" error={errors.name}>
            <input
              className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full"
              value={customer.name}
              onChange={(e) => updateCustomer("name", e.target.value)}
            />
          </Field>
          <Field label="Email" error={errors.email}>
            <input
              className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full"
              type="email"
              value={customer.email}
              onChange={(e) => updateCustomer("email", e.target.value)}
            />
          </Field>
          <Field label="Phone" error={errors.phone}>
            <input
              className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full"
              value={customer.phone}
              onChange={(e) => updateCustomer("phone", e.target.value)}
            />
          </Field>
          <Field label="GSTIN (optional)">
            <input
              className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full"
              value={customer.gstin}
              onChange={(e) => updateCustomer("gstin", e.target.value)}
            />
          </Field>
          <Field label="Billing address" full>
            <textarea
              className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full"
              rows={2}
              value={customer.address}
              onChange={(e) => updateCustomer("address", e.target.value)}
            />
          </Field>
        </div>
      </div>

      <div>
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-medium">Items / services</h2>
          <button
            onClick={addItem}
            className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
          >
            <Plus size={16} /> Add item
          </button>
        </div>
        {errors.items && <p className="text-red-600 text-sm mt-1">{errors.items}</p>}
        <div className="mt-3 space-y-2">
          {items.map((it) => (
            <div key={it.id} className="grid grid-cols-12 gap-2 items-center">
              <input
                className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full col-span-5"
                placeholder="Description"
                value={it.description}
                onChange={(e) => updateItem(it.id, "description", e.target.value)}
              />
              <input
                className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full col-span-2"
                placeholder="Category"
                value={it.category}
                onChange={(e) => updateItem(it.id, "category", e.target.value)}
              />
              <input
                className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full col-span-2"
                type="number"
                min={1}
                placeholder="Qty"
                value={it.quantity}
                onChange={(e) => updateItem(it.id, "quantity", e.target.value)}
              />
              <input
                className="border border-neutral-300 rounded-md px-3 py-2 text-sm w-full col-span-2"
                type="number"
                min={0}
                placeholder="Rate"
                value={it.rate}
                onChange={(e) => updateItem(it.id, "rate", e.target.value)}
              />
              <button
                onClick={() => removeItem(it.id)}
                className="col-span-1 text-neutral-400 hover:text-red-600"
              >
                <Trash2 size={16} />
              </button>
            </div>
          ))}
        </div>
      </div>

      <div className="flex justify-end">
        <div className="w-64 space-y-1 text-sm">
          <div className="flex justify-between">
            <span>Subtotal</span>
            <span>₹{subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="flex items-center gap-1">
              Tax
              <input
                type="number"
                className="w-14 border border-neutral-300 rounded px-1 py-0.5"
                value={taxPercent}
                onChange={(e) => setTaxPercent(e.target.value)}
              />
              %
            </span>
            <span>₹{tax.toFixed(2)}</span>
          </div>
          <div className="flex justify-between font-medium text-base border-t pt-1">
            <span>Total</span>
            <span>₹{total.toFixed(2)}</span>
          </div>
        </div>
      </div>

      {serverError && <p className="text-red-600 text-sm">{serverError}</p>}

      <button
        onClick={handleSubmit}
        disabled={submitting}
        className="w-full bg-blue-600 text-white rounded-md py-2.5 font-medium hover:bg-blue-700 disabled:opacity-60 flex items-center justify-center gap-2"
      >
        {submitting && <Loader2 size={16} className="animate-spin" />}
        {submitting ? "Processing..." : "Continue to payment"}
      </button>
    </div>
  );
}

function Field({ label, error, full, children }) {
  return (
    <label className={`text-sm text-neutral-600 ${full ? "col-span-2" : ""}`}>
      {label}
      {children}
      {error && <span className="block text-red-600 text-xs mt-0.5">{error}</span>}
    </label>
  );
}
