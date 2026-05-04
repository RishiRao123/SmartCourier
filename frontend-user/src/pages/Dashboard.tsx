import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Package, ArrowRight, LayoutDashboard, ListFilter, Plus, Box, Clock, CheckCircle, Truck } from 'lucide-react';
import { deliveryService } from '../services/api';
import type { Delivery } from '../services/api';
import ShipmentList from '../components/Dashboard/ShipmentList';
import LoadingSpinner from '../components/LoadingSpinner';
import toast from 'react-hot-toast';

const Dashboard: React.FC = () => {
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'active' | 'delivered'>('all');

  useEffect(() => {
    fetchData();
  }, [filter]);

  const fetchData = async () => {
    try {
      setLoading(true);
      let data: Delivery[];
      if (filter === 'active') {
        data = await deliveryService.getMyActiveDeliveries();
      } else if (filter === 'delivered') {
        data = await deliveryService.getMyDeliveredDeliveries();
      } else {
        data = await deliveryService.getMyDeliveries();
      }
      setDeliveries(data);
    } catch (err: any) {
      console.error("Dashboard fetch error:", err);
      if (err.response) {
        console.error("Response data:", err.response.data);
        console.error("Response status:", err.response.status);
        if (err.response.status === 503) {
          toast.error("Services are currently syncing. Please wait a moment and try again.");
          return;
        }
      }
      toast.error('Failed to fetch your shipments.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <LoadingSpinner message="Finding your boxes..." />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white py-12 text-secondary-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Top Stats & Actions */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-16">
          <div className="lg:col-span-2">
            <div className="flex items-center gap-2 text-yellow-500 font-bold mb-4 uppercase tracking-widest text-sm">
              <LayoutDashboard className="w-4 h-4" />
              Customer Command Center
            </div>
            <h1 className="text-5xl font-black text-secondary-900 mb-6">Shipment Dashboard</h1>
            
            <div className="flex flex-wrap gap-4">
              <div className="flex bg-gray-50 p-1.5 rounded-2xl border border-gray-100">
                <button
                  onClick={() => setFilter('all')}
                  className={`px-8 py-3 rounded-xl font-bold transition-all ${filter === 'all' ? 'bg-yellow-500 text-[#071a2a] shadow-lg' : 'text-gray-400 hover:text-secondary-900'}`}
                >
                  All Shipments
                </button>
                <button
                  onClick={() => setFilter('active')}
                  className={`px-8 py-3 rounded-xl font-bold transition-all ${filter === 'active' ? 'bg-yellow-500 text-[#071a2a] shadow-lg' : 'text-gray-400 hover:text-secondary-900'}`}
                >
                  Active Only
                </button>
                <button
                  onClick={() => setFilter('delivered')}
                  className={`px-8 py-3 rounded-xl font-bold transition-all ${filter === 'delivered' ? 'bg-yellow-500 text-[#071a2a] shadow-lg' : 'text-gray-400 hover:text-secondary-900'}`}
                >
                  Delivered
                </button>
              </div>
            </div>
          </div>

          <div className="bg-gray-50 border border-gray-100 p-8 rounded-[40px] flex flex-col justify-between group hover:border-yellow-500/30 transition-all">
            <div>
              <p className="text-gray-400 font-bold uppercase tracking-widest text-xs mb-2">Total Deliveries</p>
              <h2 className="text-6xl font-black text-secondary-900 mb-4">{deliveries.length}</h2>
            </div>
            <Link
              to="/create-shipment"
              className="flex items-center justify-center gap-3 bg-[#071a2a] text-white py-5 rounded-2xl font-black hover:bg-yellow-500 hover:text-[#071a2a] transition-all shadow-xl active:scale-95 text-lg shadow-[#071a2a]/10 hover:shadow-yellow-500/20"
            >
              <Plus className="w-6 h-6" /> Book New Delivery
            </Link>
          </div>
        </div>

        {/* Shipment Cards Grid */}
        <ShipmentList deliveries={deliveries} />

        {/* Help Link */}
        <div className="mt-20 pt-10 border-t border-gray-100 text-center">
          <Link
            to="/#tracking-section"
            onClick={() => window.scrollTo(0, 0)}
            className="inline-flex items-center gap-2 text-gray-500 font-bold hover:text-yellow-500 transition-colors text-lg"
          >
            Need to track another package? <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
