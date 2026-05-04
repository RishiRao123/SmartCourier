import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { User, Mail, Phone, MapPin, Save, Camera, ArrowLeft, CheckCircle, Trash } from 'lucide-react';
import { profileService } from '../services/api';
import type { UserProfile } from '../services/api';
import toast from 'react-hot-toast';
import CustomDropdown from '../components/Shared/CustomDropdown';
import LoadingSpinner from '../components/LoadingSpinner';

const INDIAN_STATES = [
  { value: "Andhra Pradesh", label: "Andhra Pradesh" },
  { value: "Arunachal Pradesh", label: "Arunachal Pradesh" },
  { value: "Assam", label: "Assam" },
  { value: "Bihar", label: "Bihar" },
  { value: "Chhattisgarh", label: "Chhattisgarh" }, 
  { value: "Goa", label: "Goa" },
  { value: "Gujarat", label: "Gujarat" },
  { value: "Haryana", label: "Haryana" },
  { value: "Himachal Pradesh", label: "Himachal Pradesh" },
  { value: "Jharkhand", label: "Jharkhand" }, 
  { value: "Karnataka", label: "Karnataka" },
  { value: "Kerala", label: "Kerala" },
  { value: "Madhya Pradesh", label: "Madhya Pradesh" },
  { value: "Maharashtra", label: "Maharashtra" },
  { value: "Manipur", label: "Manipur" }, 
  { value: "Meghalaya", label: "Meghalaya" },
  { value: "Mizoram", label: "Mizoram" },
  { value: "Nagaland", label: "Nagaland" },
  { value: "Odisha", label: "Odisha" },
  { value: "Punjab", label: "Punjab" }, 
  { value: "Rajasthan", label: "Rajasthan" },
  { value: "Sikkim", label: "Sikkim" },
  { value: "Tamil Nadu", label: "Tamil Nadu" },
  { value: "Telangana", label: "Telangana" },
  { value: "Tripura", label: "Tripura" }, 
  { value: "Uttar Pradesh", label: "Uttar Pradesh" },
  { value: "Uttarakhand", label: "Uttarakhand" },
  { value: "West Bengal", label: "West Bengal" },
  { value: "Delhi", label: "Delhi" },
  { value: "Chandigarh", label: "Chandigarh" }
];

