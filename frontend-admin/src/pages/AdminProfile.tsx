import React, { useState, useEffect } from "react";
import { Camera, User, Phone, Crown, Shield, Lock, MapPin, Save, Eye, EyeOff, Trash } from "lucide-react";
import toast from "react-hot-toast";
import api from "../services/api";
import { useAuth } from "../contexts/AuthContext";
import LoadingSpinner from "../components/LoadingSpinner";

interface ProfileData {
  id: number;
  username: string;
  email: string;
  role: string;
  phone: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  profileImagePath: string;
}

const AdminProfile = () => {
  const { user, isSuperAdmin, refreshUser } = useAuth();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    username: "",
    phone: "",
    street: "",
    city: "",
    state: "",
    zipCode: "",
  });

  // Change Password state
  const [pwData, setPwData] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [pwLoading, setPwLoading] = useState(false);
  const [showOld, setShowOld] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [imageLoading, setImageLoading] = useState(false);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await api.get("/auth/profile");
      const data = res.data?.data ?? res.data;
      setProfile(data);
      setForm({
        username: data.username || "",
        phone: data.phone || "",
        street: data.street || "",
        city: data.city || "",
        state: data.state || "",
        zipCode: data.zipCode || "",
      });
    } catch (err) {
      toast.error("Failed to load profile");
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.put("/auth/profile", form);
      toast.success("Profile updated successfully");
      refreshUser();
      fetchProfile();
    } catch (err) {
      toast.error("Failed to update profile");
    } finally {
      setSaving(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!pwData.oldPassword || !pwData.newPassword) {
      toast.error("All fields are required.");
      return;
    }
    if (pwData.newPassword.length < 6) {
      toast.error("New password must be at least 6 characters.");
      return;
    }
    if (pwData.newPassword !== pwData.confirmPassword) {
      toast.error("New passwords do not match.");
      return;
    }
    setPwLoading(true);
    try {
      await api.put("/auth/change-password", {
        oldPassword: pwData.oldPassword,
        newPassword: pwData.newPassword,
      });
      toast.success("Password changed successfully!");
      setPwData({ oldPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to change password.");
    } finally {
      setPwLoading(false);
    }
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImageLoading(true);
    const formData = new FormData();
    formData.append("file", file);

    try {
      await api.post("/auth/profile/image", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      toast.success("Profile image updated!");
      refreshUser();
      await fetchProfile();
    } catch (err) {
      toast.error("Failed to upload image");
    } finally {
      setImageLoading(false);
    }
  };

  const handleImageDelete = async () => {
    setImageLoading(true);
    try {
      await api.delete("/auth/profile/image");
      toast.success("Profile image deleted!");
      refreshUser();
      await fetchProfile();
    } catch (err) {
      toast.error("Failed to delete image");
    } finally {
      setImageLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading Profile..." />;
  }

  return (
    <div className='max-w-3xl mx-auto space-y-8 animate-fade-in'>
      {/* Profile Header */}
      <div className='bg-white rounded-[32px] p-10 relative overflow-hidden shadow-xl border border-gray-100'>
        <div className='absolute top-0 right-0 w-48 h-48 bg-yellow-500 rounded-full filter blur-[120px] opacity-10'></div>
        <div className='flex items-center gap-8 relative z-10'>
          {/* Avatar */}
          <div className='relative group'>
            <div className='w-24 h-24 rounded-2xl bg-yellow-500 flex items-center justify-center text-blue-900 font-black text-3xl shadow-2xl overflow-hidden'>
              {imageLoading ? (
                <div className='relative w-12 h-12'>
                  <div className='absolute inset-0 border-4 border-yellow-600/20 rounded-xl rotate-45'></div>
                  <div className='absolute inset-0 border-4 border-blue-900 border-t-transparent rounded-xl animate-spin rotate-45'></div>
                </div>
              ) : profile?.profileImagePath ? (
                <img
                  src={`http://localhost:8080/auth/profile/image/${profile.profileImagePath}`}
                  alt='Profile'
                  className='w-full h-full object-cover'
                />
              ) : (
                profile?.username?.charAt(0)?.toUpperCase() || "?"
              )}
            </div>
            {!imageLoading && (
              <div className='absolute inset-0 flex items-center justify-center gap-2 bg-black/50 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity'>
                <label className='cursor-pointer p-2 hover:bg-white/20 rounded-full transition-colors'>
                  <Camera className='w-5 h-5 text-white' />
                  <input
                    type='file'
                    accept='image/*'
                    className='hidden'
                    onChange={handleImageUpload}
                  />
                </label>
                {profile?.profileImagePath && (
                  <button type="button" onClick={handleImageDelete} className='p-2 text-red-400 hover:text-red-300 hover:bg-white/20 rounded-full transition-colors'>
                    <Trash className='w-5 h-5' />
                  </button>
                )}
              </div>
            )}
          </div>

          <div>
            <h1 className='text-3xl font-black tracking-tight text-blue-900'>
              {profile?.username}
            </h1>
            <p className='text-gray-500 font-bold mt-1'>{profile?.email}</p>
            <div className='mt-3'>
              {isSuperAdmin ? (
                <span className='inline-flex items-center gap-1.5 bg-yellow-500/20 border border-yellow-500/40 text-yellow-400 px-3 py-1 rounded-full text-xs font-black tracking-widest'>
                  <Crown className='w-3.5 h-3.5' /> SUPER ADMIN
                </span>
              ) : (
                <span className='inline-flex items-center gap-1.5 bg-blue-500/20 border border-blue-500/40 text-blue-300 px-3 py-1 rounded-full text-xs font-black tracking-widest'>
                  <Shield className='w-3.5 h-3.5' /> ADMIN
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Edit Form */}
      <div className='bg-white rounded-[32px] shadow-xl border border-gray-100 p-10'>
        <h2 className='text-xl font-black text-blue-900 mb-8 flex items-center gap-3'>
          <div className='w-10 h-10 bg-yellow-100 rounded-xl flex items-center justify-center'>
            <User className='w-5 h-5 text-yellow-600' />
          </div>
          Personal Information
        </h2>

        <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
          <div className='md:col-span-2'>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              Display Name
            </label>
            <input
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              className='w-full px-5 py-4 bg-gray-50 rounded-2xl border-2 border-gray-100 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all'
            />
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              Phone
            </label>
            <div className='relative'>
              <Phone className='absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400' />
              <input
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
                className='w-full px-5 py-4 pl-12 bg-gray-50 rounded-2xl border-2 border-gray-100 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all'
                placeholder='+91 9876543210'
              />
            </div>
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              Street
            </label>
            <input
              value={form.street}
              onChange={(e) => setForm({ ...form, street: e.target.value })}
              className='w-full px-5 py-4 bg-gray-50 rounded-2xl border-2 border-gray-100 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all'
              placeholder='123 Main Street'
            />
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              City
            </label>
            <div className='relative'>
              <MapPin className='absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400' />
              <input
                value={form.city}
                onChange={(e) => setForm({ ...form, city: e.target.value })}
                className='w-full px-5 py-4 pl-12 bg-gray-50 rounded-2xl border-2 border-gray-100 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all'
                placeholder='Bangalore'
              />
            </div>
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              State
            </label>
            <input
              value={form.state}
              onChange={(e) => setForm({ ...form, state: e.target.value })}
              className='w-full px-5 py-4 bg-gray-50 rounded-2xl border-2 border-gray-100 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all'
              placeholder='Karnataka'
            />
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              Zip Code
            </label>
            <input
              value={form.zipCode}
              onChange={(e) => setForm({ ...form, zipCode: e.target.value })}
              className='w-full px-5 py-4 bg-gray-50 rounded-2xl border-2 border-gray-100 font-bold text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all'
              placeholder='560001'
            />
          </div>
        </div>

        <button
          onClick={handleSave}
          disabled={saving}
          className='mt-10 w-full bg-yellow-500 text-[#071a2a] font-black py-4 rounded-2xl shadow-xl hover:bg-yellow-400 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-3 text-lg disabled:opacity-50'
        >
          <Save className='w-5 h-5' />
          {saving ? "Saving Changes..." : "Save Profile"}
        </button>
      </div>

      {/* Change Password Card */}
      <div className='bg-white rounded-[32px] shadow-xl border border-gray-100 p-10'>
        <h2 className='text-xl font-black text-blue-900 mb-8 flex items-center gap-3'>
          <div className='w-10 h-10 bg-red-50 rounded-xl flex items-center justify-center'>
            <Lock className='w-5 h-5 text-red-500' />
          </div>
          Change Password
        </h2>

        <form onSubmit={handleChangePassword} className='space-y-6'>
          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              Current Password
            </label>
            <div className='relative'>
              <Lock className='absolute left-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400' />
              <input
                type={showOld ? "text" : "password"}
                value={pwData.oldPassword}
                onChange={(e) =>
                  setPwData({ ...pwData, oldPassword: e.target.value })
                }
                placeholder='••••••••'
                className='w-full bg-gray-50 border-2 border-gray-200 text-gray-800 text-lg rounded-2xl focus:ring-0 focus:border-yellow-500 focus:bg-white pl-12 pr-12 py-4 transition-all font-bold outline-none'
              />
              <button
                type='button'
                onClick={() => setShowOld(!showOld)}
                className='absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-yellow-500 transition-colors'
              >
                {showOld ? (
                  <EyeOff className='w-5 h-5' />
                ) : (
                  <Eye className='w-5 h-5' />
                )}
              </button>
            </div>
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              New Password
            </label>
            <div className='relative'>
              <Lock className='absolute left-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400' />
              <input
                type={showNew ? "text" : "password"}
                value={pwData.newPassword}
                onChange={(e) =>
                  setPwData({ ...pwData, newPassword: e.target.value })
                }
                placeholder='••••••••'
                className='w-full bg-gray-50 border-2 border-gray-200 text-gray-800 text-lg rounded-2xl focus:ring-0 focus:border-yellow-500 focus:bg-white pl-12 pr-12 py-4 transition-all font-bold outline-none'
              />
              <button
                type='button'
                onClick={() => setShowNew(!showNew)}
                className='absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-yellow-500 transition-colors'
              >
                {showNew ? (
                  <EyeOff className='w-5 h-5' />
                ) : (
                  <Eye className='w-5 h-5' />
                )}
              </button>
            </div>
          </div>

          <div>
            <label className='block text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>
              Confirm New Password
            </label>
            <div className='relative'>
              <Lock className='absolute left-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400' />
              <input
                type={showNew ? "text" : "password"}
                value={pwData.confirmPassword}
                onChange={(e) =>
                  setPwData({ ...pwData, confirmPassword: e.target.value })
                }
                placeholder='••••••••'
                className='w-full bg-gray-50 border-2 border-gray-200 text-gray-800 text-lg rounded-2xl focus:ring-0 focus:border-yellow-500 focus:bg-white pl-12 py-4 transition-all font-bold outline-none'
              />
            </div>
          </div>

          <button
            type='submit'
            disabled={pwLoading}
            className='w-full flex items-center justify-center gap-3 bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-4 rounded-2xl transition-all duration-300 shadow-xl hover:-translate-y-0.5 mt-4 text-lg disabled:opacity-50'
          >
            {pwLoading ? (
              <div className='w-5 h-5 border-4 border-white border-t-transparent rounded-full animate-spin'></div>
            ) : (
              <Lock className='w-5 h-5' />
            )}
            Update Password
          </button>
        </form>
      </div>
    </div>
  );
};

export default AdminProfile;
