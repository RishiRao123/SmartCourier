import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Package, Truck, CheckCircle, Clock, ArrowRight, MapPin, Calendar, IndianRupee } from 'lucide-react';
import type { Delivery } from '../../services/api';

interface ShipmentCardProps {
  delivery: Delivery;
  index: number;
}

const ShipmentCard: React.FC<ShipmentCardProps> = ({ delivery, index }) => {
  const getStatusStyle = (status: string) => {
    switch (status.toLowerCase()) {
      case 'delivered':
        return 'bg-green-50 text-green-600 border-green-100';
      case 'in_transit':
        return 'bg-yellow-50 text-yellow-600 border-yellow-100';
      case 'shipped':
        return 'bg-blue-50 text-blue-600 border-blue-100';
      default:
        return 'bg-gray-50 text-gray-400 border-gray-100';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'delivered':
        return <CheckCircle className="w-4 h-4" />;
      case 'in_transit':
        return <Truck className="w-4 h-4" />;
      case 'shipped':
        return <Package className="w-4 h-4" />;
      default:
        return <Clock className="w-4 h-4" />;
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.05 }}
      className="bg-white rounded-[40px] p-8 border border-gray-100 hover:border-yellow-500/30 transition-all group relative overflow-hidden shadow-sm hover:shadow-xl"
    >
      <div className="flex justify-between items-start mb-8">
        <div className="p-4 bg-yellow-500 rounded-2xl text-[#071a2a] shadow-lg group-hover:scale-110 transition-transform">
          <Package className="w-6 h-6" />
        </div>
        <div className={`flex items-center gap-2 px-4 py-2 rounded-full text-xs font-black border uppercase tracking-widest ${getStatusStyle(delivery.status)}`}>
          {getStatusIcon(delivery.status)}
          {delivery.status.replace('_', ' ')}
        </div>
      </div>

      <div className="mb-8">
        <p className="text-[10px] text-gray-400 font-black uppercase tracking-[0.2em] mb-2">Tracking ID</p>
        <p className="text-2xl font-black text-secondary-900 group-hover:text-yellow-500 transition-colors">{delivery.trackingNumber}</p>
      </div>

      <div className="space-y-5 mb-8">
        <div className="flex items-start gap-4">
          <div className="mt-1.5 w-2 h-2 bg-gray-200 rounded-full"></div>
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-widest">From</p>
            <p className="text-sm font-bold text-secondary-900">{delivery.senderName}</p>
          </div>
        </div>
        <div className="flex items-start gap-4">
          <div className="mt-1.5 w-2 h-2 bg-yellow-500 rounded-full"></div>
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Destination</p>
            <p className="text-sm font-bold text-secondary-900">{delivery.receiverName}</p>
          </div>
        </div>

        {/* Tracking Progress Redesign */}
        <div className="pt-4 pb-2">
          <div className="flex justify-between items-center mb-2">
            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tracking Status</span>
            <span className="text-[10px] font-black text-yellow-500 uppercase tracking-widest">{delivery.status.replace('_', ' ')}</span>
          </div>
          <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden flex">
            <div className={`h-full transition-all duration-1000 ${
              delivery.status === 'DELIVERED' ? 'w-full bg-green-500' : 
              delivery.status === 'IN_TRANSIT' ? 'w-2/3 bg-yellow-500' : 
              delivery.status === 'SHIPPED' ? 'w-1/3 bg-blue-500' : 'w-1/6 bg-gray-300'
            }`}></div>
          </div>
        </div>

        {delivery.price > 0 && (
          <div className="flex items-center justify-between bg-gray-50 rounded-2xl px-5 py-3 mt-4">
            <div className="flex items-center gap-2">
              <IndianRupee className="w-4 h-4 text-yellow-500" />
              <span className="text-lg font-black text-[#071a2a]">{delivery.price}</span>
            </div>
            <span className={`text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-widest ${
              delivery.paymentStatus === 'PAID' ? 'bg-green-100 text-green-600' : 'bg-orange-100 text-orange-600'
            }`}>
              {delivery.paymentStatus}
            </span>
          </div>
        )}
      </div>

      <Link
        to={`/track/${delivery.trackingNumber}`}
        className="w-full py-5 bg-[#071a2a] text-white font-black rounded-2xl flex items-center justify-center gap-3 hover:bg-yellow-500 hover:text-[#071a2a] transition-all text-sm uppercase tracking-widest shadow-lg shadow-[#071a2a]/10 hover:shadow-yellow-500/20"
      >
        Track Details
        <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
      </Link>
    </motion.div>
  );
};

export default ShipmentCard;