const Profile: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [form, setForm] = useState({
    username: '',
    phone: '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
  });
  const [imageLoading, setImageLoading] = useState(false);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const data = await profileService.getProfile();
      setProfile(data);
      setForm({
        username: data.username || '',
        phone: data.phone || '',
        street: data.street || '',
        city: data.city || '',
        state: data.state || '',
        zipCode: data.zipCode || '',
      });
    } catch (err) {
      toast.error('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await profileService.updateProfile(form);
      toast.success('Profile updated successfully!');
      fetchProfile();
    } catch (err) {
      toast.error('Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImageLoading(true);
    try {
      await profileService.uploadProfileImage(file);
      toast.success('Profile photo updated!');
      await fetchProfile();
    } catch (err) {
      toast.error('Failed to upload photo');
    } finally {
      setImageLoading(false);
    }
  };

  const handleImageDelete = async () => {
    setImageLoading(true);
    try {
      await profileService.deleteProfileImage();
      toast.success('Profile photo deleted!');
      await fetchProfile();
    } catch (err) {
      toast.error('Failed to delete photo');
    } finally {
      setImageLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <LoadingSpinner message="Getting your details..." />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <button 
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-secondary-900 font-bold mb-8 hover:text-primary-500 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" /> Back
        </button>

        {/* Profile Header Card */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-secondary-900 rounded-3xl p-10 text-white relative overflow-hidden shadow-2xl mb-8"
        >
          <div className="absolute top-0 right-0 w-48 h-48 bg-primary-500 rounded-full filter blur-[80px] opacity-20"></div>
          <div className="flex items-center gap-8 relative z-10">
            <div className="relative group">
              <div className="w-24 h-24 rounded-2xl bg-primary-500 flex items-center justify-center text-secondary-900 font-black text-3xl shadow-2xl overflow-hidden">
                {imageLoading ? (
                  <div className='relative w-12 h-12'>
                    <div className='absolute inset-0 border-4 border-secondary-900/10 rounded-xl rotate-45'></div>
                    <div className='absolute inset-0 border-4 border-secondary-900 border-t-transparent rounded-xl animate-spin rotate-45'></div>
                  </div>
                ) : profile?.profileImagePath ? (
                  <img
                    src={`http://localhost:8080/auth/profile/image/${profile.profileImagePath}`}
                    alt="Profile"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  profile?.username?.charAt(0)?.toUpperCase() || '?'
                )}
              </div>
              {!imageLoading && (
                <div className="absolute inset-0 flex items-center justify-center gap-2 bg-black/50 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity">
                  <label className="cursor-pointer p-2 hover:bg-white/20 rounded-full transition-colors">
                    <Camera className="w-5 h-5 text-white" />
                    <input type="file" accept="image/*" className="hidden" onChange={handleImageUpload} />
                  </label>
                  {profile?.profileImagePath && (
                    <button onClick={handleImageDelete} className="p-2 text-red-400 hover:text-red-300 hover:bg-white/20 rounded-full transition-colors">
                      <Trash className="w-5 h-5" />
                    </button>
                  )}
                </div>
              )}
            </div>
            <div>
              <h1 className="text-3xl font-black tracking-tight">{profile?.username}</h1>
              <p className="text-gray-400 font-bold mt-1 flex items-center gap-2">
                <Mail className="w-4 h-4" /> {profile?.email}
              </p>
              <div className="mt-3 inline-flex items-center gap-1.5 bg-primary-500/20 border border-primary-500/40 text-primary-500 px-3 py-1 rounded-full text-xs font-black tracking-widest">
                <CheckCircle className="w-3.5 h-3.5" /> VERIFIED CUSTOMER
              </div>
            </div>
          </div>
        </motion.div>

        {/* Edit Form */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-3xl p-10 shadow-sm border border-gray-100"
        >
          <h2 className="text-xl font-bold text-secondary-900 mb-8 flex items-center gap-3">
            <div className="w-10 h-10 bg-primary-500/10 rounded-xl flex items-center justify-center text-primary-500">
              <User className="w-5 h-5" />
            </div>
            Personal Information
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="md:col-span-2">
              <label className="block text-sm font-bold text-gray-700 mb-2">Display Name</label>
              <input
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                className="w-full px-5 py-4 bg-gray-50 rounded-xl border border-gray-100 focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all font-bold"
              />
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Phone</label>
              <div className="relative">
                <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  className="w-full px-5 py-4 pl-12 bg-gray-50 rounded-xl border border-gray-100 focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all font-bold"
                  placeholder="+91 9876543210"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Street</label>
              <input
                value={form.street}
                onChange={(e) => setForm({ ...form, street: e.target.value })}
                className="w-full px-5 py-4 bg-gray-50 rounded-xl border border-gray-100 focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all font-bold"
                placeholder="123 Main Street"
              />
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">City</label>
              <div className="relative">
                <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  value={form.city}
                  onChange={(e) => setForm({ ...form, city: e.target.value })}
                  className="w-full px-5 py-4 pl-12 bg-gray-50 rounded-xl border border-gray-100 focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all font-bold"
                  placeholder="Bangalore"
                />
              </div>
            </div>

            <div>
              <CustomDropdown 
                label="State" 
                options={INDIAN_STATES} 
                value={form.state}
                onChange={(val) => setForm({ ...form, state: val })} 
                placeholder="Select State" 
              />
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-2">Zip Code</label>
              <input
                value={form.zipCode}
                onChange={(e) => setForm({ ...form, zipCode: e.target.value })}
                className="w-full px-5 py-4 bg-gray-50 rounded-xl border border-gray-100 focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all font-bold"
                placeholder="560001"
              />
            </div>
          </div>

          <button
            onClick={handleSave}
            disabled={saving}
            className="mt-10 w-full bg-primary-500 text-secondary-900 font-black py-5 rounded-2xl shadow-xl hover:bg-yellow-400 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-3 text-lg disabled:opacity-50"
          >
            <Save className="w-5 h-5" />
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </motion.div>
      </div>
    </div>
  );
};

export default Profile;
