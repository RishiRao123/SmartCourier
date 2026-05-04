import React, { useState, useEffect } from "react";
import { formatDateTime } from '../utils/dateFormat';
import { useNavigate } from "react-router-dom";
import {
  Activity,
  Clock,
  CheckCircle2,
  AlertCircle,
  Calendar,
  Package,
  Truck,
  CheckCircle,
  Navigation,
  XCircle,
  Undo,
  AlertTriangle,
  FileEdit,
  ClipboardList,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import api from "../services/api";
import LoadingSpinner from "../components/LoadingSpinner";

interface TrackingEvent {
  trackingNumber: string;
  status: string;
  message: string;
  timestamp: string;
}

interface DashboardStats {
  totalActiveDeliveries: number;
  totalTrackingEvents: number;
}

interface DashboardSummary {
  totalDeliveries: number;
  activeInTransit: number;
  totalDelivered: number;
  recentEvents: number;
}

import { useDashboard } from "../hooks/useDashboard";
import StatusBadge from "../components/StatusBadge";
 
const Dashboard = () => {
  const navigate = useNavigate();
  const {
    stats,
    summary,
    recentActivity,
    loading,
    error
  } = useDashboard();
 
  // Pagination State
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
 
  const formatDate = (dateString: string) => formatDateTime(dateString);
 
  // Pagination Logic
  const totalPages = Math.ceil(recentActivity.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = recentActivity.slice(indexOfFirstItem, indexOfLastItem);
 
  const paginate = (pageNumber: number) => {
    setCurrentPage(pageNumber);
    // Smooth scroll to top of list
    const listElement = document.getElementById('activity-feed');
    if (listElement) {
      listElement.scrollIntoView({ behavior: 'smooth' });
    }
  };
 
  if (loading) {
    return <LoadingSpinner message="Getting Dashboard Ready..." />;
  }
 
  if (error) {
    return (
      <div className='bg-red-50 border-2 border-red-200 rounded-3xl p-8 flex items-start gap-6 shadow-xl max-w-3xl mx-auto mt-10'>
        <AlertCircle className='w-10 h-10 text-red-600 flex-shrink-0' />
        <div>
          <h3 className='font-black text-red-900 mb-2 text-2xl tracking-tight'>
            System Connection Error
          </h3>
          <p className='text-red-700 font-bold text-lg'>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className='space-y-10 animate-fade-in-up pb-10'>
      <h1 className="text-3xl font-black text-blue-900 tracking-tight">Main Dashboard</h1>


      {/* Dashboard Summary Metrics */}
      <div className='grid grid-cols-2 md:grid-cols-4 gap-6'>
        <div className='bg-white rounded-3xl p-8 shadow-xl border border-gray-100 flex items-center justify-between group hover:-translate-y-2 transition-all duration-500'>
          <div>
            <p className='text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>Total Shipments</p>
            <h4 className='text-4xl font-black text-blue-900 tracking-tighter'>{summary.totalDeliveries.toLocaleString()}</h4>
          </div>
          <div className='w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center border border-blue-100 group-hover:bg-yellow-500 group-hover:rotate-6 transition-all duration-500'>
            <Package className='w-8 h-8 text-blue-600 group-hover:text-blue-900 transition-colors' />
          </div>
        </div>
        
        <div className='bg-white rounded-3xl p-8 shadow-xl border border-gray-100 flex items-center justify-between group hover:-translate-y-2 transition-all duration-500'>
          <div>
            <p className='text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>Packages Moving</p>
            <h4 className='text-4xl font-black text-blue-900 tracking-tighter'>{summary.activeInTransit.toLocaleString()}</h4>
          </div>
          <div className='w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center border border-blue-100 group-hover:bg-yellow-500 group-hover:rotate-6 transition-all duration-500'>
            <Truck className='w-8 h-8 text-blue-600 group-hover:text-blue-900 transition-colors' />
          </div>
        </div>

        <div className='bg-white rounded-3xl p-8 shadow-xl border border-gray-100 flex items-center justify-between group hover:-translate-y-2 transition-all duration-500'>
          <div>
            <p className='text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>Safe Arrivals</p>
            <h4 className='text-4xl font-black text-green-600 tracking-tighter'>{summary.totalDelivered.toLocaleString()}</h4>
          </div>
          <div className='w-16 h-16 bg-green-50 rounded-2xl flex items-center justify-center border border-green-100 group-hover:bg-yellow-500 group-hover:rotate-6 transition-all duration-500'>
            <CheckCircle2 className='w-8 h-8 text-green-600 group-hover:text-blue-900 transition-colors' />
          </div>
        </div>

        <div className='bg-white rounded-3xl p-8 shadow-xl border border-gray-100 flex items-center justify-between group hover:-translate-y-2 transition-all duration-500'>
          <div>
            <p className='text-xs font-black text-gray-400 uppercase tracking-widest mb-2'>Recent Updates</p>
            <h4 className='text-4xl font-black text-yellow-600 tracking-tighter'>{summary.recentEvents.toLocaleString()}</h4>
          </div>
          <div className='w-16 h-16 bg-yellow-50 rounded-2xl flex items-center justify-center border border-yellow-100 group-hover:bg-yellow-500 group-hover:rotate-6 transition-all duration-500'>
            <Activity className='w-8 h-8 text-yellow-600 group-hover:text-blue-900 transition-colors' />
          </div>
        </div>
      </div>

      {/* Bottom Section: Recent Activity Feed */}
      <div id="activity-feed" className='bg-white rounded-[40px] shadow-2xl border border-gray-100 overflow-hidden'>
        <div className='p-8 border-b border-gray-100 bg-gray-50/50 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4'>
          <h2 className='text-2xl font-black text-blue-900 flex items-center gap-4'>
            <div className='p-2 bg-yellow-100 rounded-xl'>
              <Clock className='w-6 h-6 text-yellow-600' />
            </div>
            All Updates
          </h2>
          <div className="flex items-center gap-4">
            <span className='px-4 py-2 bg-white shadow-sm border border-gray-200 text-gray-700 rounded-xl text-[10px] font-black tracking-[0.2em] uppercase'>
              Live Tracking
            </span>
            {totalPages > 1 && (
               <div className="flex items-center bg-gray-100 p-1 rounded-xl border border-gray-200">
                  <button 
                    onClick={() => paginate(currentPage - 1)}
                    disabled={currentPage === 1}
                    className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-blue-900"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <span className="px-3 text-[10px] font-black text-blue-900">
                    {currentPage} / {totalPages}
                  </span>
                  <button 
                    onClick={() => paginate(currentPage + 1)}
                    disabled={currentPage === totalPages}
                    className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-blue-900"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
               </div>
            )}
          </div>
        </div>

        <div className='p-0'>
          {recentActivity.length === 0 ? (
            <div className='p-24 text-center text-gray-500 font-medium flex flex-col items-center bg-gray-50/30'>
              <div className='w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mb-6'>
                <CheckCircle2 className='w-10 h-10 text-blue-300' />
              </div>
              <p className='text-2xl font-black text-blue-900'>All Clear</p>
              <p className='text-lg mt-2'>
                No recent tracking events recorded in the system.
              </p>
            </div>
          ) : (
            <>
              <ul className='divide-y divide-gray-100'>
                {currentItems.map((event, index) => (
                  <li
                    key={index}
                    onClick={() => navigate(`/tracking/${event.trackingNumber}`)}
                    className='p-6 hover:bg-blue-50/40 transition-colors duration-300 group cursor-pointer'
                  >
                    <div className='flex flex-col sm:flex-row sm:items-center justify-between gap-6'>
                      <div className='flex items-start gap-6'>
                        <div className='mt-1 w-14 h-14 bg-gray-50 rounded-2xl flex items-center justify-center flex-shrink-0 shadow-sm border border-gray-200 group-hover:bg-white group-hover:border-blue-200 group-hover:shadow-md transition-all duration-300'>
                          <Package className='w-6 h-6 text-blue-900 group-hover:text-yellow-500 transition-colors duration-300' />
                        </div>
                        <div>
                          <div className='flex items-center gap-4 mb-2'>
                            <span className='font-black text-blue-900 text-xl tracking-tight'>
                              #{event.trackingNumber}
                            </span>
                            <StatusBadge status={event.status} />
                          </div>
                          <p className='text-gray-700 text-base font-bold'>
                            {event.message}
                          </p>
                        </div>
                      </div>
                      <div className='flex items-center gap-2 text-gray-600 text-sm font-black bg-gray-50 px-5 py-3 rounded-xl border border-gray-200 shadow-sm sm:ml-auto whitespace-nowrap group-hover:bg-white transition-colors'>
                        <Calendar className='w-5 h-5 text-indigo-500' />
                        {formatDate(event.timestamp)}
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
              {totalPages > 1 && (
                <div className="p-8 border-t border-gray-50 flex justify-center bg-gray-50/30">
                  <div className="flex items-center gap-2">
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map((number) => (
                      <button
                        key={number}
                        onClick={() => paginate(number)}
                        className={`w-10 h-10 rounded-xl font-black text-sm transition-all ${
                          currentPage === number 
                            ? 'bg-yellow-500 text-blue-900 shadow-lg shadow-yellow-500/30' 
                            : 'bg-white text-gray-400 hover:bg-gray-100 border border-gray-200'
                        }`}
                      >
                        {number}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
