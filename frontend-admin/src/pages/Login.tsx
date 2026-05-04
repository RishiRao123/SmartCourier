import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Truck, ArrowRight, ShieldCheck, Eye, EyeOff } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const validateForm = () => {
    if (!email) {
      toast.error('Email address is required');
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      toast.error('Please enter a valid email address');
      return false;
    }
    if (!password) {
      toast.error('Password is required');
      return false;
    }
    if (password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return false;
    }
    return true;
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { email, password });
      let token = response.data?.data || response.data?.token || response.data;
      if (typeof token === 'object') throw new Error('Received malformed token from server');
      if (typeof token === 'string' && token.startsWith('token: ')) token = token.replace('token: ', '').trim();
      
      login(token);
      toast.success('Login Successful! Redirecting...');
      setTimeout(() => navigate('/dashboard'), 1000);
    } catch (err: any) {
      console.error(err);
      toast.error(err.response?.data?.message || 'Login failed. Please verify your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex font-sans bg-white">
      {/* Left Pane - Branding & Image */}
      <div className="hidden lg:flex w-1/2 bg-[#071a2a] flex-col justify-center items-center relative overflow-hidden p-12">
        <div className="absolute top-0 left-0 w-full h-full pointer-events-none">
          <div className="absolute top-0 left-0 w-[50%] h-[50%] bg-blue-500/10 rounded-full blur-[100px]"></div>
          <div className="absolute bottom-0 right-0 w-[50%] h-[50%] bg-yellow-500/10 rounded-full blur-[100px]"></div>
        </div>
        
        <div className="relative z-10 w-full max-w-lg flex flex-col items-center text-center">
          <div className="flex items-center justify-center gap-4 mb-8 group cursor-pointer" onClick={() => navigate('/')}>
            <div className="w-16 h-16 bg-yellow-500 rounded-2xl flex items-center justify-center shadow-[0_0_40px_rgba(255,199,44,0.3)] transform group-hover:-translate-y-2 group-hover:rotate-[5deg] transition-all duration-500">
              <Truck className="w-8 h-8 text-[#071a2a]" />
            </div>
            <h1 className="text-4xl font-black text-white tracking-tight">Smart<span className="text-yellow-500">Courier</span></h1>
          </div>
          <p className="text-gray-300 text-lg mb-12 max-w-md">Admin Console. Securely manage nationwide logistics, track fleet performance, and oversee system operations.</p>
          
          <img 
            src="/delivery-hero.png" 
            alt="Delivery Hero" 
            className="w-full max-w-sm h-auto object-contain hover:scale-105 transition-transform duration-700 pointer-events-none rounded-[60px] mb-8 animate-bulge"
            style={{ 
              maskImage: 'radial-gradient(circle, black 60%, transparent 100%)',
              WebkitMaskImage: 'radial-gradient(circle, black 60%, transparent 100%)'
            }}
          />
        </div>
      </div>

      {/* Right Pane - Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center p-8 sm:p-12 lg:p-24 relative">
        <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0 lg:hidden">
          <div className="absolute -top-[10%] -left-[10%] w-[50%] h-[50%] bg-blue-900/5 rounded-full blur-[100px]"></div>
        </div>

        <div className="w-full max-w-md relative z-10">
          <div className="mb-10 text-center lg:text-left">
            <div className="inline-flex items-center gap-2 bg-blue-50 text-blue-900 px-4 py-2 rounded-full text-xs font-black uppercase tracking-widest mb-6 lg:hidden">
              <ShieldCheck className="w-4 h-4" /> Admin Portal
            </div>
            <h2 className="text-3xl lg:text-4xl font-black text-[#071a2a] mb-3">System <span className="text-yellow-500">Access</span></h2>
            <p className="text-gray-500 font-medium">Securely sign in to your administrative dashboard.</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-6">
            <div>
              <label className="block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest">Admin Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]"
                placeholder="admin@smartcourier.com"
              />
            </div>
            <div>
              <label className="block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-6 py-4 pr-12 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-yellow-500 transition-colors"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-yellow-500 text-blue-900 font-black py-5 rounded-2xl shadow-xl hover:bg-yellow-400 hover:-translate-y-1 transition-all flex items-center justify-center gap-3 text-lg disabled:opacity-50 mt-8"
            >
              {loading ? (
                <span className="flex items-center gap-3">
                  <div className="w-6 h-6 border-4 border-blue-900 border-t-transparent rounded-full animate-spin"></div>
                  Authenticating...
                </span>
              ) : (
                <>
                  Secure Login
                  <ArrowRight className="w-6 h-6" />
                </>
              )}
            </button>

            <div className="text-center mt-4">
              <Link to="/forgot-password" className="text-sm text-gray-400 hover:text-yellow-500 font-semibold transition-colors">
                Forgot your password?
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
