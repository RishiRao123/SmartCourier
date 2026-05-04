import React, { useEffect, useState } from 'react';
import { formatDate, formatTime } from '../utils/dateFormat';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Package, 
  Truck, 
  CheckCircle, 
  Clock, 
  MapPin, 
  AlertCircle,
  ArrowLeft,
  Box,
  Scale,
  IndianRupee,
  FileText,
  Image as ImageIcon,
  ExternalLink
} from 'lucide-react';
import { trackingService, deliveryService } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import type { TrackingData, Delivery } from '../services/api';

const Track: React.FC = () => {
  const { trackingNumber } = useParams<{ trackingNumber: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<TrackingData | null>(null);
  const [delivery, setDelivery] = useState<Delivery | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTracking = async () => {
      if (!trackingNumber) return;
      try {
        setLoading(true);
        setError(null);
        
        // Check if user is logged in
        const token = localStorage.getItem('user_token');
        if (!token) {
          setError('Please log in to track your shipments.');
          setLoading(false);
          return;
        }
        
        try {
          // Check if this delivery belongs to the logged-in user
          const myDeliveries = await deliveryService.getMyDeliveries();
          const isMine = myDeliveries.some((d: any) => d.trackingNumber === trackingNumber);
          if (!isMine) {
            setError('You are not authorized to view the tracking details of this shipment.');
            setLoading(false);
            return;
          }
        } catch (e) {
            console.warn("Could not verify ownership, proceeding...");
        }

        // Fetch tracking timeline
        const result = await trackingService.getTrackingDetails(trackingNumber);
        setData(result);

        // Fetch delivery specs (weight, price, payment, etc.)
        try {
          const deliveryResult = await deliveryService.getDeliveryByTrackingNumber(trackingNumber);
          setDelivery(deliveryResult);
        } catch (deliveryErr) {
          console.warn('Delivery details not found, but tracking exists.');
        }

      } catch (err) {
        console.error(err);
        setError('Tracking Not Found');
      } finally {
        setLoading(false);
      }
    };

    fetchTracking();
  }, [trackingNumber]);

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'delivered': return <CheckCircle className="h-6 w-6" />;
      case 'in_transit': return <Truck className="h-6 w-6" />;
      case 'shipped': return <Package className="h-6 w-6" />;
      case 'pending': return <Clock className="h-6 w-6" />;
      default: return <Package className="h-6 w-6" />;
    }
  };

  const getStatusColor = (status: string, isLatest: boolean) => {
    if (!isLatest) return 'bg-yellow-50 text-yellow-500 border border-yellow-100';
    return 'bg-yellow-500 text-[#071a2a]';
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <LoadingSpinner message="Tracking your box..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-xl mx-auto px-4 py-20 text-center">
        <div className="bg-red-50 p-12 rounded-[40px] border border-red-100">
          <AlertCircle className="h-16 w-16 text-red-500 mx-auto mb-6" />
          <h2 className="text-3xl font-black text-secondary-900 mb-8 tracking-tighter">{error}</h2>
          <Link to="/dashboard" className="bg-yellow-500 text-[#071a2a] font-black px-10 py-4 rounded-2xl shadow-xl hover:-translate-y-1 hover:bg-yellow-400 transition-all inline-flex items-center gap-3">
            <ArrowLeft className="h-5 w-5" />
            <span>Go Back</span>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white min-h-screen py-12 lg:py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        
        {/* Navigation & Header */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6 bg-gray-50 p-8 rounded-[40px] border border-gray-100">
          <div className="flex items-center gap-6">
            <button 
              onClick={() => navigate('/dashboard')}
              className="p-4 bg-white text-[#071a2a] rounded-2xl border border-gray-200 hover:bg-yellow-500 transition-all hover:scale-105 shadow-sm"
            >
              <ArrowLeft className="w-6 h-6" />
            </button>
            <div>
              <h1 className="text-4xl font-black text-[#071a2a] tracking-tighter flex items-center gap-4">
                Shipment Manifest
                <span className="text-yellow-500">#{data?.trackingNumber}</span>
              </h1>
            </div>
          </div>
          
          <div className="bg-white p-2 rounded-3xl shadow-lg border border-gray-100 flex items-center gap-4 pr-6">
            <div className="w-12 h-12 bg-yellow-500 rounded-2xl flex items-center justify-center shadow-[0_0_20px_rgba(234,179,8,0.2)]">
              <Clock className="w-6 h-6 text-[#071a2a]" />
            </div>
            <div className="flex flex-col">
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Current Status</p>
              <span className={`text-sm font-black uppercase tracking-tight ${data?.currentStatus === 'DELIVERED' ? 'text-green-600' : 'text-yellow-600'}`}>
                {data?.currentStatus?.replace('_', ' ')}
              </span>
            </div>
          </div>
        </div>

        {/* Info Grid */}
        <div className="grid grid-cols-1 xl:grid-cols-3 gap-10">
          <div className="xl:col-span-2 space-y-10">
            
            {/* Main Stats Card */}
            <div className="bg-white text-[#071a2a] rounded-[40px] shadow-sm border border-gray-100 p-10 relative overflow-hidden">
              <div className="absolute top-0 right-0 w-64 h-64 bg-yellow-500/5 rounded-full blur-3xl"></div>
              
              <div className="flex justify-between items-start mb-12">
                <h3 className="text-2xl font-black text-[#071a2a] flex items-center gap-3 relative z-10">
                  <Box className="w-8 h-8 text-yellow-500" /> Logistics Overview
                </h3>
                {delivery && (
                  <button 
                    onClick={() => navigate(`/invoice/${trackingNumber}`)}
                    className="bg-yellow-500 text-[#071a2a] px-6 py-3 rounded-2xl font-black text-xs flex items-center gap-2 hover:bg-yellow-400 transition-all shadow-xl z-10"
                  >
                    <FileText className="w-4 h-4" /> DOWNLOAD INVOICE
                  </button>
                )}
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                <div className="space-y-8 relative z-10">
                  <div>
                    <div className="flex items-center gap-3 mb-4">
                      <div className="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center border border-white/20">
                        <MapPin className="w-5 h-5 text-yellow-500" />
                      </div>
                      <h4 className="text-xs font-black text-gray-400 uppercase tracking-[0.2em]">Origin Point</h4>
                    </div>
                    <div className="bg-gray-50 border border-gray-100 rounded-3xl p-6">
                      <p className="text-xl font-bold text-[#071a2a]">{delivery?.senderName}</p>
                      <p className="text-sm text-gray-500 mt-2">{delivery?.senderAddress?.street}, {delivery?.senderAddress?.city}</p>
                    </div>
                  </div>
                  
                  <div>
                    <div className="flex items-center gap-3 mb-4">
                      <div className="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center border border-white/20">
                        <MapPin className="w-5 h-5 text-yellow-500" />
                      </div>
                      <h4 className="text-xs font-black text-gray-400 uppercase tracking-[0.2em]">Destination Point</h4>
                    </div>
                    <div className="bg-gray-50 border border-gray-100 rounded-3xl p-6">
                      <p className="text-xl font-bold text-[#071a2a]">{delivery?.receiverName}</p>
                      <p className="text-sm text-gray-500 mt-2">{delivery?.receiverAddress?.street}, {delivery?.receiverAddress?.city}</p>
                    </div>
                  </div>
                </div>
                
                <div className="space-y-6 flex flex-col justify-center border-l-2 border-gray-100 pl-0 md:pl-10 relative z-10">
                  <div className="bg-gray-50 border border-gray-100 p-6 rounded-3xl">
                    <div className="flex items-center gap-3 mb-2">
                      <Scale className="w-4 h-4 text-gray-400" />
                      <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Weight Data</span>
                    </div>
                    <p className="text-lg font-bold text-[#071a2a]">{delivery?.packageDetails?.weight || '0'} KG</p>
                  </div>
                  
                  <div className="bg-yellow-500 text-[#071a2a] p-6 rounded-3xl flex justify-between items-center shadow-lg transform hover:scale-105 transition-all">
                    <div>
                      <span className="text-[10px] font-black uppercase tracking-widest opacity-80 flex items-center gap-2">
                        <IndianRupee className="w-4 h-4" /> Total Price
                      </span>
                      <h4 className="text-3xl font-black mt-1">₹{delivery?.price || '—'}</h4>
                    </div>
                    <div className="text-right">
                      <span className={`text-[10px] font-black px-3 py-1 rounded-full border ${delivery?.paymentStatus === 'PAID' ? 'bg-green-100 text-green-800 border-green-200' : 'bg-red-100 text-red-800 border-red-200'}`}>
                        {delivery?.paymentStatus || 'PENDING'}
                      </span>
                      <p className="text-[8px] font-bold opacity-70 mt-2 uppercase">{delivery?.paymentMethod?.replace('_', ' ')}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Journey Timeline */}
            <div className="bg-white rounded-[40px] shadow-sm border border-gray-100 p-10">
              <h3 className="text-3xl font-black text-[#071a2a] mb-12 flex items-center gap-4">
                <div className="w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-yellow-500 border border-gray-100">
                  <Truck className="w-6 h-6" />
                </div>
                Journey Timeline
              </h3>
              
              <div className="space-y-0">
                {data?.events.map((event, index) => {
                  const isLatest = index === 0;
                  return (
                    <motion.div 
                      key={event.id}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: index * 0.1 }}
                      className="relative pl-16 pb-12 last:pb-0"
                    >
                      {index !== data.events.length - 1 && (
                        <div className="absolute left-6 top-10 bottom-0 w-0.5 bg-gray-100"></div>
                      )}

                      <div className={`absolute left-0 top-0 w-12 h-12 rounded-2xl flex items-center justify-center z-10 shadow-lg ${getStatusColor(event.status, isLatest)}`}>
                        {getStatusIcon(event.status)}
                      </div>

                      <div className={`p-8 rounded-[32px] transition-all border ${isLatest ? 'bg-gray-50 border-yellow-500/30 shadow-xl ring-1 ring-yellow-500/10' : 'bg-white border-gray-100'}`}>
                        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
                          <div>
                            <h3 className="text-xl font-black text-[#071a2a] capitalize">{event.status.replace('_', ' ')}</h3>
                            <div className="flex items-center text-gray-400 text-sm mt-1 font-bold">
                              <MapPin className="h-4 w-4 mr-1" />
                              <span>{event.location}</span>
                            </div>
                          </div>
                          <div className="text-right">
                            <p className="text-[#071a2a] font-black">{formatDate(event.timestamp)}</p>
                            <p className="text-gray-400 text-sm font-bold">{formatTime(event.timestamp)}</p>
                          </div>
                        </div>
                        <p className="text-gray-600 font-medium leading-relaxed mb-6">{event.message}</p>
                        
                        {event.proofImagePath && (
                          <div className="mt-6 bg-white rounded-[28px] p-6 border border-gray-100 shadow-sm overflow-hidden group/proof">
                            <div className="flex items-center gap-3 mb-4">
                              <ImageIcon className="w-4 h-4 text-green-500" />
                              <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Official Delivery Evidence</span>
                            </div>
                            
                            <div className="relative rounded-2xl overflow-hidden shadow-lg border border-gray-100 aspect-video max-w-md mb-4 bg-gray-100">
                              <img 
                                src={`http://localhost:8080/tracking/documents/file/${event.proofImagePath}`} 
                                alt="Proof of Delivery" 
                                className="w-full h-full object-cover transition-transform duration-700 group-hover/proof:scale-110"
                                onError={(e) => {
                                  (e.target as HTMLImageElement).src = 'https://via.placeholder.com/800x450?text=Proof+Image+Pending';
                                }}
                              />
                            </div>
                            
                            {event.deliveryNote && (
                              <div className="bg-green-50/50 p-4 rounded-2xl border border-green-100/50 italic text-green-900 text-sm font-bold">
                                "{event.deliveryNote}"
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            </div>
          </div>

          <div className="space-y-10">
            {/* Quick Support Card */}
            <div className="bg-yellow-500 text-[#071a2a] rounded-[40px] p-8 shadow-xl relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl"></div>
              <h4 className="text-xl font-black mb-4">Need Help?</h4>
              <p className="text-[#071a2a]/70 font-bold text-sm mb-6">If you have any questions regarding your shipment, our support team is available 24/7.</p>
              <button className="w-full bg-[#071a2a] text-yellow-500 font-black py-4 rounded-2xl hover:bg-[#0c2d48] transition-all shadow-lg">
                CONTACT SUPPORT
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Track;

