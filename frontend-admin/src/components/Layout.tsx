import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Package, LogOut, ShieldCheck, Search, ArrowRight, Users, User, FileText, Crown } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const Layout = () => {
  const navigate = useNavigate();
  const { logout, user, isSuperAdmin } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e?: React.KeyboardEvent<HTMLInputElement>) => {
    if ((!e || e.key === 'Enter') && searchQuery.trim()) {
      navigate(`/tracking/${searchQuery.trim()}`);
      setSearchQuery('');
    }
  };

  const handleLogout = () => {
    logout();
  };

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { name: 'Deliveries', path: '/deliveries', icon: <Package className="w-5 h-5" /> },
    { name: 'Users', path: '/users', icon: <Users className="w-5 h-5" /> },
    { name: 'Reports', path: '/reports', icon: <FileText className="w-5 h-5" /> },
    ...(isSuperAdmin ? [{ name: 'Add Admin', path: '/settings', icon: <ShieldCheck className="w-5 h-5" /> }] : []),
    { name: 'Profile', path: '/profile', icon: <User className="w-5 h-5" /> },
  ];



  return (
    <div className="flex h-screen bg-gray-100 overflow-hidden font-sans">
      {/* Fixed Sidebar - Dark Blue */}
      <aside className="w-64 bg-blue-900 text-white flex flex-col h-screen shadow-2xl z-20 flex-shrink-0 transition-all duration-300">
        <div className="h-20 flex items-center justify-center border-b border-blue-800 px-4 group cursor-pointer relative overflow-hidden">
          <div className="flex items-center space-x-3 transition-transform duration-500 group-hover:scale-105 relative z-10">
            <div className="w-10 h-10 bg-yellow-500 rounded-xl flex items-center justify-center shadow-lg transform group-hover:rotate-6 transition-all duration-500">
              <Package className="w-6 h-6 text-blue-900" />
            </div>
            <span className="text-2xl font-black tracking-tight">Smart<span className="text-yellow-500">Courier</span></span>
          </div>
        </div>

        {/* Role Badge */}
        <div className="px-4 pt-6 pb-2">
          {isSuperAdmin ? (
            <div className="flex items-center gap-2 bg-yellow-500/15 border border-yellow-500/30 rounded-xl px-3 py-2">
              <Crown className="w-4 h-4 text-yellow-400" />
              <span className="text-xs font-black text-yellow-400 tracking-widest uppercase">Super Admin</span>
            </div>
          ) : (
            <div className="flex items-center gap-2 bg-blue-800/50 border border-blue-700/50 rounded-xl px-3 py-2">
              <ShieldCheck className="w-4 h-4 text-blue-300" />
              <span className="text-xs font-black text-blue-300 tracking-widest uppercase">Admin</span>
            </div>
          )}
        </div>

        <nav className="flex-1 py-4 px-4 space-y-3 overflow-y-auto">
          <p className="px-4 text-xs font-bold text-blue-300/60 tracking-[0.15em] uppercase mb-4">Main Menu</p>
          {navItems.map((item) => (
            <NavLink
              key={item.name}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center space-x-4 px-4 py-3.5 rounded-xl transition-all duration-300 relative overflow-hidden group ${
                  isActive
                    ? 'bg-yellow-500 text-blue-900 font-bold shadow-[0_4px_15px_rgba(255,199,44,0.35)] transform scale-[1.02]'
                    : 'text-gray-300 hover:bg-blue-800 hover:text-white hover:translate-x-1.5'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <div className={`transition-transform duration-300 ${isActive ? 'scale-110' : 'group-hover:scale-110 group-hover:text-yellow-500'}`}>
                    {item.icon}
                  </div>
                  <span className="tracking-wide text-sm font-semibold">{item.name}</span>
                </>
              )}
            </NavLink>
          ))}
        </nav>

        <div className="p-5 border-t border-blue-800 bg-blue-900/50">
          <button
            onClick={handleLogout}
            className="flex items-center space-x-3 w-full px-4 py-3.5 text-gray-300 hover:bg-red-500/10 hover:text-red-400 rounded-xl transition-all duration-300 group font-semibold"
          >
            <LogOut className="w-5 h-5 transition-transform duration-300 group-hover:-translate-x-1" />
            <span>Secure Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Top Header - Redesigned for focus */}
        <header className="h-24 bg-white/80 backdrop-blur-md border-b border-gray-100 flex items-center justify-between px-10 z-10 flex-shrink-0 sticky top-0">
          
          {/* Focused Search Bar Area */}
          <div className="flex-1 flex justify-center">
            <div className="w-full max-w-3xl relative group">
              <div className="absolute -inset-1 bg-gradient-to-r from-blue-600 to-yellow-500 rounded-[28px] blur opacity-10 group-hover:opacity-20 transition duration-1000 group-hover:duration-200"></div>
              <div className="relative flex items-center">
                <div className="absolute left-6 text-blue-900/40 group-focus-within:text-yellow-500 transition-colors">
                  <Search className="w-6 h-6" />
                </div>
                <input 
                  type="text" 
                  placeholder="Enter Tracking Id"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onKeyDown={handleSearch}
                  className="w-full bg-gray-50/50 border-2 border-gray-100 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 rounded-3xl py-4 pl-16 pr-20 text-base font-black text-blue-900 placeholder:text-blue-900/60 shadow-inner transition-all outline-none"
                />
                <button 
                  onClick={() => handleSearch()}
                  className="absolute right-3 bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] px-6 py-2.5 rounded-2xl transition-all shadow-lg flex items-center gap-2 group/btn"
                >
                  <span className="text-[10px] font-black uppercase tracking-widest">Search</span>
                  <ArrowRight className="w-4 h-4 transition-transform group-hover/btn:translate-x-1" />
                </button>
              </div>
            </div>
          </div>

          {/* Minimal Profile Area */}
          <div 
            onClick={() => navigate('/profile')}
            className="flex items-center gap-4 ml-10 cursor-pointer group"
          >
            <div className="text-right hidden sm:block">
              <p className="text-sm font-black text-blue-900 leading-tight group-hover:text-yellow-600 transition-colors">{user?.username || 'Admin'}</p>
              <p className="text-[10px] font-bold text-gray-400 uppercase tracking-[0.2em]">
                {isSuperAdmin ? 'Super Admin' : 'Admin'}
              </p>
            </div>
            <div className="w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center border-2 border-gray-100 group-hover:border-yellow-500/50 transition-all overflow-hidden shadow-sm">
              {user?.profileImagePath ? (
                <img 
                  src={`http://localhost:8080/auth/profile/image/${user.profileImagePath}`} 
                  alt="Profile" 
                  className="w-full h-full object-cover transition-transform group-hover:scale-110"
                />
              ) : (
                <User className="w-6 h-6 text-blue-900/30 group-hover:text-yellow-500" />
              )}
            </div>
          </div>
        </header>

        {/* Dynamic Outlet */}
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-100 p-8">
          <div className="max-w-7xl mx-auto animate-fade-in-up">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default Layout;
