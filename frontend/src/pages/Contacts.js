import { useState, useEffect, useCallback }
  from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axios';

function Contacts() {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Import states
  const [importing, setImporting] =
    useState(false);
  const [importResult, setImportResult] =
    useState(null);

  // Modal states
  const [showCreate, setShowCreate] =
    useState(false);
  const [showEdit, setShowEdit] =
    useState(false);
  const [showDelete, setShowDelete] =
    useState(false);
  const [selectedContact, setSelectedContact] =
    useState(null);

  // Form state
  const emptyForm = {
    firstName: '',
    lastName: '',
    title: '',
    emailAddresses: [
      { email: '', label: 'work' }],
    phoneNumbers: [
      { number: '', label: 'work' }]
  };
  const [formData, setFormData] =
    useState(emptyForm);
  const [formLoading, setFormLoading] =
    useState(false);
  const [formError, setFormError] =
    useState('');

  // ─── FETCH CONTACTS ───────────────────────
  const fetchContacts = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 10,
        ...(search && { search })
      };
      const response = await api.get(
        '/contacts', { params });
      setContacts(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError('Failed to load contacts');
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => {
    fetchContacts();
  }, [fetchContacts]);

  // ─── SEARCH ───────────────────────────────
  const handleSearch = (e) => {
    setSearch(e.target.value);
    setPage(0);
  };

  // ─── EXPORT ───────────────────────────────
  const handleExport = async () => {
    try {
      const response = await api.get(
        '/contacts/export',
        { responseType: 'blob' }
      );

      const url = window.URL
        .createObjectURL(
          new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute(
        'download', 'contacts.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

    } catch (err) {
      setError('Failed to export contacts');
    }
  };

  // ─── IMPORT ───────────────────────────────
  const handleImport = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setImporting(true);
    setImportResult(null);
    setError('');

    const data = new FormData();
    data.append('file', file);

    try {
      const response = await api.post(
        '/contacts/import', data, {
          headers: {
            'Content-Type':
              'multipart/form-data'
          }
        });
      setImportResult(response.data);
      fetchContacts();
    } catch (err) {
      setError(
        err.response?.data?.error ||
        'Failed to import contacts');
    } finally {
      setImporting(false);
      e.target.value = '';
    }
  };

  // ─── GET INITIALS ─────────────────────────
  const getInitials = (contact) => {
    return `${contact.firstName?.[0] || ''}
      ${contact.lastName?.[0] || ''}`
      .trim().toUpperCase();
  };

  // ─── OPEN MODALS ──────────────────────────
  const openCreate = () => {
    setFormData(emptyForm);
    setFormError('');
    setShowCreate(true);
  };

  const openEdit = (contact) => {
    setSelectedContact(contact);
    setFormData({
      firstName: contact.firstName || '',
      lastName: contact.lastName || '',
      title: contact.title || '',
      emailAddresses:
        contact.emailAddresses?.length > 0
          ? contact.emailAddresses
          : [{ email: '', label: 'work' }],
      phoneNumbers:
        contact.phoneNumbers?.length > 0
          ? contact.phoneNumbers
          : [{ number: '', label: 'work' }]
    });
    setFormError('');
    setShowEdit(true);
  };

  const openDelete = (contact) => {
    setSelectedContact(contact);
    setShowDelete(true);
  };

  // ─── FORM HANDLERS ────────────────────────
  const handleFormChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleEmailChange = (i, f, v) => {
    const updated = [...formData.emailAddresses];
    updated[i][f] = v;
    setFormData({
      ...formData, emailAddresses: updated });
  };

  const handlePhoneChange = (i, f, v) => {
    const updated = [...formData.phoneNumbers];
    updated[i][f] = v;
    setFormData({
      ...formData, phoneNumbers: updated });
  };

  const addEmail = () => setFormData({
    ...formData,
    emailAddresses: [
      ...formData.emailAddresses,
      { email: '', label: 'work' }]
  });

  const addPhone = () => setFormData({
    ...formData,
    phoneNumbers: [
      ...formData.phoneNumbers,
      { number: '', label: 'work' }]
  });

  const removeEmail = (i) => setFormData({
    ...formData,
    emailAddresses:
      formData.emailAddresses.filter(
        (_, idx) => idx !== i)
  });

  const removePhone = (i) => setFormData({
    ...formData,
    phoneNumbers:
      formData.phoneNumbers.filter(
        (_, idx) => idx !== i)
  });

  // ─── CRUD OPERATIONS ──────────────────────
  const handleCreate = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      await api.post('/contacts', formData);
      setShowCreate(false);
      fetchContacts();
    } catch (err) {
      setFormError(
        err.response?.data?.error ||
        'Failed to create contact');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      await api.put(
        `/contacts/${selectedContact.id}`,
        formData);
      setShowEdit(false);
      fetchContacts();
    } catch (err) {
      setFormError(
        err.response?.data?.error ||
        'Failed to update contact');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setFormLoading(true);
    try {
      await api.delete(
        `/contacts/${selectedContact.id}`);
      setShowDelete(false);
      fetchContacts();
    } catch (err) {
      setError('Failed to delete contact');
    } finally {
      setFormLoading(false);
    }
  };

  // ─── CONTACT FORM COMPONENT ───────────────
  const ContactForm = ({ onSubmit }) => (
    <form onSubmit={onSubmit}>
      {formError && (
        <div className="alert alert-danger">
          ⚠️ {formError}
        </div>
      )}

      <div className="row mb-3">
        <div className="col">
          <label className="form-label
            fw-semibold">
            First Name *
          </label>
          <input
            type="text"
            name="firstName"
            className="form-control"
            placeholder="First name"
            value={formData.firstName}
            onChange={handleFormChange}
            required
          />
        </div>
        <div className="col">
          <label className="form-label
            fw-semibold">
            Last Name
          </label>
          <input
            type="text"
            name="lastName"
            className="form-control"
            placeholder="Last name"
            value={formData.lastName}
            onChange={handleFormChange}
          />
        </div>
      </div>

      <div className="mb-3">
        <label className="form-label
          fw-semibold">Title</label>
        <input
          type="text"
          name="title"
          className="form-control"
          placeholder="e.g. Manager"
          value={formData.title}
          onChange={handleFormChange}
        />
      </div>

      {/* Emails */}
      <div className="mb-3">
        <label className="form-label
          fw-semibold">
          Email Addresses
        </label>
        {formData.emailAddresses.map(
          (em, i) => (
          <div key={i}
            className="d-flex gap-2 mb-2">
            <input
              type="email"
              className="form-control"
              placeholder="email@example.com"
              value={em.email}
              onChange={(e) =>
                handleEmailChange(
                  i, 'email', e.target.value)}
            />
            <select
              className="form-select"
              style={{width:'120px',
                flexShrink:0}}
              value={em.label}
              onChange={(e) =>
                handleEmailChange(
                  i, 'label', e.target.value)}>
              <option value="work">Work</option>
              <option value="personal">
                Personal
              </option>
              <option value="other">Other</option>
            </select>
            {formData.emailAddresses.length
              > 1 && (
              <button type="button"
                className="btn btn-outline-danger"
                onClick={() => removeEmail(i)}>
                ✕
              </button>
            )}
          </div>
        ))}
        <button type="button"
          className="btn btn-outline-secondary
            btn-sm"
          onClick={addEmail}>
          + Add Email
        </button>
      </div>

      {/* Phones */}
      <div className="mb-4">
        <label className="form-label
          fw-semibold">
          Phone Numbers
        </label>
        {formData.phoneNumbers.map((ph, i) => (
          <div key={i}
            className="d-flex gap-2 mb-2">
            <input
              type="text"
              className="form-control"
              placeholder="+923001234567"
              value={ph.number}
              onChange={(e) =>
                handlePhoneChange(
                  i, 'number', e.target.value)}
            />
            <select
              className="form-select"
              style={{width:'120px',
                flexShrink:0}}
              value={ph.label}
              onChange={(e) =>
                handlePhoneChange(
                  i, 'label', e.target.value)}>
              <option value="work">Work</option>
              <option value="home">Home</option>
              <option value="personal">
                Personal
              </option>
              <option value="other">Other</option>
            </select>
            {formData.phoneNumbers.length
              > 1 && (
              <button type="button"
                className="btn btn-outline-danger"
                onClick={() => removePhone(i)}>
                ✕
              </button>
            )}
          </div>
        ))}
        <button type="button"
          className="btn btn-outline-secondary
            btn-sm"
          onClick={addPhone}>
          + Add Phone
        </button>
      </div>

      <div className="d-flex gap-2
        justify-content-end">
        <button
          type="button"
          className="btn btn-outline-secondary"
          style={{borderRadius:'8px',
            padding:'10px 20px'}}
          onClick={() => {
            setShowCreate(false);
            setShowEdit(false);
          }}>
          Cancel
        </button>
        <button
          type="submit"
          disabled={formLoading}
          className="btn"
          style={{
            background:
              'linear-gradient(135deg, #667eea, #764ba2)',
            color:'white',
            borderRadius:'8px',
            padding:'10px 20px',
            border:'none',
            fontWeight:'600'
          }}>
          {formLoading ? (
            <>
              <span className="spinner-border
                spinner-border-sm me-2"/>
              Saving...
            </>
          ) : 'Save Contact'}
        </button>
      </div>
    </form>
  );

  // ─── RENDER ───────────────────────────────
  return (
    <div className="dashboard-container">
      <Navbar />

      <div className="content-area">

        {/* ── Header ── */}
        <div className="d-flex
          justify-content-between
          align-items-center mb-4
          flex-wrap gap-2">
          <h4 className="fw-bold mb-0">
            📋 My Contacts
          </h4>
          <div className="d-flex gap-2
            flex-wrap">

            {/* Export Button */}
            <button
              onClick={handleExport}
              className="btn"
              style={{
                background:'#28a745',
                color:'white',
                borderRadius:'8px',
                padding:'10px 16px',
                border:'none',
                fontWeight:'600'
              }}>
              📥 Export CSV
            </button>

            {/* Import Button */}
            <label
              className="btn mb-0"
              style={{
                background:'#17a2b8',
                color:'white',
                borderRadius:'8px',
                padding:'10px 16px',
                border:'none',
                fontWeight:'600',
                cursor:'pointer'
              }}>
              {importing ? (
                <>
                  <span className="
                    spinner-border
                    spinner-border-sm me-2"/>
                  Importing...
                </>
              ) : '📤 Import CSV'}
              <input
                type="file"
                accept=".csv"
                style={{display:'none'}}
                onChange={handleImport}
                disabled={importing}
              />
            </label>

            {/* Add Contact */}
            <button
              onClick={openCreate}
              className="btn-add">
              + Add Contact
            </button>
          </div>
        </div>

        {/* ── Import Result ── */}
        {importResult && (
          <div className="alert alert-success
            d-flex justify-content-between
            align-items-start">
            <div>
              ✅ Import complete!{' '}
              <strong>
                {importResult.imported}
              </strong> imported,{' '}
              <strong>
                {importResult.skipped}
              </strong> skipped.
              {importResult.errors?.length
                > 0 && (
                <ul className="mt-2 mb-0">
                  {importResult.errors
                    .map((e, i) => (
                    <li key={i}>
                      <small>{e}</small>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <button
              className="btn-close"
              onClick={() =>
                setImportResult(null)}/>
          </div>
        )}

        {/* ── Search ── */}
        <div className="search-bar">
          <input
            type="text"
            className="form-control"
            placeholder="🔍 Search by name..."
            value={search}
            onChange={handleSearch}
          />
        </div>

        {/* ── CSV Format Helper ── */}
        <div className="alert"
          style={{
            background:'#f8f9fa',
            border:'1px dashed #dee2e6',
            borderRadius:'8px',
            fontSize:'13px',
            color:'#666',
            marginBottom:'16px'
          }}>
          📄 <strong>CSV Format:</strong>{' '}
          First Name, Last Name, Title,
          Email 1, Email 1 Label, Email 2,
          Email 2 Label, Phone 1,
          Phone 1 Label, Phone 2,
          Phone 2 Label
        </div>

        {/* ── Error ── */}
        {error && (
          <div className="alert alert-danger">
            ⚠️ {error}
            <button
              className="btn-close float-end"
              onClick={() => setError('')}/>
          </div>
        )}

        {/* ── Loading ── */}
        {loading ? (
          <div className="text-center mt-5">
            <div className="spinner-border"
              style={{color:'#667eea'}}/>
            <p className="mt-2 text-muted">
              Loading contacts...
            </p>
          </div>

        ) : contacts.length === 0 ? (
          /* ── Empty State ── */
          <div className="text-center mt-5">
            <div style={{fontSize:'64px'}}>
              📭
            </div>
            <h5 className="text-muted mt-3">
              {search
                ? 'No contacts found'
                : 'No contacts yet'}
            </h5>
            <p className="text-muted">
              {search
                ? 'Try different search'
                : 'Click "+ Add Contact"'}
            </p>
          </div>

        ) : (
          /* ── Contact List ── */
          <>
            <p className="text-muted mb-3">
              <small>
                Showing {contacts.length} contact
                {contacts.length !== 1 ? 's' : ''}
              </small>
            </p>

            {contacts.map(contact => (
              <div key={contact.id}
                className="contact-card d-flex
                  align-items-center
                  justify-content-between">

                <div className="d-flex
                  align-items-center gap-3">
                  <div className="contact-avatar">
                    {getInitials(contact)}
                  </div>
                  <div>
                    <h6 className="mb-0 fw-bold">
                      {contact.firstName}{' '}
                      {contact.lastName}
                    </h6>
                    {contact.title && (
                      <small className="text-muted">
                        💼 {contact.title}
                      </small>
                    )}
                    {contact.emailAddresses
                      ?.[0] && (
                      <div>
                        <small style={{
                          color:'#667eea'}}>
                          📧{' '}
                          {contact
                            .emailAddresses[0]
                            .email}
                        </small>
                      </div>
                    )}
                    {contact.phoneNumbers
                      ?.[0] && (
                      <div>
                        <small className="
                          text-muted">
                          📱{' '}
                          {contact
                            .phoneNumbers[0]
                            .number}
                        </small>
                      </div>
                    )}
                  </div>
                </div>

                <div className="d-flex gap-2">
                  <button
                    onClick={() =>
                      openEdit(contact)}
                    className="btn btn-sm"
                    style={{
                      background:'#f0f0f0',
                      borderRadius:'8px',
                      border:'none',
                      padding:'8px 16px'
                    }}>
                    ✏️ Edit
                  </button>
                  <button
                    onClick={() =>
                      openDelete(contact)}
                    className="btn btn-sm
                      btn-outline-danger"
                    style={{
                      borderRadius:'8px'}}>
                    🗑️ Delete
                  </button>
                </div>
              </div>
            ))}

            {/* ── Pagination ── */}
            {totalPages > 1 && (
              <div className="
                pagination-container">
                <nav>
                  <ul className="pagination">
                    <li className={`page-item
                      ${page === 0 ?
                        'disabled':''}`}>
                      <button
                        className="page-link"
                        onClick={() =>
                          setPage(p => p-1)}>
                        ← Prev
                      </button>
                    </li>
                    {[...Array(totalPages)]
                      .map((_, i) => (
                      <li key={i}
                        className={`page-item
                          ${page === i ?
                            'active':''}`}>
                        <button
                          className="page-link"
                          onClick={() =>
                            setPage(i)}>
                          {i+1}
                        </button>
                      </li>
                    ))}
                    <li className={`page-item
                      ${page===totalPages-1 ?
                        'disabled':''}`}>
                      <button
                        className="page-link"
                        onClick={() =>
                          setPage(p => p+1)}>
                        Next →
                      </button>
                    </li>
                  </ul>
                </nav>
              </div>
            )}
          </>
        )}
      </div>

      {/* ── Create Modal ── */}
      {showCreate && (
        <div className="modal show d-block"
          style={{backgroundColor:
            'rgba(0,0,0,0.5)'}}>
          <div className="modal-dialog
            modal-dialog-centered modal-lg">
            <div className="modal-content"
              style={{borderRadius:'16px'}}>
              <div className="modal-header
                modal-header-custom">
                <h5 className="modal-title">
                  ➕ Create Contact
                </h5>
                <button
                  onClick={() =>
                    setShowCreate(false)}
                  className="btn-close
                    btn-close-white"/>
              </div>
              <div className="modal-body p-4">
                <ContactForm
                  onSubmit={handleCreate}/>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ── */}
      {showEdit && (
        <div className="modal show d-block"
          style={{backgroundColor:
            'rgba(0,0,0,0.5)'}}>
          <div className="modal-dialog
            modal-dialog-centered modal-lg">
            <div className="modal-content"
              style={{borderRadius:'16px'}}>
              <div className="modal-header
                modal-header-custom">
                <h5 className="modal-title">
                  ✏️ Edit Contact
                </h5>
                <button
                  onClick={() =>
                    setShowEdit(false)}
                  className="btn-close
                    btn-close-white"/>
              </div>
              <div className="modal-body p-4">
                <ContactForm
                  onSubmit={handleUpdate}/>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── Delete Modal ── */}
      {showDelete && (
        <div className="modal show d-block"
          style={{backgroundColor:
            'rgba(0,0,0,0.5)'}}>
          <div className="modal-dialog
            modal-dialog-centered">
            <div className="modal-content"
              style={{borderRadius:'16px'}}>
              <div className="modal-header"
                style={{
                  background:'#dc3545',
                  color:'white',
                  borderRadius:
                    '16px 16px 0 0'
                }}>
                <h5 className="modal-title">
                  🗑️ Delete Contact
                </h5>
                <button
                  onClick={() =>
                    setShowDelete(false)}
                  className="btn-close
                    btn-close-white"/>
              </div>
              <div className="modal-body
                p-4 text-center">
                <div style={{fontSize:'48px'}}>
                  ⚠️
                </div>
                <h5 className="mt-3">
                  Are you sure?
                </h5>
                <p className="text-muted">
                  Delete{' '}
                  <strong>
                    {selectedContact?.firstName}
                    {' '}
                    {selectedContact?.lastName}
                  </strong>?
                  This cannot be undone!
                </p>
                <div className="d-flex gap-2
                  justify-content-center mt-4">
                  <button
                    onClick={() =>
                      setShowDelete(false)}
                    className="btn
                      btn-outline-secondary"
                    style={{
                      borderRadius:'8px',
                      padding:'10px 24px'
                    }}>
                    Cancel
                  </button>
                  <button
                    onClick={handleDelete}
                    disabled={formLoading}
                    className="btn btn-danger"
                    style={{
                      borderRadius:'8px',
                      padding:'10px 24px',
                      fontWeight:'600'
                    }}>
                    {formLoading ? (
                      <>
                        <span className="
                          spinner-border
                          spinner-border-sm
                          me-2"/>
                        Deleting...
                      </>
                    ) : 'Yes, Delete'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Contacts;