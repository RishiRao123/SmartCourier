import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, MapPin, Box, ArrowRight, ArrowLeft, IndianRupee, CreditCard, Banknote, AlertTriangle } from 'lucide-react';
import { deliveryService, profileService } from '../services/api';
import toast from 'react-hot-toast';
import LoadingSpinner from '../components/LoadingSpinner';
import CustomDatePicker from '../components/Shared/CustomDatePicker';
import CustomDropdown from '../components/Shared/CustomDropdown';
import PaymentModal from '../components/Shared/PaymentModal';

import { useCreateShipment } from '../hooks/useCreateShipment';
 
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
 
const CreateShipment: React.FC = () => {
  const navigate = useNavigate();
  const {
    formData,
    loading,
    price,
    showConfirmModal,
    setShowConfirmModal,
    showPaymentModal,
    setShowPaymentModal,
    handleChange,
    handleAddressChange,
    processBooking
  } = useCreateShipment();
 
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneRegex.test(formData.receiverPhone)) {
      toast.error('Please enter a valid 10-digit phone number');
      return;
    }
    setShowConfirmModal(true);
  };
 
  const handleConfirmBooking = () => {
    setShowConfirmModal(false);
    if (formData.paymentMethod === 'PAY_NOW') {
      setShowPaymentModal(true);
      return;
    }
    processBooking();
  };

  return (
    <div className="min-h-screen bg-white py-12 selection:bg-yellow-500 relative">
      {loading && (
        <div className="fixed inset-0 z-[100] bg-white/80 backdrop-blur-sm flex items-center justify-center">
          <LoadingSpinner message="Preparing your box..." />
        </div>
      )}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <button 
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-[#071a2a] font-bold mb-8 hover:text-yellow-500 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" /> Back to Dashboard
        </button>

        <div className="mb-12 flex justify-between items-end">
          <div>
            <h1 className="text-4xl font-black text-[#071a2a] mb-2 underline decoration-yellow-500 decoration-8 underline-offset-4">Book Shipment</h1>
            <p className="text-gray-500 font-medium">Professional logistics solutions across India.</p>
          </div>
          {price > 0 && (
            <div className="hidden md:block">
              <div className="bg-yellow-500/10 border-2 border-yellow-500 p-4 rounded-3xl text-center min-w-[120px]">
                <p className="text-[10px] font-black uppercase tracking-widest text-yellow-600 mb-1">Estimated</p>
                <p className="text-3xl font-black text-[#071a2a] flex items-center justify-center">
                  <IndianRupee className="w-5 h-5" /> {price}
                </p>
              </div>
            </div>
          )}
        </div>

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Sender Details */}
          <div className="bg-white rounded-[40px] p-8 border-4 border-gray-50 shadow-sm hover:border-yellow-500/20 transition-all">
            <div className="flex items-center gap-4 mb-8">
              <div className="w-14 h-14 bg-yellow-500 rounded-2xl flex items-center justify-center shadow-lg text-[#071a2a]">
                <User className="w-7 h-7" />
              </div>
              <h2 className="text-2xl font-black text-[#071a2a]">Sender Info</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="md:col-span-2">
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Full Name</label>
                <input required type="text" value={formData.senderName}
                  onChange={(e) => handleChange('senderName', '', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="Your Name" />
              </div>
              <div className="md:col-span-2">
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Street Address</label>
                <input required type="text" value={formData.senderAddress.street}
                  onChange={(e) => handleAddressChange('senderAddress', 'street', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="Flat, House, Area" />
              </div>
              <div>
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">City</label>
                <input required type="text" value={formData.senderAddress.city}
                  onChange={(e) => handleAddressChange('senderAddress', 'city', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="City" />
              </div>
              <CustomDropdown label="State" options={INDIAN_STATES} value={formData.senderAddress.state}
                onChange={(val) => handleAddressChange('senderAddress', 'state', val)} placeholder="Select State" />
            </div>
          </div>

          {/* Receiver Details */}
          <div className="bg-white rounded-[40px] p-8 border-4 border-gray-50 shadow-sm hover:border-yellow-500/20 transition-all">
            <div className="flex items-center gap-4 mb-8">
              <div className="w-14 h-14 bg-[#071a2a] rounded-2xl flex items-center justify-center shadow-lg text-white">
                <MapPin className="w-7 h-7" />
              </div>
              <h2 className="text-2xl font-black text-[#071a2a]">Receiver Info</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Full Name</label>
                <input required type="text" value={formData.receiverName}
                  onChange={(e) => handleChange('receiverName', '', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="Recipient Name" />
              </div>
              <div>
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Phone Number</label>
                <input required type="tel" value={formData.receiverPhone}
                  onChange={(e) => handleChange('receiverPhone', '', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="+91 9876543210" />
              </div>
              <div className="md:col-span-2">
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Street Address</label>
                <input required type="text" value={formData.receiverAddress.street}
                  onChange={(e) => handleAddressChange('receiverAddress', 'street', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="Flat, House, Area" />
              </div>
              <div>
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">City</label>
                <input required type="text" value={formData.receiverAddress.city}
                  onChange={(e) => handleAddressChange('receiverAddress', 'city', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="City" />
              </div>
              <CustomDropdown label="State" options={INDIAN_STATES} value={formData.receiverAddress.state}
                onChange={(val) => handleAddressChange('receiverAddress', 'state', val)} placeholder="Select State" />
            </div>
          </div>

          {/* Package Details */}
          <div className="bg-white rounded-[40px] p-8 border-4 border-gray-50 shadow-sm hover:border-yellow-500/20 transition-all">
            <div className="flex items-center gap-4 mb-8">
              <div className="w-14 h-14 bg-yellow-500 rounded-2xl flex items-center justify-center shadow-lg text-[#071a2a]">
                <Box className="w-7 h-7" />
              </div>
              <h2 className="text-2xl font-black text-[#071a2a]">Package & Schedule</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Weight (kg)</label>
                <input required type="number" step="0.1" value={formData.packageDetails.weight}
                  onChange={(e) => handleChange('packageDetails', 'weight', parseFloat(e.target.value))}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="0.0" />
              </div>
              <CustomDatePicker label="Pickup Date" value={formData.pickupDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={(val) => handleChange('pickupDate', '', val)} />
              <div className="md:col-span-2">
                <label className="block text-[10px] font-black text-gray-400 mb-2 uppercase tracking-[0.2em]">Item Description</label>
                <input required type="text" value={formData.packageDetails.description}
                  onChange={(e) => handleChange('packageDetails', 'description', e.target.value)}
                  className="w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white transition-all font-bold outline-none"
                  placeholder="What's inside?" />
              </div>
            </div>
          </div>

          {/* Payment Method */}
          <div className="bg-white rounded-[40px] p-8 border-4 border-gray-50 shadow-sm hover:border-yellow-500/20 transition-all">
            <div className="flex items-center gap-4 mb-8">
              <div className="w-14 h-14 bg-[#071a2a] rounded-2xl flex items-center justify-center shadow-lg text-yellow-500">
                <CreditCard className="w-7 h-7" />
              </div>
              <h2 className="text-2xl font-black text-[#071a2a]">Payment Method</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div
                onClick={() => handleChange('paymentMethod', '', 'PAY_ON_DELIVERY')}
                className={`p-6 rounded-3xl border-4 cursor-pointer transition-all flex items-center gap-5 ${
                  formData.paymentMethod === 'PAY_ON_DELIVERY'
                    ? 'border-yellow-500 bg-yellow-50 shadow-lg'
                    : 'border-gray-100 hover:border-gray-200'
                }`}
              >
                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center flex-shrink-0 ${
                  formData.paymentMethod === 'PAY_ON_DELIVERY' ? 'bg-yellow-500 text-[#071a2a]' : 'bg-gray-100 text-gray-400'
                }`}>
                  <Banknote className="w-7 h-7" />
                </div>
                <div>
                  <p className="font-black text-[#071a2a] text-lg">Pay on Delivery</p>
                  <p className="text-gray-400 text-sm font-medium">Pay cash when package arrives</p>
                </div>
              </div>
              <div
                onClick={() => handleChange('paymentMethod', '', 'PAY_NOW')}
                className={`p-6 rounded-3xl border-4 cursor-pointer transition-all flex items-center gap-5 ${
                  formData.paymentMethod === 'PAY_NOW'
                    ? 'border-yellow-500 bg-yellow-50 shadow-lg'
                    : 'border-gray-100 hover:border-gray-200'
                }`}
              >
                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center flex-shrink-0 ${
                  formData.paymentMethod === 'PAY_NOW' ? 'bg-yellow-500 text-[#071a2a]' : 'bg-gray-100 text-gray-400'
                }`}>
                  <CreditCard className="w-7 h-7" />
                </div>
                <div>
                  <p className="font-black text-[#071a2a] text-lg">Pay Now</p>
                  <p className="text-gray-400 text-sm font-medium">Online payment (Razorpay)</p>
                </div>
              </div>
            </div>
          </div>

          {/* Mobile price preview */}
          {price > 0 && (
            <div className="md:hidden bg-yellow-500/10 border-2 border-yellow-500 text-[#071a2a] p-6 rounded-3xl text-center">
              <p className="text-xs font-black uppercase tracking-widest mb-1 text-yellow-600">Estimated Total</p>
              <p className="text-4xl font-black flex items-center justify-center">
                <IndianRupee className="w-6 h-6" /> {price}
              </p>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-yellow-500 text-[#071a2a] font-black py-5 rounded-3xl shadow-xl hover:bg-yellow-400 hover:-translate-y-1 transition-all flex items-center justify-center gap-3 text-xl disabled:opacity-50 max-w-xl mx-auto"
          >
            {loading ? 'Processing...' : formData.paymentMethod === 'PAY_NOW' ? 'Proceed to Payment' : 'Confirm Booking'}
            {!loading && <ArrowRight className="w-6 h-6" />}
          </button>
        </form>
      </div>

      {/* Payment Modal */}
      <PaymentModal
        isOpen={showPaymentModal}
        amount={price}
        onConfirm={processBooking}
        onClose={() => setShowPaymentModal(false)}
        loading={loading}
      />

      {/* Confirmation Modal */}
      {showConfirmModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-[#071a2a]/60 backdrop-blur-md transition-all duration-500" onClick={() => setShowConfirmModal(false)}></div>
          <div className="bg-white rounded-[40px] w-full max-w-lg shadow-2xl relative z-10 overflow-hidden animate-in fade-in zoom-in duration-300 border border-white/20">
            <div className="bg-yellow-500 p-10 text-center relative overflow-hidden">
              <div className="absolute -top-10 -right-10 w-40 h-40 bg-white/20 rounded-full blur-3xl"></div>
              <div className="absolute -bottom-10 -left-10 w-40 h-40 bg-[#071a2a]/10 rounded-full blur-2xl"></div>
              
              <div className="w-20 h-20 bg-white rounded-3xl mx-auto flex items-center justify-center shadow-2xl mb-6 transform -rotate-6 hover:rotate-0 transition-transform duration-500">
                <AlertTriangle className="w-10 h-10 text-yellow-600" />
              </div>
              <h2 className="text-3xl font-black text-[#071a2a] mb-2 tracking-tighter">Final Review</h2>
              <p className="text-[#071a2a]/70 font-bold uppercase tracking-widest text-[10px]">One last step before we fly</p>
            </div>
            
            <div className="p-10">
              <div className="bg-gray-50 rounded-3xl p-6 mb-8 border border-gray-100">
                <p className="text-gray-600 font-bold text-center leading-relaxed">
                  Are you sure you want to book this shipment? 
                  <span className="block mt-2 text-[#071a2a] font-black italic underline decoration-yellow-500/50 decoration-4">
                    Changes or deletions are not allowed after this point.
                  </span>
                </p>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <button
                  onClick={() => setShowConfirmModal(false)}
                  className="px-6 py-5 rounded-2xl font-black text-gray-400 hover:text-[#071a2a] hover:bg-gray-100 transition-all uppercase tracking-widest text-xs"
                >
                  Go Back
                </button>
                <button
                  onClick={handleConfirmBooking}
                  className="bg-[#071a2a] text-yellow-500 font-black px-6 py-5 rounded-2xl shadow-xl hover:bg-[#0c2d48] hover:shadow-yellow-500/20 transition-all uppercase tracking-widest text-xs flex items-center justify-center gap-2 group"
                >
                  <Box className="w-5 h-5 group-hover:scale-110 transition-transform" /> Confirm
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CreateShipment;
