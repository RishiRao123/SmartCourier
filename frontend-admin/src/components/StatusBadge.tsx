import React from 'react';

interface StatusBadgeProps {
  status: string;
}

const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  if (!status) return null;

  const normalizedStatus = status.toUpperCase();

  const getStyles = () => {
    switch (normalizedStatus) {
      case 'DRAFT': return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'BOOKED': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'PICKED_UP': return 'bg-indigo-100 text-indigo-800 border-indigo-200';
      case 'IN_TRANSIT': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'OUT_FOR_DELIVERY': return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'DELIVERED': return 'bg-green-100 text-green-800 border-green-200';
      case 'DELAYED': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'FAILED': return 'bg-red-100 text-red-800 border-red-200';
      case 'RETURNED': return 'bg-rose-100 text-rose-800 border-rose-200';
      case 'EXCEPTION': return 'bg-red-600 text-white border-red-700';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  return (
    <span className={`px-3 py-1 rounded-full text-[10px] font-black tracking-widest border uppercase w-max block ${getStyles()}`}>
      {status}
    </span>
  );
};

export default StatusBadge;
