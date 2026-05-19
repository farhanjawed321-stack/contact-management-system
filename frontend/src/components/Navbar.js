import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="custom-navbar d-flex
      justify-content-between align-items-center">
      <Link to="/contacts" className="brand">
        📋 Contact Manager
      </Link>
      <div>
        <Link to="/contacts"
          className="nav-link-custom">
          Contacts
        </Link>
        <Link to="/profile"
          className="nav-link-custom">
          Profile
        </Link>
        <button
          onClick={handleLogout}
          className="nav-link-custom">
          Logout
        </button>
      </div>
    </nav>
  );
}

export default Navbar;