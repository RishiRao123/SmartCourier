import React, { useState } from "react";
import {
  UserPlus,
  Shield,
  Lock,
  Mail,
  User,
  AlertCircle,
  CheckCircle2
} from "lucide-react";
import toast from "react-hot-toast";
import api from "../services/api";

const Settings = () => {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
  });
  const [loading, setLoading] = useState(false);



  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleAdminSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.username || !formData.email) {
      toast.error("Username and email are required.");
      return;
    }

    setLoading(true);
    try {
      await api.post("/auth-admin/signup", {
        username: formData.username,
        email: formData.email,
        password: "auto-generated-password" // Dummy password to bypass DTO validation
      });
      toast.success("New Administrator created successfully! Credentials sent via email.");
      setFormData({ username: "", email: "" });
    } catch (err: any) {
      toast.error(
        err.response?.data?.message || "Failed to create new Admin account.",
      );
      console.error(err);
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className='space-y-10 animate-fade-in-up'>
      <div className='max-w-2xl mx-auto'>
        {/* Create Admin Form */}
        <div className='bg-white rounded-3xl shadow-xl border border-gray-100 p-8'>
          <div className='flex items-center gap-3 mb-8 pb-6 border-b border-gray-100'>
            <div className='p-3 bg-blue-50 text-blue-600 rounded-xl'>
              <UserPlus className='w-6 h-6' />
            </div>
            <div>
              <h3 className='text-2xl font-black text-blue-900'>
                Register New Admin
              </h3>
              <p className='text-gray-500 text-sm font-semibold'>
                Grant full system access to a new user.
              </p>
            </div>
          </div>

          <form onSubmit={handleAdminSignup} className='space-y-6'>
            <div>
              <label className='block text-sm font-black text-gray-700 uppercase tracking-widest mb-2'>
                Username
              </label>
              <div className='relative'>
                <User className='absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400' />
                <input
                  type='text'
                  name='username'
                  value={formData.username}
                  onChange={handleChange}
                  placeholder='e.g. rishi rao'
                  className='w-full bg-gray-50 border-2 border-gray-200 text-gray-800 text-lg rounded-2xl focus:ring-0 focus:border-yellow-500 focus:bg-white pl-12 p-4 transition-all font-bold'
                />
              </div>
            </div>

            <div>
              <label className='block text-sm font-black text-gray-700 uppercase tracking-widest mb-2'>
                Email Address
              </label>
              <div className='relative'>
                <Mail className='absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400' />
                <input
                  type='email'
                  name='email'
                  value={formData.email}
                  onChange={handleChange}
                  placeholder='admin@smartcourier.com'
                  className='w-full bg-gray-50 border-2 border-gray-200 text-gray-800 text-lg rounded-2xl focus:ring-0 focus:border-yellow-500 focus:bg-white pl-12 p-4 transition-all font-bold'
                />
              </div>
            </div>

            <div className="bg-yellow-50 rounded-2xl p-4 border border-yellow-100 flex items-start gap-3">
              <Mail className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
              <p className="text-sm font-semibold text-yellow-800">
                A secure, auto-generated password will be emailed to this address. The new admin must use it to log in.
              </p>
            </div>

            <button
              type='submit'
              disabled={loading}
              className='w-full flex items-center justify-center gap-3 bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black text-xl py-5 rounded-2xl transition-all duration-300 shadow-xl hover:-translate-y-1 mt-4 disabled:opacity-70 disabled:hover:translate-y-0'
            >
              {loading ? (
                <div className='w-6 h-6 border-4 border-yellow-500 border-t-transparent rounded-full animate-spin'></div>
              ) : (
                <CheckCircle2 className='w-6 h-6' />
              )}
              Authorize New Admin
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Settings;
