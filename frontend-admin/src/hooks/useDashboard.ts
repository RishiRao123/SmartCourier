import { useState, useEffect } from "react";
import api from "../services/api";

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

export const useDashboard = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalActiveDeliveries: 0,
    totalTrackingEvents: 0,
  });
  const [summary, setSummary] = useState<DashboardSummary>({
    totalDeliveries: 0,
    activeInTransit: 0,
    totalDelivered: 0,
    recentEvents: 0,
  });
  const [recentActivity, setRecentActivity] = useState<TrackingEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  const fetchDashboardData = async () => {
    setLoading(true);
    setError("");
    try {
      const [statsRes, trackingRes, summaryRes] = await Promise.all([
        api.get("/admin/dashboard/stats"),
        api.get("/admin/dashboard/tracking/recent?days=7"),
        api.get("/admin/dashboard/summary")
      ]);

      const statsData = statsRes.data?.data ?? statsRes.data;
      if (statsData) {
        setStats({
          totalActiveDeliveries: statsData.totalActiveDeliveries || 0,
          totalTrackingEvents: statsData.totalTrackingEvents || 0,
        });
      }

      const activityData = trackingRes.data?.data ?? trackingRes.data;
      setRecentActivity(activityData || []);

      const summaryData = summaryRes.data?.data ?? summaryRes.data;
      if (summaryData) {
        setSummary({
          totalDeliveries: summaryData.totalDeliveries || 0,
          activeInTransit: summaryData.activeInTransit || 0,
          totalDelivered: summaryData.totalDelivered || 0,
          recentEvents: summaryData.recentEvents || 0,
        });
      }
    } catch (err: any) {
      console.error("Failed to load dashboard data", err);
      setError("Failed to securely fetch dashboard metrics from the API Gateway.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  return {
    stats,
    summary,
    recentActivity,
    loading,
    error,
    refresh: fetchDashboardData
  };
};
