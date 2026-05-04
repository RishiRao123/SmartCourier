import React from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Package, Github, Twitter, Linkedin, User, LogOut } from 'lucide-react';
import toast from 'react-hot-toast';

const Layout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const isLoggedIn = !!localStorage.getItem('user_token');
  const isHomePage = location.pathname === '/';

  const handleLogout = () => {
    localStorage.removeItem('user_token');
    toast.success('Logged out successfully');
    navigate('/login');
  };

  return (
    <div className={`min-h-screen flex flex-col font-sans selection:bg-yellow-500 selection:text-[#071a2a] ${isHomePage ? 'bg-[#071a2a]' : 'bg-white'}`}>
      {/* Navbar */}
      <nav className="sticky top-0 z-50 bg-[#071a2a] border-b border-white/5 shadow-xl">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-20 items-center">
            {/* Logo */}
            <Link to="/" className="flex items-center space-x-3 group cursor-pointer">
              <div className="flex items-center space-x-3 transition-all duration-500 group-hover:scale-105">
                <div className="w-10 h-10 bg-yellow-500 rounded-xl flex items-center justify-center shadow-lg transform group-hover:rotate-6 transition-all duration-500 flex-shrink-0">
                  <Package className="w-6 h-6 text-[#071a2a]" />
                </div>
                <span className="text-2xl font-black tracking-tight whitespace-nowrap text-white">Smart<span className="text-yellow-500">Courier</span></span>
              </div>
            </Link>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center space-x-8">
              <Link to="/" className="font-medium transition-colors text-gray-300 hover:text-white">How it works</Link>
              {isLoggedIn ? (
                <>
                  <Link to="/dashboard" className="font-medium transition-colors text-gray-300 hover:text-white">My Dashboard</Link>
                  <Link to="/profile" className="flex items-center gap-2 font-medium transition-colors text-gray-300 hover:text-white">
                    <User className="w-4 h-4" />
                    My Profile
                  </Link>
                  <button 
                    onClick={handleLogout}
                    className="bg-yellow-500 text-[#071a2a] px-6 py-2.5 rounded-xl font-bold hover:bg-yellow-400 transition-all shadow-md active:scale-95 flex items-center gap-2"
                  >
                    <LogOut className="w-4 h-4" />
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" className="font-medium transition-colors text-gray-300 hover:text-white">Login</Link>
                  <Link to="/signup" className="bg-yellow-500 text-[#071a2a] px-6 py-2.5 rounded-xl font-bold hover:bg-yellow-400 transition-all shadow-md active:scale-95">
                    Register
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className={`flex-grow ${!isHomePage ? 'border-t-4 border-yellow-500' : ''}`}>
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-[#071a2a] text-white pt-16 pb-8 border-t border-white/5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
            <div className="col-span-1 md:col-span-2">
              <Link to="/" className="flex items-center space-x-3 group cursor-pointer mb-6">
                <div className="w-10 h-10 bg-yellow-500 rounded-xl flex items-center justify-center shadow-lg transform group-hover:rotate-6 transition-all duration-500">
                  <Package className="w-6 h-6 text-[#071a2a]" />
                </div>
                <span className="text-2xl font-black tracking-tight text-white">
                  Smart<span className="text-yellow-500">Courier</span>
                </span>
              </Link>
              <p className="text-gray-500 max-w-sm leading-relaxed">
                India's leading tech-enabled logistics platform. Fast, reliable, and transparent shipping solutions for the modern Indian business.
              </p>
            </div>
            
            <div>
              <h4 className="text-lg font-bold mb-6 text-white">Quick Links</h4>
              <ul className="space-y-4 text-gray-500 font-medium">
                <li><Link to="/" className="hover:text-yellow-500 transition-colors">Track Package</Link></li>
                <li><Link to="#" className="hover:text-yellow-500 transition-colors">Shipping Rates</Link></li>
                <li><Link to="#" className="hover:text-yellow-500 transition-colors">Service Points</Link></li>
              </ul>
            </div>

            <div>
              <h4 className="text-lg font-bold mb-6 text-white">Support</h4>
              <ul className="space-y-4 text-gray-500 font-medium">
                <li><Link to="#" className="hover:text-yellow-500 transition-colors">Help Center</Link></li>
                <li><Link to="#" className="hover:text-yellow-500 transition-colors">Contact Support</Link></li>
                <li><Link to="#" className="hover:text-yellow-500 transition-colors">API Docs</Link></li>
              </ul>
            </div>
          </div>
          
          <div className="border-t border-white/5 pt-8 flex flex-col md:flex-row justify-between items-center">
            <p className="text-gray-500 text-sm font-medium">
              &copy; {new Date().getFullYear()} SmartCourier Logistics India.
            </p>
            <div className="flex space-x-6 mt-4 md:mt-0 text-sm text-gray-500 font-medium">
              <a href="#" className="hover:text-white transition-colors">Privacy Policy</a>
              <a href="#" className="hover:text-white transition-colors">Terms of Service</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
