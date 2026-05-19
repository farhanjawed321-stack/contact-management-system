import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import api from '../api/axios';

function Profile() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Change Password Modal
  const [showModal, setShowModal] = useState(false);
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [pwError, setPwError] = useState('');
  const [pwLoading, setPwLoading] = useState(false);

  // Load profile on mount
  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get(
        '/auth/profile');
      setProfile(response.data);
    } catch (err) {
      setError('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = (e) => {
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value
    });
    setPwError('');
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPwError('');

    if (passwordData.newPassword !==
        passwordData.confirmPassword) {
      setPwError('New passwords do not match');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setPwError(
        'Password must be at least 6 characters');
      return;
    }

    setPwLoading(true);
    try {
      await api.post('/auth/change-password', {
        currentPassword:
          passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      setSuccess('Password changed successfully!');
      setShowModal(false);
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
    } catch (err) {
      setPwError(
        err.response?.data?.error ||
        'Failed to change password'
      );
    } finally {
      setPwLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Get initials for avatar
  const getInitials = () => {
    if (!profile) return '?';
    return `${profile.firstName?.[0] || ''}
      ${profile.lastName?.[0] || ''}`.trim()
      .toUpperCase();
  };

  if (loading) return (
    <div className="dashboard-container">
      <Navbar />
      <div className="content-area
        text-center mt-5">
        <div className="spinner-border"
          style={{color: '#667eea'}} />
      </div>
    </div>
  );

  return (
    <div className="dashboard-container">
      <Navbar />

      <div className="content-area">
        <h4 className="mb-4 fw-bold">
          👤 My Profile
        </h4>

        {/* Success Alert */}
        {success && (
          <div className="alert alert-success
            alert-custom">
            ✅ {success}
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger
            alert-custom">
            ⚠️ {error}
          </div>
        )}

        <div className="row justify-content-center">
          <div className="col-md-6">
            <div className="profile-card">

              {/* Avatar */}
              <div className="profile-avatar">
                {getInitials()}
              </div>

              {/* Name */}
              <h4 style={{
                textAlign: 'center',
                fontWeight: '700',
                marginBottom: '4px'
              }}>
                {profile?.firstName}{' '}
                {profile?.lastName}
              </h4>
              <p style={{
                textAlign: 'center',
                color: '#666',
                marginBottom: '24px'
              }}>
                Contact Manager User
              </p>

              {/* Info */}
              <div style={{
                background: '#f8f9fa',
                borderRadius: '12px',
                padding: '20px',
                marginBottom: '24px'
              }}>
                {profile?.email && (
                  <div className="d-flex
                    align-items-center mb-3">
                    <span style={{
                      fontSize: '20px',
                      marginRight: '12px'
                    }}>
                      📧
                    </span>
                    <div>
                      <div style={{
                        fontSize: '12px',
                        color: '#999'
                      }}>
                        Email
                      </div>
                      <div style={{
                        fontWeight: '600'
                      }}>
                        {profile.email}
                      </div>
                    </div>
                  </div>
                )}

                {profile?.phone && (
                  <div className="d-flex
                    align-items-center">
                    <span style={{
                      fontSize: '20px',
                      marginRight: '12px'
                    }}>
                      📱
                    </span>
                    <div>
                      <div style={{
                        fontSize: '12px',
                        color: '#999'
                      }}>
                        Phone
                      </div>
                      <div style={{
                        fontWeight: '600'
                      }}>
                        {profile.phone}
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Buttons */}
              <div className="d-grid gap-2">
                <button
                  onClick={() =>
                    setShowModal(true)}
                  className="btn"
                  style={{
                    background: 'linear-gradient(135deg, #667eea, #764ba2)',
                    color: 'white',
                    borderRadius: '8px',
                    padding: '12px',
                    fontWeight: '600',
                    border: 'none'
                  }}>
                  🔒 Change Password
                </button>

                <button
                  onClick={handleLogout}
                  className="btn btn-outline-danger"
                  style={{
                    borderRadius: '8px',
                    padding: '12px',
                    fontWeight: '600'
                  }}>
                  🚪 Logout
                </button>
              </div>

            </div>
          </div>
        </div>
      </div>

      {/* Change Password Modal */}
      {showModal && (
        <div className="modal show d-block"
          style={{
            backgroundColor:
              'rgba(0,0,0,0.5)'
          }}>
          <div className="modal-dialog
            modal-dialog-centered">
            <div className="modal-content"
              style={{borderRadius: '16px'}}>

              {/* Modal Header */}
              <div className="modal-header
                modal-header-custom">
                <h5 className="modal-title">
                  🔒 Change Password
                </h5>
                <button
                  onClick={() => {
                    setShowModal(false);
                    setPwError('');
                  }}
                  className="btn-close
                    btn-close-white"/>
              </div>

              {/* Modal Body */}
              <div className="modal-body p-4">
                {pwError && (
                  <div className="alert
                    alert-danger">
                    ⚠️ {pwError}
                  </div>
                )}

                <form
                  onSubmit={
                    handleChangePassword}>

                  <div className="mb-3">
                    <label className="
                      form-label fw-semibold">
                      Current Password
                    </label>
                    <input
                      type="password"
                      name="currentPassword"
                      className="form-control"
                      placeholder="Current password"
                      value={
                        passwordData
                          .currentPassword}
                      onChange={
                        handlePasswordChange}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="
                      form-label fw-semibold">
                      New Password
                    </label>
                    <input
                      type="password"
                      name="newPassword"
                      className="form-control"
                      placeholder="Min 6 characters"
                      value={
                        passwordData.newPassword}
                      onChange={
                        handlePasswordChange}
                      required
                    />
                  </div>

                  <div className="mb-4">
                    <label className="
                      form-label fw-semibold">
                      Confirm New Password
                    </label>
                    <input
                      type="password"
                      name="confirmPassword"
                      className="form-control"
                      placeholder="Repeat new password"
                      value={
                        passwordData
                          .confirmPassword}
                      onChange={
                        handlePasswordChange}
                      required
                    />
                  </div>

                  {/* Modal Footer */}
                  <div className="d-flex
                    gap-2 justify-content-end">
                    <button
                      type="button"
                      onClick={() => {
                        setShowModal(false);
                        setPwError('');
                      }}
                      className="btn
                        btn-outline-secondary"
                      style={{
                        borderRadius: '8px',
                        padding: '10px 20px'
                      }}>
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={pwLoading}
                      className="btn"
                      style={{
                        background: 'linear-gradient(135deg, #667eea, #764ba2)',
                        color: 'white',
                        borderRadius: '8px',
                        padding: '10px 20px',
                        border: 'none',
                        fontWeight: '600'
                      }}>
                      {pwLoading ? (
                        <>
                          <span className="
                            spinner-border
                            spinner-border-sm
                            me-2"/>
                          Saving...
                        </>
                      ) : 'Save Password'}
                    </button>
                  </div>
                </form>
              </div>

            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Profile;