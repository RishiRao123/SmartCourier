import React from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Layout from './components/Layout';
import Home from './pages/Home';
import Track from './pages/Track';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import CreateShipment from './pages/CreateShipment';
import InvoicePage from './pages/InvoicePage';
import Profile from './pages/Profile';
import ForgotPassword from './pages/ForgotPassword';
import ProtectedRoute from './components/ProtectedRoute';
import ScrollToTop from './components/ScrollToTop';

const App: React.FC = () => {
  return (
    <>
      <Toaster 
        position="top-center" 
        toastOptions={{ 
          duration: 4000,
          style: {
            background: '#ffffff',
            color: '#071a2a',
            fontWeight: '600',
            borderRadius: '12px',
            border: '2px solid #EAB308',
            boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
            padding: '16px 24px',
          },
          success: {
            iconTheme: {
              primary: '#EAB308',
              secondary: '#fff',
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
      <Router>
        <ScrollToTop />
        <Routes>
        {/* Full-screen Auth Pages */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        
        {/* Main Application Routes with Layout */}
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="track/:trackingNumber" element={<Track />} />
          <Route path="dashboard" element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } />
          <Route path="create-shipment" element={
            <ProtectedRoute>
              <CreateShipment />
            </ProtectedRoute>
          } />
          <Route path="invoice/:trackingNumber" element={
            <ProtectedRoute>
              <InvoicePage />
            </ProtectedRoute>
          } />
          <Route path="profile" element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          } />
          <Route path="*" element={<Home />} />
        </Route>
      </Routes>
    </Router>
    </>
  );
};

export default App;
