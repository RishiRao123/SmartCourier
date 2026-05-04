import React, { useState, useEffect } from 'react';
import { formatDate } from '../utils/dateFormat';
import { Search, Shield, Crown, User, Trash2, Download, UserPlus, ChevronLeft, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

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

import { useUsers } from '../hooks/useUsers';
 
const roleColors: Record<string, string> = {
  ROLE_SUPER_ADMIN: 'bg-yellow-500/10 text-yellow-600 border-yellow-500/30',
  ROLE_ADMIN: 'bg-blue-500/10 text-blue-600 border-blue-500/30',
  ROLE_CUSTOMER: 'bg-green-500/10 text-green-600 border-green-500/30',
};
 
const roleIcons: Record<string, React.ReactNode> = {
  ROLE_SUPER_ADMIN: <Crown className="w-3.5 h-3.5" />,
  ROLE_ADMIN: <Shield className="w-3.5 h-3.5" />,
  ROLE_CUSTOMER: <User className="w-3.5 h-3.5" />,
};
 
const roleLabels: Record<string, string> = {
  ROLE_SUPER_ADMIN: 'SUPER ADMIN',
  ROLE_ADMIN: 'ADMIN',
  ROLE_CUSTOMER: 'CUSTOMER',
};
 
const UsersPage = () => {
  const { isSuperAdmin } = useAuth();
  const navigate = useNavigate();
  const {
    users,
    loading,
    handleRoleChange,
    handleActivationToggle,
    handleDelete
  } = useUsers();
 
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
 
  // Pagination State
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const downloadCSV = () => {
    const headers = ['ID', 'Username', 'Email', 'Role', 'Status', 'Joined Date'];
    const csvRows = [
      headers.join(','),
      ...filteredUsers.map(u => [
        u.id,
        `"${u.username}"`,
        `"${u.email}"`,
        u.role,
        u.active ? 'ACTIVE' : 'INACTIVE',
        u.createdAt ? formatDate(u.createdAt) : 'N/A'
      ].join(','))
    ];

    const csvString = csvRows.join('\n');
    const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `SmartCourier_Users_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    toast.success('User list exported to CSV');
  };

  const filteredUsers = users.filter(u => {
    const matchesSearch = u.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          u.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesRole = roleFilter === 'ALL' || u.role === roleFilter;
    
    // Admin restriction: only see customers
    if (!isSuperAdmin && u.role !== 'ROLE_CUSTOMER') return false;
    
    return matchesSearch && matchesRole;
  });

  const stats = {
    total: isSuperAdmin ? users.length : users.filter(u => u.role === 'ROLE_CUSTOMER').length,
    superAdmins: users.filter(u => u.role === 'ROLE_SUPER_ADMIN').length,
    admins: users.filter(u => u.role === 'ROLE_ADMIN').length,
    customers: users.filter(u => u.role === 'ROLE_CUSTOMER').length,
  };

  // Pagination Logic
  const totalPages = Math.ceil(filteredUsers.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredUsers.slice(indexOfFirstItem, indexOfLastItem);

  const paginate = (pageNumber: number) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <h1 className="text-4xl font-black text-blue-900 tracking-tighter">Users</h1>
          <p className="text-gray-500 font-bold mt-1">See and manage all user accounts</p>
        </div>
        {isSuperAdmin && (
          <div className="flex gap-4">
            <button
              onClick={downloadCSV}
              className="bg-yellow-500 text-[#071a2a] border-2 border-transparent font-black px-6 py-4 rounded-2xl shadow-sm hover:bg-yellow-400 hover:-translate-y-1 transition-all flex items-center gap-2"
            >
              <Download className="w-5 h-5" />
              Download CSV
            </button>
            <button
              onClick={() => navigate('/settings')}
              className="bg-yellow-500 text-[#071a2a] font-black px-6 py-4 rounded-2xl shadow-xl hover:bg-yellow-400 hover:-translate-y-1 transition-all flex items-center gap-2"
            >
              <UserPlus className="w-5 h-5" />
              Add New Admin
            </button>
          </div>
        )}
        {totalPages > 1 && (
          <div className="flex items-center bg-gray-100 p-1 rounded-xl border border-gray-200">
            <button 
              onClick={() => paginate(currentPage - 1)}
              disabled={currentPage === 1}
              className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="px-3 text-[10px] font-black text-[#071a2a]">
              {currentPage} / {totalPages}
            </span>
            <button 
              onClick={() => paginate(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl p-5 shadow-md border border-gray-100">
          <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{isSuperAdmin ? 'Total Users' : 'Total Customers'}</p>
          <h4 className="text-3xl font-black text-blue-900">{stats.total}</h4>
        </div>
        {isSuperAdmin && (
          <>
            <div className="bg-white rounded-2xl p-5 shadow-md border border-gray-100">
              <p className="text-[10px] font-black text-yellow-600 uppercase tracking-widest mb-1">Super Admins</p>
              <h4 className="text-3xl font-black text-yellow-600">{stats.superAdmins}</h4>
            </div>
            <div className="bg-white rounded-2xl p-5 shadow-md border border-gray-100">
              <p className="text-[10px] font-black text-blue-600 uppercase tracking-widest mb-1">Admins</p>
              <h4 className="text-3xl font-black text-blue-600">{stats.admins}</h4>
            </div>
          </>
        )}
        <div className="bg-white rounded-2xl p-5 shadow-md border border-gray-100">
          <p className="text-[10px] font-black text-green-600 uppercase tracking-widest mb-1">Customers</p>
          <h4 className="text-3xl font-black text-green-600">{stats.customers}</h4>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-white rounded-[32px] shadow-xl border border-gray-100 p-6">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex-1 min-w-[250px] relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Search by name or email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-gray-50 border-2 border-gray-100 rounded-2xl py-3 pl-12 pr-4 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all"
            />
          </div>
          <div className="flex gap-2">
            {['ALL', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_CUSTOMER']
              .filter(role => isSuperAdmin || role === 'ROLE_CUSTOMER' || role === 'ALL')
              .map(role => (
                <button
                  key={role}
                  onClick={() => setRoleFilter(role)}
                  className={`px-4 py-2.5 rounded-xl text-xs font-black uppercase tracking-widest transition-all ${
                    roleFilter === role
                      ? 'bg-blue-900 text-yellow-500 shadow-lg'
                      : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                  }`}
                >
                  {role === 'ALL' ? 'All' : roleLabels[role]}
                </button>
              ))}
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-[32px] shadow-xl border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100">
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">User</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Email</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Role</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Status</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Joined</th>
                {(isSuperAdmin || !isSuperAdmin) && (
                  <th className="px-8 py-5 text-right text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Actions</th>
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-8 py-20">
                    <LoadingSpinner message="Loading users..." />
                  </td>
                </tr>
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-8 py-20 text-center">
                    <p className="text-gray-400 font-bold uppercase tracking-widest text-sm">No users found</p>
                  </td>
                </tr>
              ) : (
                currentItems.map((u) => (
                  <tr key={u.id} className="hover:bg-blue-50/40 transition-all group">
                    <td className="px-8 py-5">
                      <div className="flex items-center gap-4">
                        <div className="w-10 h-10 bg-blue-900 rounded-xl flex items-center justify-center text-white font-black text-sm">
                          {u.username?.charAt(0)?.toUpperCase() || '?'}
                        </div>
                        <span className="font-black text-blue-900">{u.username}</span>
                      </div>
                    </td>
                    <td className="px-8 py-5 text-sm font-bold text-gray-500">{u.email}</td>
                    <td className="px-8 py-5">
                      {isSuperAdmin && u.role !== 'ROLE_SUPER_ADMIN' ? (
                        <select
                          value={u.role}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className={`px-3 py-1.5 rounded-full text-xs font-black tracking-widest border appearance-none cursor-pointer ${roleColors[u.role]}`}
                        >
                          <option value="ROLE_CUSTOMER">CUSTOMER</option>
                          <option value="ROLE_ADMIN">ADMIN</option>
                        </select>
                      ) : (
                        <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-black tracking-widest border ${roleColors[u.role]}`}>
                          {roleIcons[u.role]}
                          {roleLabels[u.role]}
                        </span>
                      )}
                    </td>

                    <td className="px-8 py-5">
                      <span className={`px-3 py-1 rounded-full text-xs font-black tracking-widest ${
                        u.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                      }`}>
                        {u.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="px-8 py-5 text-sm font-bold text-gray-500">
                      {formatDate(u.createdAt)}
                    </td>
                    <td className="px-8 py-5 text-right space-x-2">
                      {u.role !== 'ROLE_SUPER_ADMIN' && (
                        <>
                          <button
                            onClick={() => handleActivationToggle(u.id, u.active)}
                            className={`p-2 rounded-xl transition-all font-bold text-xs ${
                              u.active ? 'text-yellow-600 bg-yellow-50 hover:bg-yellow-100' : 'text-green-600 bg-green-50 hover:bg-green-100'
                            }`}
                            title={u.active ? "Deactivate User" : "Activate User"}
                          >
                            {u.active ? "Deactivate" : "Activate"}
                          </button>
                          <button
                            onClick={() => handleDelete(u.id, u.username)}
                            className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all"
                            title="Delete User"
                          >
                            <Trash2 className="w-4 h-4 inline-block" />
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <div className="p-8 border-t border-gray-50 flex justify-center bg-gray-50/30">
            <div className="flex items-center gap-2">
              <button
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
                className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((number) => (
                <button
                  key={number}
                  onClick={() => paginate(number)}
                  className={`w-10 h-10 rounded-xl font-black text-sm transition-all ${
                    currentPage === number 
                      ? 'bg-yellow-500 text-[#071a2a] shadow-lg shadow-yellow-500/30' 
                      : 'bg-white text-gray-400 hover:bg-gray-100 border border-gray-200'
                  }`}
                >
                  {number}
                </button>
              ))}
              <button
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>
          </div>
        )}
      </div>

    </div>
  );
};

export default UsersPage;
