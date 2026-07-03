import { useSearchParams, Link } from "react-router-dom";
import { CheckCircle2, Download } from "lucide-react";
import { invoiceDownloadUrl } from "../api/invoiceApi.js";

export default function PaymentSuccess() {
  const [params] = useSearchParams();
  const invoiceId = params.get("invoiceId");
  const invoiceNumber = params.get("invoiceNumber");
  const email = params.get("email");

  return (
    <div className="max-w-md mx-auto mt-20 p-6 text-center space-y-4">
      <CheckCircle2 className="mx-auto text-green-600" size={48} />
      <h1 className="text-xl font-medium">Payment successful</h1>
      <p className="text-neutral-600 text-sm">
        Invoice <span className="font-medium">{invoiceNumber}</span> has been
        generated and emailed to you. You can also download it below.
      </p>

      <a
        href={invoiceDownloadUrl(invoiceId, email)}
        className="inline-flex items-center gap-2 bg-blue-600 text-white rounded-md px-4 py-2.5 font-medium hover:bg-blue-700"
      >
        <Download size={16} /> Download invoice
      </a>

      <div>
        <Link to="/" className="text-sm text-neutral-500 hover:text-neutral-700 underline">
          Back to home
        </Link>
      </div>
    </div>
  );
}
