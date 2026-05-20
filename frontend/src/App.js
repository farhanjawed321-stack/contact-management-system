import { BrowserRouter, Routes, Route, 
  Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Contacts from './pages/Contacts';
import Profile from './pages/Profile';
import ContactDetail from './pages/ContactDetail';

// ─── Protected Route ──────────────────────────
// Redirects to login if not authenticated
function PrivateRoute({ children }) {
  const { isAuth } = useAuth();
  return isAuth ? children : 
    <Navigate to="/login" replace />;
}

// ─── Public Route ─────────────────────────────
// Redirects to contacts if already logged in
function PublicRoute({ children }) {
  const { isAuth } = useAuth();
  return !isAuth ? children : 
    <Navigate to="/contacts" replace />;
}

// ─── Main App ─────────────────────────────────
function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* ── Public Routes ── */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <Login />
            </PublicRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicRoute>
              <Register />
            </PublicRoute>
          }
        />

        {/* ── Protected Routes ── */}
        <Route
          path="/contacts"
          element={
            <PrivateRoute>
              <Contacts />
            </PrivateRoute>
          }
        />

        {/* Contact Detail Page */}
        <Route
          path="/contacts/:id"
          element={
            <PrivateRoute>
              <ContactDetail />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <Profile />
            </PrivateRoute>
          }
        />

        {/* ── Default Redirects ── */}
        <Route
          path="/"
          element={
            <Navigate to="/contacts" replace />
          }
        />
        <Route
          path="*"
          element={
            <Navigate to="/contacts" replace />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}

export default App;