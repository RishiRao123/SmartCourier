import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('user_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface TrackingEvent {
  id: string;
  trackingNumber: string;
  status: string;
  location: string;
  timestamp: string;
  message: string;
  proofImagePath?: string;
  deliveryNote?: string;
}

export interface TrackingData {
  trackingNumber: string;
  currentStatus: string;
  events: TrackingEvent[];
}

export interface Delivery {
  id: string;
  trackingNumber: string;
  senderName: string;
  senderAddress?: any;
  receiverName: string;
  receiverAddress: any;
  status: string;
  price: number;
  paymentMethod: string;
  paymentStatus: string;
  packageDetails: {
    weight: number;
    description: string;
  };
  createdAt: string;
}

export interface Invoice {
  invoiceNumber: string;
  trackingNumber: string;
  amount: number;
  weightKg: number;
  paymentMethod: string;
  paymentStatus: string;
  senderName: string;
  receiverName: string;
  receiverCity: string;
  description: string;
  createdAt: string;
  paidAt: string | null;
}

export interface UserProfile {
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

export const trackingService = {
  getTrackingDetails: async (trackingNumber: string): Promise<TrackingData> => {
    const response = await api.get(`/tracking/${trackingNumber}`);
    const events = response.data?.data || response.data;
    
    if (Array.isArray(events) && events.length > 0) {
      return {
        trackingNumber: events[0].trackingNumber,
        currentStatus: events[0].status,
        events: events
      };
    }
    
    return events;
  },
};

export const deliveryService = {
  getMyDeliveries: async (): Promise<Delivery[]> => {
    const response = await api.get('/deliveries/my');
    return response.data?.data || response.data;
  },
  getMyActiveDeliveries: async (): Promise<Delivery[]> => {
    const response = await api.get('/deliveries/my/active');
    return response.data?.data || response.data;
  },
  getMyDeliveredDeliveries: async (): Promise<Delivery[]> => {
    const response = await api.get('/deliveries/my/delivered');
    return response.data?.data || response.data;
  },
  getDeliveryByTrackingNumber: async (trackingNumber: string): Promise<Delivery> => {
    const response = await api.get(`/deliveries/${trackingNumber}`);
    return response.data?.data || response.data;
  },
  createDelivery: async (deliveryData: any): Promise<Delivery> => {
    const response = await api.post('/deliveries', deliveryData);
    return response.data?.data || response.data;
  }
};

export const invoiceService = {
  getInvoice: async (trackingNumber: string): Promise<Invoice> => {
    const response = await api.get(`/deliveries/${trackingNumber}/invoice`);
    return response.data?.data || response.data;
  }
};

export const profileService = {
  getProfile: async (): Promise<UserProfile> => {
    const response = await api.get('/auth/profile');
    return response.data?.data || response.data;
  },
  updateProfile: async (data: Partial<UserProfile>): Promise<UserProfile> => {
    const response = await api.put('/auth/profile', data);
    return response.data?.data || response.data;
  },
  uploadProfileImage: async (file: File): Promise<UserProfile> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/auth/profile/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data?.data || response.data;
  },
  deleteProfileImage: async (): Promise<UserProfile> => {
    const response = await api.delete('/auth/profile/image');
    return response.data?.data || response.data;
  }
};

export const authService = {
  verifyOtp: async (email: string, otp: string) => {
    const response = await api.post(`/auth/verify-otp?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`);
    return response.data?.data || response.data;
  },
  resendOtp: async (email: string) => {
    const response = await api.post(`/auth/resend-otp?email=${encodeURIComponent(email)}`);
    return response.data?.data || response.data;
  }
};
