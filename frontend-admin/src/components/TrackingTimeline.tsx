import React from 'react';
import { formatDateTime } from '../utils/dateFormat';
import { Clock } from 'lucide-react';
import StatusBadge from './StatusBadge';

export interface TrackingEvent {
  id?: number;
  trackingNumber: string;
  status: string;
  message: string;
  timestamp: string;
}

interface TrackingTimelineProps {
  history: TrackingEvent[];
  loading: boolean;
}

const formatDate = (dateString: string) => formatDateTime(dateString);

const TrackingTimeline: React.FC<TrackingTimelineProps> = ({ history, loading }) => {
  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <Clock className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    );
  }

  if (history.length === 0) {
    return (
      <div className="text-center py-6">
        <p className="text-gray-500">No tracking history available.</p>
      </div>
    );
  }

  return (
    <div className="relative border-l-2 border-blue-200 ml-4 space-y-6 pb-4">
      {history.map((event, idx) => (
        <div key={idx} className="relative pl-6">
          <div className="absolute w-4 h-4 bg-yellow-500 rounded-full -left-[9px] top-1 border-2 border-white"></div>
          <p className="text-xs text-gray-500 mb-1">{formatDate(event.timestamp)}</p>
          <div className="mb-2"><StatusBadge status={event.status} /></div>
          <p className="text-sm font-medium text-gray-700">{event.message}</p>
        </div>
      ))}
    </div>
  );
};

export default TrackingTimeline;
