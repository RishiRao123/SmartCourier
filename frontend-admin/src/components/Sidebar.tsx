import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Package, LogOut } from 'lucide-react';

const Sidebar = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { name: 'Deliveries', path: '/deliveries', icon: <Package className="w-5 h-5" /> },
  ];

  return (
    <aside className="w-64 bg-[#071a2a] text-white flex flex-col h-screen shadow-2xl z-20 transition-all duration-300">
      <div className="h-20 flex items-center justify-center border-b border-white/5 px-4 group cursor-pointer relative overflow-hidden">
        <div className="flex items-center space-x-3 transition-transform duration-500 group-hover:scale-105 relative z-10">
          <div className="w-10 h-10 bg-yellow-500 rounded-xl flex items-center justify-center shadow-lg relative overflow-hidden transform group-hover:rotate-6 transition-all duration-500">
            <Package className="w-6 h-6 text-[#071a2a]" />
          </div>
          <span className="text-2xl font-black tracking-tight text-white">Smart<span className="text-yellow-500">Courier</span></span>
        </div>
      </div>

      <nav className="flex-1 py-8 px-4 space-y-2.5 overflow-y-auto">
        {navItems.map((item) => (
          <NavLink
            key={item.name}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center space-x-4 px-4 py-3.5 rounded-xl transition-all duration-300 relative overflow-hidden group ${
                isActive
                ? 'bg-yellow-500 text-[#071a2a] font-bold shadow-[0_4px_15px_rgba(255,199,44,0.35)] transform scale-[1.02]'
                : 'text-gray-300 hover:bg-white/10 hover:text-white hover:translate-x-1.5'
            }`
          }
        >
          {item.icon}
          <span className="tracking-wide text-sm">{item.name}</span>
        </NavLink>
      ))}
    </nav>

    <div className="p-5 border-t border-white/5 bg-white/2">
      <button
        onClick={handleLogout}
        className="flex items-center space-x-3 w-full px-4 py-3.5 text-gray-300 hover:bg-red-500/10 hover:text-red-400 rounded-xl transition-all duration-300 group font-semibold"
      >
          <LogOut className="w-5 h-5 transition-transform duration-300 group-hover:-translate-x-1" />
          <span>Secure Logout</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
