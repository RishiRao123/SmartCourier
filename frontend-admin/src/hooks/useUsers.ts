import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import api from '../services/api';

interface UserData {
  id: number;
  username: string;
  email: string;
  role: string;
  phone: string;
  city: string;
  state: string;
  profileImagePath: string;
  createdAt: string;
  active: boolean;
}

export const useUsers = () => {
  const [users, setUsers] = useState<UserData[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/users');
      const data = res.data?.data ?? res.data;
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = async (userId: number, newRole: string) => {
    try {
      await api.put(`/admin/users/${userId}/role?newRole=${newRole}`);
      toast.success('User role updated successfully');
      fetchUsers();
      return true;
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to update role');
      return false;
    }
  };

  const handleActivationToggle = async (userId: number, currentStatus: boolean) => {
    try {
      await api.put(`/admin/users/${userId}/activate?active=${!currentStatus}`);
      toast.success(`User ${!currentStatus ? 'activated' : 'deactivated'} successfully`);
      fetchUsers();
      return true;
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to update status');
      return false;
    }
  };

  const handleDelete = async (userId: number, username: string) => {
    if (!confirm(`Are you sure you want to delete user "${username}"? This action cannot be undone.`)) return false;
    try {
      await api.delete(`/admin/users/${userId}`);
      toast.success('User deleted successfully');
      fetchUsers();
      return true;
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to delete user');
      return false;
    }
  };

  return {
    users,
    loading,
    handleRoleChange,
    handleActivationToggle,
    handleDelete,
    refresh: fetchUsers
  };
};
