import { Link } from "react-router-dom";
import { XCircle } from "lucide-react";

export default function PaymentFailed() {
  return (
    <div className="max-w-md mx-auto mt-20 p-6 text-center space-y-4">
      <XCircle className="mx-auto text-red-600" size={48} />
      <h1 className="text-xl font-medium">Payment failed</h1>
      <p className="text-neutral-600 text-sm">
        Your payment didn't go through, and no amount has been charged.
        Please try again.
      </p>
      <Link
        to="/"
        className="inline-block bg-blue-600 text-white rounded-md px-4 py-2.5 font-medium hover:bg-blue-700"
      >
        Try again
      </Link>
    </div>
  );
}
