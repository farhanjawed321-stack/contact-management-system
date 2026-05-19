import { BrowserRouter, Routes, Route,
  Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import Contacts from './pages/Contacts';
import Profile from './pages/Profile';

function PrivateRoute({ children }) {
  const { isAuth } = useAuth();
  return isAuth ? children : 
    <Navigate to="/login" />;
}

function PublicRoute({ children }) {
  const { isAuth } = useAuth();
  return !isAuth ? children : 
    <Navigate to="/contacts" />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={
          <PublicRoute><Login /></PublicRoute>
        } />
        <Route path="/register" element={
          <PublicRoute><Register /></PublicRoute>
        } />
        <Route path="/contacts" element={
          <PrivateRoute><Contacts /></PrivateRoute>
        } />
        <Route path="/profile" element={
          <PrivateRoute><Profile /></PrivateRoute>
        } />
        <Route path="/" element={
          <Navigate to="/contacts" />
        } />
        <Route path="*" element={
          <Navigate to="/contacts" />
        } />
      </Routes>
    </BrowserRouter>
  );
}

export default App;