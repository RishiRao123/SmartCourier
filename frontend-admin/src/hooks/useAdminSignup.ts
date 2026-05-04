import { useState } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';

export const useAdminSignup = () => {
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSignup = async (username: string, email: string, password: string, confirmPassword: string) => {
    if (!username || !email || !password) {
      toast.error('All fields are required');
      return false;
    }
    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return false;
    }
    if (password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return false;
    }
    
    setLoading(true);
    try {
      await api.post('/auth/admin/signup', { username, email, password });
      setSubmitted(true);
      return true;
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Registration failed');
      return false;
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    submitted,
    handleSignup
  };
};
