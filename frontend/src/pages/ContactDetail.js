import { useState, useEffect } from 'react';
import { useParams, useNavigate } 
  from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axios';

function ContactDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [contact, setContact] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchContact();
  }, [id]);

  const fetchContact = async () => {
    try {
      const response = await
        api.get(`/contacts/${id}`);
      setContact(response.data);
    } catch (err) {
      setError('Contact not found');
    } finally {
      setLoading(false);
    }
  };

  const getInitials = () => {
    if (!contact) return '?';
    return `${contact.firstName?.[0] || ''}
      ${contact.lastName?.[0] || ''}`
      .trim().toUpperCase();
  };

  if (loading) return (
    <div className="dashboard-container">
      <Navbar />
      <div className="text-center mt-5">
        <div className="spinner-border"
          style={{color: '#667eea'}}/>
      </div>
    </div>
  );

  if (error) return (
    <div className="dashboard-container">
      <Navbar />
      <div className="content-area">
        <div className="alert alert-danger">
          ⚠️ {error}
        </div>
        <button
          onClick={() => navigate('/contacts')}
          className="btn btn-outline-secondary">
          ← Back to Contacts
        </button>
      </div>
    </div>
  );

  return (
    <div className="dashboard-container">
      <Navbar />
      <div className="content-area">

        {/* Back Button */}
        <button
          onClick={() => navigate('/contacts')}
          className="btn btn-outline-secondary
            mb-4"
          style={{borderRadius: '8px'}}>
          ← Back to Contacts
        </button>

        <div className="row
          justify-content-center">
          <div className="col-md-7">
            <div className="profile-card">

              {/* Avatar */}
              <div className="profile-avatar">
                {getInitials()}
              </div>

              {/* Name & Title */}
              <h3 style={{
                textAlign: 'center',
                fontWeight: '700',
                marginBottom: '4px'
              }}>
                {contact.firstName}{' '}
                {contact.lastName}
              </h3>

              {contact.title && (
                <p style={{
                  textAlign: 'center',
                  color: '#667eea',
                  fontWeight: '600',
                  marginBottom: '24px'
                }}>
                  💼 {contact.title}
                </p>
              )}

              {/* Email Addresses */}
              {contact.emailAddresses?.length
                > 0 && (
                <div style={{
                  background: '#f8f9fa',
                  borderRadius: '12px',
                  padding: '20px',
                  marginBottom: '16px'
                }}>
                  <h6 style={{
                    fontWeight: '700',
                    marginBottom: '12px',
                    color: '#333'
                  }}>
                    📧 Email Addresses
                  </h6>
                  {contact.emailAddresses
                    .map((email, i) => (
                    <div key={i}
                      className="d-flex
                        justify-content-between
                        align-items-center
                        mb-2">
                      <span style={{
                        fontWeight: '500'
                      }}>
                        {email.email}
                      </span>
                      <span className="badge"
                        style={{
                          background:
                            'linear-gradient(135deg, #667eea, #764ba2)',
                          padding: '6px 12px',
                          borderRadius: '20px',
                          fontSize: '12px'
                        }}>
                        {email.label}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {/* Phone Numbers */}
              {contact.phoneNumbers?.length
                > 0 && (
                <div style={{
                  background: '#f8f9fa',
                  borderRadius: '12px',
                  padding: '20px',
                  marginBottom: '16px'
                }}>
                  <h6 style={{
                    fontWeight: '700',
                    marginBottom: '12px',
                    color: '#333'
                  }}>
                    📱 Phone Numbers
                  </h6>
                  {contact.phoneNumbers
                    .map((phone, i) => (
                    <div key={i}
                      className="d-flex
                        justify-content-between
                        align-items-center
                        mb-2">
                      <span style={{
                        fontWeight: '500'
                      }}>
                        {phone.number}
                      </span>
                      <span className="badge"
                        style={{
                          background:
                            'linear-gradient(135deg, #667eea, #764ba2)',
                          padding: '6px 12px',
                          borderRadius: '20px',
                          fontSize: '12px'
                        }}>
                        {phone.label}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {/* Action Buttons */}
              <div className="d-flex gap-2
                mt-3">
                <button
                  onClick={() =>
                    navigate('/contacts')}
                  className="btn btn-outline-secondary flex-fill"
                  style={{
                    borderRadius: '8px',
                    padding: '12px'
                  }}>
                  ← Back
                </button>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ContactDetail;