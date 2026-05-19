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

  // Fetch contacts
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

  // Search with debounce
  const handleSearch = (e) => {
    setSearch(e.target.value);
    setPage(0);
  };

  // Get initials for avatar
  const getInitials = (contact) => {
    return `${contact.firstName?.[0] || ''}
      ${contact.lastName?.[0] || ''}`
      .trim().toUpperCase();
  };

  // Open Create Modal
  const openCreate = () => {
    setFormData(emptyForm);
    setFormError('');
    setShowCreate(true);
  };

  // Open Edit Modal
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

  // Open Delete Modal
  const openDelete = (contact) => {
    setSelectedContact(contact);
    setShowDelete(true);
  };

  // Handle form field change
  const handleFormChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  // Handle email change
  const handleEmailChange = (index, field,
      value) => {
    const updated = [...formData.emailAddresses];
    updated[index][field] = value;
    setFormData({
      ...formData,
      emailAddresses: updated
    });
  };

  // Handle phone change
  const handlePhoneChange = (index, field,
      value) => {
    const updated = [...formData.phoneNumbers];
    updated[index][field] = value;
    setFormData({
      ...formData,
      phoneNumbers: updated
    });
  };

  // Add email field
  const addEmail = () => {
    setFormData({
      ...formData,
      emailAddresses: [
        ...formData.emailAddresses,
        { email: '', label: 'work' }
      ]
    });
  };

  // Add phone field
  const addPhone = () => {
    setFormData({
      ...formData,
      phoneNumbers: [
        ...formData.phoneNumbers,
        { number: '', label: 'work' }
      ]
    });
  };

  // Remove email field
  const removeEmail = (index) => {
    setFormData({
      ...formData,
      emailAddresses:
        formData.emailAddresses.filter(
          (_, i) => i !== index)
    });
  };

  // Remove phone field
  const removePhone = (index) => {
    setFormData({
      ...formData,
      phoneNumbers:
        formData.phoneNumbers.filter(
          (_, i) => i !== index)
    });
  };

  // Create Contact
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

  // Update Contact
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

  // Delete Contact
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

  // Reusable Contact Form
  const ContactForm = ({ onSubmit }) => (
    <form onSubmit={onSubmit}>
      {formError && (
        <div className="alert alert-danger">
          ⚠️ {formError}
        </div>
      )}

      {/* Name Row */}
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

      {/* Title */}
      <div className="mb-3">
        <label className="form-label
          fw-semibold">
          Title
        </label>
        <input
          type="text"
          name="title"
          className="form-control"
          placeholder="e.g. Manager, Developer"
          value={formData.title}
          onChange={handleFormChange}
        />
      </div>

      {/* Email Addresses */}
      <div className="mb-3">
        <label className="form-label
          fw-semibold">
          Email Addresses
        </label>
        {formData.emailAddresses.map(
          (email, index) => (
          <div key={index}
            className="d-flex gap-2 mb-2">
            <input
              type="email"
              className="form-control"
              placeholder="email@example.com"
              value={email.email}
              onChange={(e) =>
                handleEmailChange(
                  index, 'email',
                  e.target.value)}
            />
            <select
              className="form-select"
              style={{width: '120px',
                flexShrink: 0}}
              value={email.label}
              onChange={(e) =>
                handleEmailChange(
                  index, 'label',
                  e.target.value)}>
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
                onClick={() =>
                  removeEmail(index)}>
                ✕
              </button>
            )}
          </div>
        ))}
        <button
          type="button"
          className="btn btn-outline-secondary
            btn-sm"
          onClick={addEmail}>
          + Add Email
        </button>
      </div>

      {/* Phone Numbers */}
      <div className="mb-4">
        <label className="form-label
          fw-semibold">
          Phone Numbers
        </label>
        {formData.phoneNumbers.map(
          (phone, index) => (
          <div key={index}
            className="d-flex gap-2 mb-2">
            <input
              type="text"
              className="form-control"
              placeholder="+923001234567"
              value={phone.number}
              onChange={(e) =>
                handlePhoneChange(
                  index, 'number',
                  e.target.value)}
            />
            <select
              className="form-select"
              style={{width: '120px',
                flexShrink: 0}}
              value={phone.label}
              onChange={(e) =>
                handlePhoneChange(
                  index, 'label',
                  e.target.value)}>
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
                onClick={() =>
                  removePhone(index)}>
                ✕
              </button>
            )}
          </div>
        ))}
        <button
          type="button"
          className="btn btn-outline-secondary
            btn-sm"
          onClick={addPhone}>
          + Add Phone
        </button>
      </div>

      {/* Form Buttons */}
      <div className="d-flex gap-2
        justify-content-end">
        <button
          type="button"
          className="btn btn-outline-secondary"
          style={{borderRadius: '8px',
            padding: '10px 20px'}}
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
            color: 'white',
            borderRadius: '8px',
            padding: '10px 20px',
            border: 'none',
            fontWeight: '600'
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

  return (
    <div className="dashboard-container">
      <Navbar />

      <div className="content-area">

        {/* Header */}
        <div className="d-flex
          justify-content-between
          align-items-center mb-4">
          <h4 className="fw-bold mb-0">
            📋 My Contacts
          </h4>
          <button
            onClick={openCreate}
            className="btn-add">
            + Add Contact
          </button>
        </div>

        {/* Search Bar */}
        <div className="search-bar">
          <input
            type="text"
            className="form-control"
            placeholder="🔍 Search by name..."
            value={search}
            onChange={handleSearch}
          />
        </div>

        {/* Error */}
        {error && (
          <div className="alert alert-danger">
            ⚠️ {error}
          </div>
        )}

        {/* Loading */}
        {loading ? (
          <div className="text-center mt-5">
            <div className="spinner-border"
              style={{color: '#667eea'}}/>
            <p className="mt-2 text-muted">
              Loading contacts...
            </p>
          </div>
        ) : contacts.length === 0 ? (
          /* Empty State */
          <div className="text-center mt-5">
            <div style={{fontSize: '64px'}}>
              📭
            </div>
            <h5 className="text-muted mt-3">
              {search
                ? 'No contacts found'
                : 'No contacts yet'}
            </h5>
            <p className="text-muted">
              {search
                ? 'Try a different search term'
                : 'Click "+ Add Contact" to start'}
            </p>
          </div>
        ) : (
          /* Contact List */
          <>
            {contacts.map(contact => (
              <div key={contact.id}
                className="contact-card d-flex
                  align-items-center
                  justify-content-between">

                <div className="d-flex
                  align-items-center gap-3">
                  {/* Avatar */}
                  <div className="contact-avatar">
                    {getInitials(contact)}
                  </div>

                  {/* Info */}
                  <div>
                    <h6 className="mb-0
                      fw-bold">
                      {contact.firstName}{' '}
                      {contact.lastName}
                    </h6>
                    {contact.title && (
                      <small className="
                        text-muted">
                        {contact.title}
                      </small>
                    )}
                    {contact.emailAddresses
                      ?.[0] && (
                      <div>
                        <small style={{
                          color: '#667eea'
                        }}>
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

                {/* Action Buttons */}
                <div className="d-flex gap-2">
                  <button
                    onClick={() =>
                      openEdit(contact)}
                    className="btn btn-sm"
                    style={{
                      background: '#f0f0f0',
                      borderRadius: '8px',
                      border: 'none',
                      padding: '8px 16px'
                    }}>
                    ✏️ Edit
                  </button>
                  <button
                    onClick={() =>
                      openDelete(contact)}
                    className="btn btn-sm
                      btn-outline-danger"
                    style={{
                      borderRadius: '8px'
                    }}>
                    🗑️ Delete
                  </button>
                </div>
              </div>
            ))}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="
                pagination-container">
                <nav>
                  <ul className="pagination">
                    <li className={`page-item
                      ${page === 0 ?
                        'disabled' : ''}`}>
                      <button
                        className="page-link"
                        onClick={() =>
                          setPage(p => p - 1)}>
                        ← Prev
                      </button>
                    </li>
                    {[...Array(totalPages)]
                      .map((_, i) => (
                      <li key={i}
                        className={`page-item
                          ${page === i ?
                            'active' : ''}`}>
                        <button
                          className="page-link"
                          onClick={() =>
                            setPage(i)}>
                          {i + 1}
                        </button>
                      </li>
                    ))}
                    <li className={`page-item
                      ${page === totalPages - 1
                        ? 'disabled' : ''}`}>
                      <button
                        className="page-link"
                        onClick={() =>
                          setPage(p => p + 1)}>
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

      {/* Create Modal */}
      {showCreate && (
        <div className="modal show d-block"
          style={{backgroundColor:
            'rgba(0,0,0,0.5)'}}>
          <div className="modal-dialog
            modal-dialog-centered
            modal-lg">
            <div className="modal-content"
              style={{borderRadius: '16px'}}>
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

      {/* Edit Modal */}
      {showEdit && (
        <div className="modal show d-block"
          style={{backgroundColor:
            'rgba(0,0,0,0.5)'}}>
          <div className="modal-dialog
            modal-dialog-centered
            modal-lg">
            <div className="modal-content"
              style={{borderRadius: '16px'}}>
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

      {/* Delete Modal */}
      {showDelete && (
        <div className="modal show d-block"
          style={{backgroundColor:
            'rgba(0,0,0,0.5)'}}>
          <div className="modal-dialog
            modal-dialog-centered">
            <div className="modal-content"
              style={{borderRadius: '16px'}}>
              <div className="modal-header"
                style={{
                  background: '#dc3545',
                  color: 'white',
                  borderRadius: '16px 16px 0 0'
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
              <div className="modal-body p-4
                text-center">
                <div style={{fontSize: '48px'}}>
                  ⚠️
                </div>
                <h5 className="mt-3">
                  Are you sure?
                </h5>
                <p className="text-muted">
                  You are about to delete{' '}
                  <strong>
                    {selectedContact?.firstName}
                    {' '}
                    {selectedContact?.lastName}
                  </strong>
                  . This cannot be undone!
                </p>
                <div className="d-flex gap-2
                  justify-content-center mt-4">
                  <button
                    onClick={() =>
                      setShowDelete(false)}
                    className="btn
                      btn-outline-secondary"
                    style={{
                      borderRadius: '8px',
                      padding: '10px 24px'
                    }}>
                    Cancel
                  </button>
                  <button
                    onClick={handleDelete}
                    disabled={formLoading}
                    className="btn btn-danger"
                    style={{
                      borderRadius: '8px',
                      padding: '10px 24px',
                      fontWeight: '600'
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