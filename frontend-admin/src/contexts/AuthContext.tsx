import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../services/api';

interface User {
  id: number;
  email: string;
  role: string;
  username: string;
  profileImagePath?: string;
}

interface AuthContextType {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isSuperAdmin: boolean;
  login: (newToken: string) => void;
  logout: (showToast?: boolean) => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const parseJwt = (token: string): any => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

const extractUser = (token: string): User | null => {
  const decoded = parseJwt(token);
  if (!decoded) return null;
  return {
    id: decoded.userId || decoded.id,
    email: decoded.sub || decoded.email,
    role: decoded.role || decoded.roles?.[0] || 'ROLE_ADMIN',
    username: decoded.username || decoded.name || 'Admin',
  };
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('admin_token'));
  const [user, setUser] = useState<User | null>(() => {
    const savedToken = localStorage.getItem('admin_token');
    return savedToken ? extractUser(savedToken) : null;
  });
  const navigate = useNavigate();

  const isSuperAdmin = user?.role === 'ROLE_SUPER_ADMIN';

  const login = (newToken: string) => {
    localStorage.setItem('admin_token', newToken);
    setToken(newToken);
    setUser(extractUser(newToken));
  };

  const logout = (showToast = true) => {
    localStorage.removeItem('admin_token');
    setToken(null);
    setUser(null);
    if (showToast) {
      toast.success('Logged out securely');
    }
    if (window.location.pathname !== '/login') {
      navigate('/login');
    }
  };

  const refreshUser = async () => {
    if (!token) return;
    try {
      const res = await api.get('/auth/profile');
      const data = res.data?.data || res.data;
      setUser(prev => prev ? {
        ...prev,
        username: data.username,
        profileImagePath: data.profileImagePath
      } : extractUser(token));
    } catch (err) {
      console.error('Failed to refresh user profile:', err);
    }
  };

  useEffect(() => {
    if (token) {
      refreshUser();
    }
  }, [token]);

  useEffect(() => {
    const responseInterceptor = api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response && error.response.status === 401) {
          const currentToken = localStorage.getItem('admin_token');
          const isLoginRequest = error.config.url.includes('/auth/login');
          if (currentToken && !isLoginRequest) {
            toast.error('Session expired. Please log in again.');
            logout(false);
          }
        }
        return Promise.reject(error);
      }
    );

    return () => {
      api.interceptors.response.eject(responseInterceptor);
    };
  }, [navigate]);

  return (
    <AuthContext.Provider value={{ token, user, isAuthenticated: !!token, isSuperAdmin, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
