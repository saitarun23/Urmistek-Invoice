import "./StatusBadge.css";

const LABELS = {
  PENDING_APPROVAL: "Pending approval",
  APPROVED: "Approved",
  REJECTED: "Rejected",
};

const CLASSES = {
  PENDING_APPROVAL: "status-badge status-pending",
  APPROVED: "status-badge status-approved",
  REJECTED: "status-badge status-rejected",
};

export default function StatusBadge({ status }) {
  return <span className={CLASSES[status] || "status-badge"}>{LABELS[status] || status}</span>;
}
