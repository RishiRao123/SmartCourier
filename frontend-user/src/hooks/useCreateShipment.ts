import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { deliveryService, profileService } from '../services/api';

export const useCreateShipment = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [price, setPrice] = useState(0);

  const [formData, setFormData] = useState({
    senderName: '',
    senderAddress: { street: '', city: '', state: '', zipCode: '' },
    receiverName: '',
    receiverPhone: '',
    receiverAddress: { street: '', city: '', state: '', zipCode: '' },
    packageDetails: { weight: 0, description: '' },
    paymentMethod: 'PAY_ON_DELIVERY',
    pickupDate: new Date().toISOString().split('T')[0]
  });

  // Client-side price preview (backend is source of truth)
  useEffect(() => {
    const w = formData.packageDetails.weight;
    let p = 0;
    if (w > 0 && w <= 0.5) p = 49;
    else if (w > 0.5 && w <= 2) p = 99;
    else if (w > 2 && w <= 5) p = 199;
    else if (w > 2 && w <= 5) p = 199;
    else if (w > 5 && w <= 10) p = 399;
    else if (w > 10) p = 399 + (w - 10) * 40;
    setPrice(Math.round(p));
  }, [formData.packageDetails.weight]);

  // Auto-populate sender (silent)
  useEffect(() => {
    const loadProfile = async () => {
      try {
        const profile = await profileService.getProfile();
        if (profile) {
          setFormData(prev => ({
            ...prev,
            senderName: profile.username || prev.senderName,
            senderAddress: {
              street: profile.street || prev.senderAddress.street,
              city: profile.city || prev.senderAddress.city,
              state: profile.state || prev.senderAddress.state,
              zipCode: profile.zipCode || prev.senderAddress.zipCode,
            }
          }));
        }
      } catch (err) {}
    };
    loadProfile();
  }, []);

  const handleChange = (section: string, field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [section]: field 
        ? { ...(prev[section as keyof typeof prev] as object), [field]: value }
        : value
    }));
  };

  const handleAddressChange = (section: 'senderAddress' | 'receiverAddress', field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [section]: { ...prev[section], [field]: value }
    }));
  };

  const processBooking = async () => {
    setLoading(true);
    try {
      const response = await deliveryService.createDelivery({
        senderName: formData.senderName,
        senderAddress: formData.senderAddress,
        receiverName: formData.receiverName,
        receiverPhone: formData.receiverPhone,
        receiverAddress: formData.receiverAddress,
        packageDetails: formData.packageDetails,
        paymentMethod: formData.paymentMethod
      });
      toast.success('Shipment booked successfully!');
      navigate(`/invoice/${response.trackingNumber}`);
      return true;
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Booking failed.');
      return false;
    } finally {
      setLoading(false);
      setShowPaymentModal(false);
    }
  };

  return {
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
  };
};
