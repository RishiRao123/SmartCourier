import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Login from './pages/Login';
import AdminSignup from './pages/AdminSignup';
import Dashboard from './pages/Dashboard';
import Deliveries from './pages/Deliveries';
import Settings from './pages/Settings';
import Reports from './pages/Reports';
import TrackingDetails from './pages/TrackingDetails';
import Users from './pages/Users';
import AdminProfile from './pages/AdminProfile';
import ForgotPassword from './pages/ForgotPassword';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import ScrollToTop from './components/ScrollToTop';
import { AuthProvider } from './contexts/AuthContext';



function App() {
  return (
    <>
      <Toaster 
        position="top-center" 
        toastOptions={{ 
          duration: 4000,
          style: {
            background: '#061826',
            color: '#fff',
            fontWeight: '600',
            borderRadius: '12px',
            boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
            padding: '16px 24px',
          },
          success: {
            iconTheme: {
              primary: '#FFC72C',
              secondary: '#061826',
            },
          },
          error: {
            style: {
              background: '#fee2e2',
              color: '#991b1b',
              border: '1px solid #f87171'
            },
            iconTheme: {
              primary: '#ef4444',
              secondary: '#fee2e2',
            },
          }
        }} 
      />
      <BrowserRouter>
        <ScrollToTop />
        <AuthProvider>
          <Routes>
          {/* Default Route handling */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          
          {/* Public Auth Route */}
          <Route path="/login" element={<Login />} />
          <Route path="/admin-signup" element={<AdminSignup />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          
          {/* Protected Routes wrapped in the main Layout shell */}
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/deliveries" element={<Deliveries />} />
            <Route path="/users" element={<Users />} />
            <Route path="/profile" element={<AdminProfile />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/tracking/:trackingNumber" element={<TrackingDetails />} />
            <Route path="/settings" element={<Settings />} />
          </Route>
        </Route>
          
          {/* Catch-all route */}
          <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </>
  );
}

export default App;
