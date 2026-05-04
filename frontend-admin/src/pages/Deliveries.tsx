import React, { useState, useEffect } from 'react';
import { formatDateTime } from '../utils/dateFormat';
import { useNavigate } from 'react-router-dom';
import { 
  Search, ChevronRight, RotateCcw, ChevronLeft
} from 'lucide-react';
import toast from 'react-hot-toast';
import api from '../services/api';
import StatusBadge from '../components/StatusBadge';
import CustomDropdown from '../components/CustomDropdown';
import CustomDatePicker from '../components/CustomDatePicker';
import LoadingSpinner from '../components/LoadingSpinner';

interface Delivery {
  id: number;
  trackingNumber: string;
  senderName: string;
  receiverName: string;
  status: string;
  updatedAt: string;
  weight?: number;
  dimensions?: number;
}

const statusOptions = [
  { value: 'ALL', label: 'All Statuses' },
  { value: 'DRAFT', label: 'DRAFT' },
  { value: 'BOOKED', label: 'BOOKED' },
  { value: 'PICKED_UP', label: 'PICKED_UP' },
  { value: 'IN_TRANSIT', label: 'IN_TRANSIT' },
  { value: 'OUT_FOR_DELIVERY', label: 'OUT_FOR_DELIVERY' },
  { value: 'DELIVERED', label: 'DELIVERED' },
  { value: 'DELAYED', label: 'DELAYED' },
  { value: 'FAILED', label: 'FAILED' },
  { value: 'RETURNED', label: 'RETURNED' },
];

