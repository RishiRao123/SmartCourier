import { useState, useEffect } from "react";
import toast from "react-hot-toast";
import api from "../services/api";
import { useAuth } from "../contexts/AuthContext";
import type { TrackingEvent } from "../components/TrackingTimeline";

interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
}

interface PackageDetails {
  weight: number;
  description: string;
}

interface DeliveryDetails {
  id: number;
  trackingNumber: string;
  senderName: string;
  senderAddress: Address;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: Address;
  packageDetails: PackageDetails;
  price: number;
  paymentMethod: string;
  paymentStatus: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

interface TrackingDocument {
  id: number;
  trackingNumber: string;
  fileName: string;
  fileType: string;
  filePath: string;
  uploadedBy: number;
  uploaderRole: string;
}

export const useTrackingDetails = (trackingNumber: string | undefined) => {
  const { user } = useAuth();
  const [delivery, setDelivery] = useState<DeliveryDetails | null>(null);
  const [history, setHistory] = useState<TrackingEvent[]>([]);
  const [documents, setDocuments] = useState<TrackingDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [resolveStatus, setResolveStatus] = useState("IN_TRANSIT");

  const fetchData = async () => {
    if (!trackingNumber) return;
    setLoading(true);
    try {
      const [deliveryRes, historyRes, docsRes] = await Promise.all([
        api.get(`/deliveries/${trackingNumber}`),
        api.get(`/admin/dashboard/tracking/${trackingNumber}/history`),
        api.get(`/tracking/${trackingNumber}/documents`),
      ]);

      const deliveryData = deliveryRes.data?.data ?? deliveryRes.data;
      setDelivery(deliveryData);

      const historyData = historyRes.data?.data ?? historyRes.data;
      setHistory(Array.isArray(historyData) ? historyData : []);

      const docsData = docsRes.data?.data ?? docsRes.data;
      setDocuments(Array.isArray(docsData) ? docsData : []);

      if (deliveryData) {
        setResolveStatus(deliveryData.status);
      }
    } catch (err) {
      console.error(err);
      toast.error("Failed to load shipment data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [trackingNumber]);

  const handleUpdateStatus = async (status: string) => {
    if (delivery?.status === "DELIVERED") {
      toast.error("Order is already DELIVERED. Status cannot be changed.");
      return false;
    }

    setActionLoading(true);
    try {
      await api.put(`/admin/deliveries/${trackingNumber}/resolve?newStatus=${status}`);
      toast.success("Status updated successfully.");
      await fetchData();
      return true;
    } catch (err) {
      console.error(err);
      toast.error("Failed to update status.");
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleMarkDelivered = async (deliveryImage: File | null, deliveryNote: string) => {
    if (delivery?.status === "DELIVERED") {
      toast.error("Order is already marked as DELIVERED.");
      return false;
    }

    if (!deliveryImage) {
      toast.error("Proof of delivery image is mandatory.");
      return false;
    }

    setActionLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", deliveryImage);

      const uploadRes = await api.post(`/tracking/${trackingNumber}/documents`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const proofImagePath = uploadRes.data?.data?.id || uploadRes.data?.id;

      await api.put(`/admin/deliveries/${trackingNumber}/deliver`, null, {
        params: {
          proofImagePath,
          deliveryNote,
        },
      });

      toast.success("Shipment marked as DELIVERED successfully.");
      await fetchData();
      return true;
    } catch (err) {
      console.error(err);
      toast.error("Failed to finalize delivery.");
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleFileUpload = async (file: File | null) => {
    if (!file || !user || !trackingNumber) return false;

    setActionLoading(true);
    const formData = new FormData();
    formData.append("file", file);

    try {
      await api.post(`/tracking/${trackingNumber}/documents`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
          "X-User-Id": user.id,
          "X-User-Role": user.role,
        },
      });
      toast.success("Evidence document uploaded successfully.");
      await fetchData();
      return true;
    } catch (err) {
      console.error(err);
      toast.error("Failed to upload document.");
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  return {
    delivery,
    history,
    documents,
    loading,
    actionLoading,
    resolveStatus,
    setResolveStatus,
    handleUpdateStatus,
    handleMarkDelivered,
    handleFileUpload,
    refresh: fetchData,
  };
};
