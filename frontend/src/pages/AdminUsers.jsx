import { useEffect, useRef, useState } from "react";
import { Loader2, Plus, UploadCloud } from "lucide-react";
import { listUsers, createUser, uploadMySignature } from "../api/usersApi.js";
import TopNav from "../components/TopNav.jsx";
import "./AdminUsers.css";

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [form, setForm] = useState({ username: "", password: "", displayName: "", role: "STAFF" });
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState("");
  const fileInputRef = useRef(null);

  const load = () => {
    setLoading(true);
    listUsers()
      .then(setUsers)
      .catch(() => setError("Couldn't load users."))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreateError("");
    setCreating(true);
    try {
      await createUser(form);
      setForm({ username: "", password: "", displayName: "", role: "STAFF" });
      load();
    } catch (err) {
      setCreateError(err?.response?.data?.message || "Couldn't create that user.");
    } finally {
      setCreating(false);
    }
  };

  const handleSignatureUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setUploadMessage("");
    try {
      await uploadMySignature(file);
      setUploadMessage("Signature saved - it'll appear on invoices you approve from now on.");
      load();
    } catch {
      setUploadMessage("Couldn't upload that file.");
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  return (
    <div className="invoice-page">
      <TopNav />
      <h2>Users</h2>
      <p className="list-sub">Everyone who can log into this company, and your own signature.</p>

      <div className="users-panel">
        <h3>Your signature</h3>
        <p className="users-panel-sub">
          Uploaded once, it's stamped on every invoice you approve from now on - never shown on
          pending or rejected invoices.
        </p>
        <label className="upload-button">
          <UploadCloud size={15} />
          {uploading ? "Uploading..." : "Upload signature image (PNG)"}
          <input ref={fileInputRef} type="file" accept="image/png,image/jpeg" hidden onChange={handleSignatureUpload} />
        </label>
        {uploadMessage && <p className="users-panel-note">{uploadMessage}</p>}
      </div>

      <div className="users-panel">
        <h3>Add a user</h3>
        <form className="add-user-form" onSubmit={handleCreate}>
          <input
            placeholder="Username"
            value={form.username}
            onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))}
            required
          />
          <input
            placeholder="Display name"
            value={form.displayName}
            onChange={(e) => setForm((f) => ({ ...f, displayName: e.target.value }))}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
            required
          />
          <select value={form.role} onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))}>
            <option value="STAFF">Staff</option>
            <option value="ADMIN">Admin</option>
          </select>
          <button className="primary-button add-user-submit" type="submit" disabled={creating}>
            {creating ? <Loader2 size={15} className="spin" /> : <Plus size={15} />}
            Add user
          </button>
        </form>
        {createError && <p className="field-error">{createError}</p>}
      </div>

      {loading && (
        <div className="list-loading">
          <Loader2 className="spin" size={18} /> Loading...
        </div>
      )}
      {error && <p className="field-error">{error}</p>}

      <table className="users-table">
        <thead>
          <tr>
            <th>Username</th>
            <th>Display name</th>
            <th>Role</th>
            <th>Signature</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.username}</td>
              <td>{u.displayName}</td>
              <td>{u.role}</td>
              <td>{u.hasSignature ? "Uploaded" : "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
