import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Validate
    if (!formData.email && !formData.phone) {
      setError(
        'Please provide email or phone number');
      setLoading(false);
      return;
    }

    if (formData.password !==
        formData.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    if (formData.password.length < 6) {
      setError(
        'Password must be at least 6 characters');
      setLoading(false);
      return;
    }

    try {
      const { confirmPassword, ...submitData }
        = formData;

      const response = await api.post(
        '/auth/register', submitData);
      login(response.data.token);
      navigate('/contacts');
    } catch (err) {
      setError(
        err.response?.data?.error ||
        'Registration failed. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">

        {/* Logo */}
        <div style={{
          textAlign: 'center',
          marginBottom: '8px',
          fontSize: '48px'
        }}>
          📋
        </div>

        <h2>Create Account</h2>
        <p className="subtitle">
          Start managing your contacts today
        </p>

        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger
            alert-custom" role="alert">
            ⚠️ {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>

          {/* Name Row */}
          <div className="row mb-3">
            <div className="col">
              <label className="form-label
                fw-semibold">
                First Name
              </label>
              <input
                type="text"
                name="firstName"
                className="form-control"
                placeholder="First name"
                value={formData.firstName}
                onChange={handleChange}
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
                onChange={handleChange}
              />
            </div>
          </div>

          {/* Email */}
          <div className="mb-3">
            <label className="form-label
              fw-semibold">
              Email Address
            </label>
            <input
              type="email"
              name="email"
              className="form-control"
              placeholder="Enter email"
              value={formData.email}
              onChange={handleChange}
            />
          </div>

          {/* Phone */}
          <div className="mb-3">
            <label className="form-label
              fw-semibold">
              Phone Number
              <span style={{
                color: '#999',
                fontWeight: 'normal',
                fontSize: '12px',
                marginLeft: '8px'
              }}>
                (if no email)
              </span>
            </label>
            <input
              type="text"
              name="phone"
              className="form-control"
              placeholder="+923001234567"
              value={formData.phone}
              onChange={handleChange}
            />
          </div>

          {/* Password */}
          <div className="mb-3">
            <label className="form-label
              fw-semibold">
              Password
            </label>
            <input
              type="password"
              name="password"
              className="form-control"
              placeholder="Min 6 characters"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          {/* Confirm Password */}
          <div className="mb-4">
            <label className="form-label
              fw-semibold">
              Confirm Password
            </label>
            <input
              type="password"
              name="confirmPassword"
              className="form-control"
              placeholder="Repeat password"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
          </div>

          {/* Submit */}
          <button
            type="submit"
            className="btn-primary-custom"
            disabled={loading}>
            {loading ? (
              <>
                <span className="spinner-border
                  spinner-border-sm me-2"/>
                Creating Account...
              </>
            ) : 'Create Account'}
          </button>
        </form>

        {/* Login Link */}
        <p style={{
          textAlign: 'center',
          marginTop: '24px',
          color: '#666',
          fontSize: '14px'
        }}>
          Already have an account?{' '}
          <Link to="/login" style={{
            color: '#667eea',
            fontWeight: '600',
            textDecoration: 'none'
          }}>
            Sign In
          </Link>
        </p>

      </div>
    </div>
  );
}

export default Register;