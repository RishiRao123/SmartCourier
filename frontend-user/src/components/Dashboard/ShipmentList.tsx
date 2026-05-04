import React from 'react';
import { Link } from 'react-router-dom';
import { Box } from 'lucide-react';
import ShipmentCard from './ShipmentCard';
import type { Delivery } from '../../services/api';

interface ShipmentListProps {
  deliveries: Delivery[];
}

const ShipmentList: React.FC<ShipmentListProps> = ({ deliveries }) => {
  if (deliveries.length === 0) {
    return (
      <div className="bg-white rounded-3xl p-20 text-center shadow-sm border border-gray-100">
        <div className="w-20 h-20 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-6 text-gray-400">
          <Box className="w-10 h-10" />
        </div>
        <h2 className="text-2xl font-bold text-secondary-900 mb-2">No shipments found</h2>
        <p className="text-gray-500 mb-8">You haven't sent or received any packages yet.</p>
        <Link to="/create-shipment" className="btn-primary inline-block">Start Shipping</Link>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
      {deliveries.map((delivery, index) => (
        <ShipmentCard key={delivery.id} delivery={delivery} index={index} />
      ))}
    </div>
  );
};

export default ShipmentList;
