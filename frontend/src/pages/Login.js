import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    identifier: '',
    password: ''
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

    try {
      const response = await api.post(
        '/auth/login', formData);
      login(response.data.token);
      navigate('/contacts');
    } catch (err) {
      setError(
        err.response?.data?.error ||
        'Login failed. Please try again.'
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

        <h2>Welcome Back!</h2>
        <p className="subtitle">
          Sign in to manage your contacts
        </p>

        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger
            alert-custom" role="alert">
            ⚠️ {error}
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit}>

          {/* Email or Phone */}
          <div className="mb-3">
            <label className="form-label
              fw-semibold">
              Email or Phone
            </label>
            <input
              type="text"
              name="identifier"
              className="form-control"
              placeholder="Enter email or phone"
              value={formData.identifier}
              onChange={handleChange}
              required
            />
          </div>

          {/* Password */}
          <div className="mb-4">
            <label className="form-label
              fw-semibold">
              Password
            </label>
            <input
              type="password"
              name="password"
              className="form-control"
              placeholder="Enter password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            className="btn-primary-custom"
            disabled={loading}>
            {loading ? (
              <>
                <span className="spinner-border
                  spinner-border-sm me-2"/>
                Signing in...
              </>
            ) : 'Sign In'}
          </button>
        </form>

        {/* Register Link */}
        <p style={{
          textAlign: 'center',
          marginTop: '24px',
          color: '#666',
          fontSize: '14px'
        }}>
          Don't have an account?{' '}
          <Link to="/register" style={{
            color: '#667eea',
            fontWeight: '600',
            textDecoration: 'none'
          }}>
            Create Account
          </Link>
        </p>

      </div>
    </div>
  );
}

export default Login;