const Deliveries = () => {
  const navigate = useNavigate();
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Filters
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [cityFilter, setCityFilter] = useState('');
  const [dateRange, setDateRange] = useState({ start: '', end: '' });

  // Pagination State
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const fetchDeliveries = async () => {
    setLoading(true);
    try {
      // Fixed endpoint: Use the new unified search endpoint
      let url = '/deliveries/admin/search';
      const params = new URLSearchParams();
      
      if (statusFilter !== 'ALL') params.append('status', statusFilter);
      if (cityFilter) params.append('city', cityFilter);
      
      // Backend expects ISO-8601 Instant (UTC)
      if (dateRange.start) params.append('start', `${dateRange.start}T00:00:00Z`);
      if (dateRange.end) params.append('end', `${dateRange.end}T23:59:59Z`);
      
      const response = await api.get(`${url}?${params.toString()}`);
      const data = response.data?.data !== undefined ? response.data.data : response.data;
      setDeliveries(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to fetch logistics data. Please check filters.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeliveries();
  }, []);

  const clearFilters = () => {
    setStatusFilter('ALL');
    setCityFilter('');
    setDateRange({ start: '', end: '' });
    // Slight delay to ensure state is updated before fetch
    setTimeout(() => fetchDeliveries(), 50);
  };

  const openDeliveryDetails = (trackingNumber: string) => {
    navigate(`/tracking/${trackingNumber}`);
  };

  // Pagination Logic
  const totalPages = Math.ceil(deliveries.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = deliveries.slice(indexOfFirstItem, indexOfLastItem);

  const paginate = (pageNumber: number) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <h1 className="text-4xl font-black text-blue-900 tracking-tighter">All Deliveries</h1>
          <p className="text-gray-500 font-bold mt-1">See and track all packages here</p>
        </div>
      </div>
    
      {/* Filter Bar */}
      <div className="bg-white rounded-[40px] shadow-2xl border border-gray-100 p-8">
        <div className="flex flex-wrap items-end gap-6">
          <div className="flex-1 min-w-[200px]">
            <CustomDropdown 
              label="Status"
              options={statusOptions}
              value={statusFilter}
              onChange={setStatusFilter}
            />
          </div>

          <div className="flex-1 min-w-[200px] space-y-1.5">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">City Hub</label>
            <input 
              type="text"
              placeholder="Mumbai"
              className="w-full bg-gray-50 border-2 border-gray-100 rounded-2xl py-3 px-4 font-black text-blue-900 focus:bg-white focus:border-yellow-500 focus:ring-4 focus:ring-yellow-500/10 outline-none transition-all hover:border-gray-200"
              value={cityFilter}
              onChange={(e) => setCityFilter(e.target.value)}
            />
          </div>

          <div className="flex-[2] min-w-[320px] space-y-1.5">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Date Range</label>
            <div className="flex gap-4">
              <CustomDatePicker 
                value={dateRange.start}
                onChange={(val) => setDateRange(prev => ({ ...prev, start: val }))}
                placeholder="From"
              />
              <CustomDatePicker 
                value={dateRange.end}
                onChange={(val) => setDateRange(prev => ({ ...prev, end: val }))}
                placeholder="To"
              />
            </div>
          </div>

          <div className="flex gap-3 h-[52px]">
            <button 
              onClick={fetchDeliveries}
              className="bg-yellow-500 text-[#071a2a] hover:bg-yellow-400 font-black px-8 rounded-2xl transition-all shadow-xl flex items-center justify-center gap-2 whitespace-nowrap"
            >
              <Search className="w-5 h-5" /> Search
            </button>
            <button 
              onClick={clearFilters}
              className="p-3 bg-gray-100 hover:bg-gray-200 text-gray-500 rounded-2xl transition-all flex items-center justify-center"
              title="Clear Filters"
            >
              <RotateCcw className="w-6 h-6" />
            </button>
          </div>
        </div>
      </div>

      {/* Grid Table */}
      <div className="bg-white rounded-[40px] shadow-2xl border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100">
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Tracking ID</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Shipment Info</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Current Status</th>
                <th className="px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Last Update</th>
                <th className="px-8 py-5 text-right pr-12 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {loading ? (
                <tr>
                  <td colSpan={5} className="px-8 py-20 text-center">
                    <div className="flex flex-col items-center gap-4">
                      <div className="w-12 h-12 border-4 border-blue-900 border-t-yellow-500 rounded-full animate-spin"></div>
                      <p className="text-blue-900 font-black uppercase tracking-widest text-xs">Loading Logistics Grid...</p>
                    </div>
                  </td>
                </tr>
              ) : deliveries.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-8 py-20 text-center">
                    <p className="text-gray-400 font-bold uppercase tracking-widest text-sm">No shipments found matching filters</p>
                  </td>
                </tr>
              ) : (
                currentItems.map((delivery) => (
                  <tr 
                    key={delivery.id} 
                    className="hover:bg-blue-50/40 transition-all cursor-pointer group"
                    onClick={() => openDeliveryDetails(delivery.trackingNumber)}
                  >
                    <td className="px-8 py-6">
                      <span className="text-xl font-black text-blue-900 group-hover:text-yellow-500 transition-colors">
                        #{delivery.trackingNumber}
                      </span>
                    </td>
                    <td className="px-8 py-6">
                      <div className="flex flex-col gap-1">
                        <p className="text-sm font-black text-blue-900">{delivery.senderName} <span className="text-yellow-500 mx-1">→</span> {delivery.receiverName}</p>
                      </div>
                    </td>
                    <td className="px-8 py-6">
                      <StatusBadge status={delivery.status} />
                    </td>
                    <td className="px-8 py-6 text-sm font-bold text-gray-500">
                      {formatDateTime(delivery.updatedAt)}
                    </td>
                    <td className="px-8 py-6 text-right pr-12">
                      <button className="p-3 bg-white text-blue-900 rounded-2xl shadow-sm border border-gray-100 group-hover:bg-yellow-500 group-hover:border-yellow-500 group-hover:text-blue-900 transition-all">
                        <ChevronRight className="w-5 h-5" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <div className="p-8 border-t border-gray-50 flex justify-center bg-gray-50/30">
            <div className="flex items-center gap-2">
              <button
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
                className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((number) => (
                <button
                  key={number}
                  onClick={() => paginate(number)}
                  className={`w-10 h-10 rounded-xl font-black text-sm transition-all ${
                    currentPage === number 
                      ? 'bg-yellow-500 text-[#071a2a] shadow-lg shadow-yellow-500/30' 
                      : 'bg-white text-gray-400 hover:bg-gray-100 border border-gray-200'
                  }`}
                >
                  {number}
                </button>
              ))}
              <button
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Deliveries;
