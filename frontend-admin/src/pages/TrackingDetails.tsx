import React, { useState, useEffect } from "react";
import { formatDate } from "../utils/dateFormat";
import ReactDOM from "react-dom";
import { useParams, useNavigate } from "react-router-dom";
import {
  Package,
  Activity,
  CheckCircle,
  ArrowLeft,
  MapPin,
  User,
  Calendar,
  Upload,
  FileText,
  ShieldAlert,
  ChevronRight,
  Image as ImageIcon,
  Download,
  UserCheck,
  Shield,
  Eye,
  X,
  CreditCard,
  Phone,
  Navigation,
  Clock,
} from "lucide-react";
import toast from "react-hot-toast";
import api from "../services/api";
import { useAuth } from "../contexts/AuthContext";
import StatusBadge from "../components/StatusBadge";
import LoadingSpinner from "../components/LoadingSpinner";
import TrackingTimeline from "../components/TrackingTimeline";
import CustomDropdown from "../components/CustomDropdown";
import ConfirmationModal from "../components/ConfirmationModal";
import Modal from "../components/Modal";
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

const STATUS_OPTIONS = [
  { value: "DRAFT", label: "DRAFT" },
  { value: "BOOKED", label: "BOOKED" },
  { value: "PICKED_UP", label: "PICKED_UP" },
  { value: "IN_TRANSIT", label: "IN_TRANSIT" },
  { value: "OUT_FOR_DELIVERY", label: "OUT_FOR_DELIVERY" },
  { value: "DELAYED", label: "DELAYED" },
  { value: "FAILED", label: "FAILED" },
  { value: "RETURNED", label: "RETURNED" },
  { value: "EXCEPTION", label: "EXCEPTION" },
];

import { useTrackingDetails } from "../hooks/useTrackingDetails";
 
const TrackingDetails = () => {
  const { trackingNumber } = useParams<{ trackingNumber: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
 
  const {
    delivery,
    history,
    documents,
    loading,
    actionLoading,
    resolveStatus,
    setResolveStatus,
    handleUpdateStatus: apiUpdateStatus,
    handleMarkDelivered: apiMarkDelivered,
    handleFileUpload: apiFileUpload,
    refresh
  } = useTrackingDetails(trackingNumber);
 
  const [deliveryNote, setDeliveryNote] = useState("Hurray, delivered!!");
  const [deliveryImage, setDeliveryImage] = useState<File | null>(null);
  const [file, setFile] = useState<File | null>(null);
 
  // Modal States
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
  const [isDeliverModalOpen, setIsDeliverModalOpen] = useState(false);
 
  // Preview State
  const [previewDoc, setPreviewDoc] = useState<{
    url: string;
    type: string;
    name: string;
  } | null>(null);
 
  const handleUpdateStatus = async () => {
    const success = await apiUpdateStatus(resolveStatus);
    if (success) setIsStatusModalOpen(false);
  };
 
  const handleMarkDelivered = async () => {
    const success = await apiMarkDelivered(deliveryImage, deliveryNote);
    if (success) {
      setIsDeliverModalOpen(false);
      setDeliveryImage(null);
      setDeliveryNote("Hurray, delivered!!");
    }
  };
 
  const handleFileUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    const success = await apiFileUpload(file);
    if (success) setFile(null);
  };

  const handlePreview = async (doc: TrackingDocument) => {
    try {
      const response = await api.get(`/tracking/documents/${doc.id}/download`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      setPreviewDoc({ url, type: doc.fileType, name: doc.fileName });
    } catch (err) {
      console.error(err);
      toast.error("Failed to load preview");
    }
  };

  const downloadFile = (url: string, fileName: string) => {
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const openStatusModal = () => {
    if (delivery?.status === "DELIVERED") {
      toast.error("This shipment is already DELIVERED.");
      return;
    }
    setIsStatusModalOpen(true);
  };

  const openDeliverModal = () => {
    if (delivery?.status === "DELIVERED") {
      toast.error("This shipment is already DELIVERED.");
      return;
    }
    setIsDeliverModalOpen(true);
  };

  if (loading) {
    return <LoadingSpinner message="Loading details..." />;
  }

  if (!delivery) {
    return (
      <div className='text-center py-24 bg-white rounded-[40px] shadow-2xl border border-gray-100 max-w-2xl mx-auto mt-20'>
        <ShieldAlert className='w-24 h-24 text-red-500 mx-auto mb-8' />
        <h2 className='text-4xl font-black text-blue-900 tracking-tighter'>
          DATA NOT FOUND
        </h2>
        <p className='text-gray-500 mt-4 font-bold text-lg'>
          Shipment #{trackingNumber} not found.
        </p>
        <button
          onClick={() => navigate("/deliveries")}
          className='mt-10 bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black px-10 py-4 rounded-2xl shadow-xl hover:-translate-y-1 transition-all'
        >
          Go Back
        </button>
      </div>
    );
  }

  return (
    <div className='space-y-10 animate-fade-in-up pb-20'>
      <div className='flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6 bg-white p-8 rounded-[40px] shadow-sm border border-gray-100'>
        <div className='flex items-center gap-6'>
          <button
            onClick={() => navigate("/dashboard")}
            className='p-4 bg-gray-50 text-blue-900 rounded-2xl border border-gray-100 hover:bg-yellow-500 transition-all hover:scale-105'
          >
            <ArrowLeft className='w-6 h-6' />
          </button>
          <div>
            <h1 className='text-4xl font-black text-blue-900 tracking-tighter flex items-center gap-4'>
              Delivery Details
              <ChevronRight className='w-6 h-6 text-gray-300' />
              <span className='text-yellow-500'>
                #{delivery.trackingNumber}
              </span>
            </h1>
          </div>
        </div>
        <div className='bg-white p-2 rounded-3xl shadow-lg border border-gray-100 flex items-center gap-4 pr-6'>
          <div className='w-12 h-12 bg-yellow-500 rounded-2xl flex items-center justify-center shadow-md'>
            <Clock className='w-6 h-6 text-blue-900' />
          </div>
          <div className='flex flex-col'>
            <p className='text-[10px] font-black text-gray-400 uppercase tracking-widest'>
              Current Status
            </p>
            <StatusBadge status={delivery.status} />
          </div>
        </div>
      </div>

      <div className='grid grid-cols-1 xl:grid-cols-3 gap-10'>
        <div className='xl:col-span-2 space-y-10'>
          <div className='bg-white rounded-[40px] shadow-sm border border-gray-100 overflow-hidden'>
            <div className='bg-gray-50 p-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-6 border-b border-gray-100'>
              <div className='flex items-center gap-5'>
                <div className='p-4 bg-yellow-500 rounded-[20px] shadow-lg'>
                  <Package className='w-10 h-10 text-blue-900' />
                </div>
                <div>
                  <h3 className='text-3xl font-black tracking-tight text-yellow-500'>
                    Shipment Details
                  </h3>
                  <p className='text-gray-400 font-bold uppercase tracking-widest text-[10px]'>
                    Shipment Summary
                  </p>
                </div>
              </div>
              <div className='flex items-center gap-3 bg-white px-6 py-3 rounded-2xl border border-gray-100 text-sm font-black uppercase tracking-widest shadow-sm text-blue-900'>
                <Calendar className='w-5 h-5 text-yellow-500' /> Created:{" "}
                {formatDate(delivery.createdAt)}
              </div>
            </div>

            {/* Sender & Receiver Info - Cleaner Look */}
            <div className='p-10 bg-white'>
              <h3 className='text-xl font-black text-blue-900 mb-10 flex items-center gap-3 border-b border-gray-100 pb-4'>
                <Navigation className='w-6 h-6 text-yellow-500' /> Route Details
              </h3>

              <div className='grid grid-cols-1 md:grid-cols-2 gap-10'>
                <div className='space-y-8'>
                  <div>
                    <div className='flex items-center gap-3 mb-4'>
                      <div className='w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center border border-blue-100'>
                        <MapPin className='w-4 h-4 text-blue-600' />
                      </div>
                      <h4 className='text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                        Sender Address
                      </h4>
                    </div>
                    <div className='bg-gray-50/50 border border-gray-100 rounded-3xl p-6 hover:bg-white hover:shadow-xl transition-all duration-500 group'>
                      <p className='text-xl font-black text-blue-900 transition-colors'>
                        {delivery.senderName}
                      </p>
                      <p className='text-sm text-gray-500 font-bold mt-2 leading-relaxed'>
                        {delivery.senderAddress?.street},{" "}
                        {delivery.senderAddress?.city}
                      </p>
                    </div>
                  </div>

                  <div>
                    <div className='flex items-center gap-3 mb-4'>
                      <div className='w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center border border-blue-100'>
                        <MapPin className='w-4 h-4 text-blue-600' />
                      </div>
                      <h4 className='text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                        Receiver Address
                      </h4>
                    </div>
                    <div className='bg-gray-50/50 border border-gray-100 rounded-3xl p-6 hover:bg-white hover:shadow-xl transition-all duration-500 group'>
                      <p className='text-xl font-black text-blue-900 transition-colors'>
                        {delivery.receiverName}
                      </p>
                      <p className='text-sm text-gray-500 font-bold mt-2 leading-relaxed'>
                        {delivery.receiverAddress?.street},{" "}
                        {delivery.receiverAddress?.city}
                      </p>
                    </div>
                  </div>
                </div>

                <div className='space-y-6 flex flex-col justify-center border-l border-gray-100 pl-0 md:pl-10'>
                  <div className='bg-gray-50/50 border border-gray-100 p-6 rounded-3xl group hover:bg-white hover:shadow-lg transition-all'>
                    <div className='flex items-center gap-3 mb-2'>
                      <User className='w-4 h-4 text-blue-400' />
                      <span className='text-[10px] font-black text-gray-400 uppercase tracking-widest'>
                        Sender Details
                      </span>
                    </div>
                    <p className='text-lg font-black text-blue-900'>
                      {delivery.senderName}
                    </p>
                  </div>

                  <div className='bg-gray-50/50 border border-gray-100 p-6 rounded-3xl group hover:bg-white hover:shadow-lg transition-all'>
                    <div className='flex items-center gap-3 mb-2'>
                      <Phone className='w-4 h-4 text-blue-400' />
                      <span className='text-[10px] font-black text-gray-400 uppercase tracking-widest'>
                        Receiver Phone
                      </span>
                    </div>
                    <p className='text-lg font-black text-blue-900'>
                      {delivery.receiverPhone || "Unlisted"}
                    </p>
                  </div>

                  <div className='bg-yellow-500 text-[#071a2a] p-8 rounded-[32px] flex justify-between items-center shadow-2xl relative overflow-hidden group border-b-8 border-yellow-600'>
                    <div className='absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl group-hover:bg-white/20 transition-all'></div>
                    <div>
                      <span className='text-[10px] font-black uppercase tracking-widest text-blue-900/60 flex items-center gap-2'>
                        <CreditCard className='w-4 h-4' /> Settlement
                      </span>
                      <h4 className='text-4xl font-black mt-2 text-blue-900 tracking-tighter'>
                        ₹{delivery.price}
                      </h4>
                    </div>
                    <div className='text-right flex flex-col items-end'>
                      <span
                        className={`text-[10px] font-black px-4 py-1.5 rounded-full border ${delivery.paymentStatus === "PAID" ? "bg-green-500/20 text-green-700 border-green-500/30" : "bg-red-500/20 text-red-700 border-red-500/30"} tracking-widest uppercase mb-2`}
                      >
                        {delivery.paymentStatus || "N/A"}
                      </span>
                      <p className='text-[8px] font-bold text-blue-900/40 uppercase tracking-widest'>
                        {delivery.paymentMethod?.replace("_", " ") || ""}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className='bg-white rounded-[40px] shadow-sm border border-gray-100 p-10'>
            <h3 className='text-3xl font-black text-blue-900 mb-10 flex items-center gap-4'>
              <div className='w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-600 border border-blue-100'>
                <Package className='w-6 h-6' />
              </div>
              Tracking Timeline
            </h3>
            <TrackingTimeline history={history} loading={false} />
          </div>

          <div className='bg-white rounded-[40px] shadow-sm border border-gray-100 p-10'>
            <h3 className='text-3xl font-black text-blue-900 mb-10 flex items-center gap-4'>
              <div className='w-12 h-12 bg-green-50 rounded-2xl flex items-center justify-center text-green-600 border border-green-100'>
                <ImageIcon className='w-6 h-6' />
              </div>
              Documents & Images
            </h3>

            {documents.length === 0 ? (
              <div className='text-center py-12 bg-gray-50 rounded-[32px] border-2 border-dashed border-gray-200'>
                <p className='text-gray-400 font-bold uppercase tracking-widest text-sm'>
                  No proofs uploaded yet
                </p>
              </div>
            ) : (
              <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
                {documents.map((doc) => (
                  <div
                    key={doc.id}
                    className='bg-gray-50 p-6 rounded-[32px] border border-gray-100 group hover:bg-white hover:shadow-xl transition-all duration-300'
                  >
                    <div className='flex items-center gap-5'>
                      <div className='w-14 h-14 bg-white rounded-2xl shadow-sm flex items-center justify-center text-blue-900 border border-gray-100 group-hover:scale-110 transition-transform'>
                        {doc.fileType?.includes("image") ? (
                          <ImageIcon className='w-8 h-8' />
                        ) : (
                          <FileText className='w-8 h-8' />
                        )}
                      </div>
                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-2 mb-1'>
                          <p className='text-sm font-black text-gray-900 truncate uppercase tracking-tight'>
                            {doc.fileName}
                          </p>
                          {doc.uploaderRole === "ADMIN" ? (
                            <span className='flex items-center gap-1 bg-blue-100 text-blue-800 text-[8px] px-2 py-0.5 rounded-full font-black tracking-widest uppercase'>
                              <Shield className='w-2 h-2' /> Admin
                            </span>
                          ) : (
                            <span className='flex items-center gap-1 bg-green-100 text-green-800 text-[8px] px-2 py-0.5 rounded-full font-black tracking-widest uppercase'>
                              <UserCheck className='w-2 h-2' /> User
                            </span>
                          )}
                        </div>
                        <p className='text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-1'>
                          {doc.fileType}
                        </p>
                      </div>
                      <div className='flex gap-2'>
                        <button
                          onClick={() => handlePreview(doc)}
                          className='p-3 bg-white hover:bg-blue-900 hover:text-white rounded-xl shadow-sm border border-gray-100 transition-all'
                          title='Preview'
                        >
                          <Eye className='w-4 h-4' />
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className='space-y-10'>
          <div className='bg-white rounded-[40px] shadow-2xl border border-gray-100 p-10 relative overflow-hidden group'>
            <div className='absolute top-0 right-0 w-32 h-32 bg-blue-500/5 rounded-full filter blur-3xl group-hover:bg-blue-500/10 transition-all'></div>
            <h3 className='text-2xl font-black text-blue-900 mb-8 flex items-center gap-3'>
              <ShieldAlert className='w-8 h-8 text-blue-600' /> Update Shipment
              Status
            </h3>

            <div className='space-y-8'>
              <div className='space-y-4'>
                <label className='text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] ml-2'>
                  Shipment Status
                </label>
                <div className='relative'>
                  <CustomDropdown
                    options={STATUS_OPTIONS}
                    value={resolveStatus}
                    onChange={(val) => setResolveStatus(val)}
                  />
                </div>
                <button
                  onClick={openStatusModal}
                  disabled={actionLoading}
                  className='w-full bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-5 rounded-2xl transition-all shadow-xl uppercase tracking-widest text-sm'
                >
                  Submit Change
                </button>
              </div>

              <div className='relative py-4'>
                <div className='absolute inset-0 flex items-center'>
                  <div className='w-full border-t-2 border-gray-50'></div>
                </div>
                <div className='relative flex justify-center text-[10px] font-black text-gray-300 bg-white px-4 tracking-widest'>
                  FINAL ACTION
                </div>
              </div>

              <button
                onClick={openDeliverModal}
                disabled={actionLoading || delivery.status === "DELIVERED"}
                className='w-full bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-6 rounded-2xl transition-all shadow-2xl flex items-center justify-center gap-4 text-lg border-b-4 border-yellow-600 disabled:opacity-70'
              >
                <CheckCircle className='w-8 h-8' /> MARK DELIVERED
              </button>
            </div>
          </div>

          <div className='bg-white rounded-[40px] shadow-2xl border border-gray-100 p-10'>
            <h3 className='text-2xl font-black text-blue-900 mb-8 flex items-center gap-3'>
              <FileText className='w-8 h-8 text-green-500' /> Upload Document
            </h3>
            <form onSubmit={handleFileUpload} className='space-y-6'>
              <div
                className={`border-4 border-dashed rounded-[32px] p-10 transition-all flex flex-col items-center text-center relative cursor-pointer ${file ? "border-yellow-500 bg-yellow-50/50" : "border-gray-100 bg-gray-50 hover:bg-gray-100"}`}
              >
                <input
                  type='file'
                  onChange={(e) => setFile(e.target.files?.[0] || null)}
                  className='absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10'
                />
                <div
                  className={`w-16 h-16 rounded-2xl flex items-center justify-center mb-4 shadow-sm ${file ? "bg-yellow-500 text-blue-900" : "bg-white text-gray-300"}`}
                >
                  <Upload className='w-8 h-8' />
                </div>
                <p className='text-sm font-black text-gray-900 leading-tight'>
                  {file ? file.name : "Select Proof Document"}
                </p>
                <p className='text-[10px] font-bold text-gray-400 mt-2 uppercase tracking-widest'>
                  DOC / PDF / IMG MAX 10MB
                </p>
              </div>
              <button
                type='submit'
                disabled={!file || actionLoading}
                className='w-full bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-5 rounded-2xl transition-all disabled:opacity-50 shadow-lg'
              >
                Upload Document
              </button>
            </form>
          </div>
        </div>
      </div>

      <ConfirmationModal
        isOpen={isStatusModalOpen}
        onClose={() => setIsStatusModalOpen(false)}
        onConfirm={handleUpdateStatus}
        title='Update Shipment Status'
        message={`Are you sure you want to change the status to ${resolveStatus}? This action will be logged.`}
        confirmText='Confirm'
        loading={actionLoading}
      />

      {/* Custom Mark as Delivered Modal */}
      {isDeliverModalOpen &&
        ReactDOM.createPortal(
          <div className='fixed inset-0 z-[100000] flex items-center justify-center p-4'>
            <div
              className='absolute inset-0 bg-blue-950/20 backdrop-blur-md animate-fade-in'
              onClick={() => setIsDeliverModalOpen(false)}
            ></div>
            <div className='bg-white w-full max-w-lg rounded-[40px] shadow-2xl overflow-hidden relative z-10 animate-scale-up border border-gray-100'>
              <div className='bg-green-600 p-8 text-center text-white relative overflow-hidden'>
                <div className='absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-10 -mt-10 blur-2xl'></div>
                <CheckCircle className='w-16 h-16 mx-auto mb-4 drop-shadow-lg' />
                <h2 className='text-3xl font-black tracking-tighter'>
                  Finalize Delivery
                </h2>
                <p className='text-green-100 font-bold opacity-80 mt-1'>
                  Upload proof and add a delivery note
                </p>
              </div>

              <div className='p-6 space-y-4'>
                <div>
                  <label className='block text-[10px] font-black text-gray-400 mb-1 uppercase tracking-widest'>
                    Proof of Delivery (Mandatory)
                  </label>
                  <div
                    className={`relative border-2 border-dashed rounded-3xl p-6 transition-all ${deliveryImage ? "border-green-500 bg-green-50" : "border-gray-200 hover:border-blue-400 bg-gray-50"}`}
                  >
                    <input
                      type='file'
                      accept='image/*'
                      onChange={(e) =>
                        setDeliveryImage(e.target.files?.[0] || null)
                      }
                      className='absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10'
                    />
                    <div className='text-center'>
                      {deliveryImage ? (
                        <div className='flex flex-col items-center'>
                          <ImageIcon className='w-8 h-8 text-green-600 mb-1' />
                          <p className='text-xs font-black text-green-900 truncate max-w-full px-2'>
                            {deliveryImage.name}
                          </p>
                        </div>
                      ) : (
                        <div className='flex flex-col items-center'>
                          <Upload className='w-8 h-8 text-gray-300 mb-1' />
                          <p className='text-xs font-bold text-gray-500'>
                            Click to upload proof
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                <div>
                  <label className='block text-[10px] font-black text-gray-400 mb-1 uppercase tracking-widest'>
                    Delivery Note
                  </label>
                  <textarea
                    value={deliveryNote}
                    onChange={(e) => setDeliveryNote(e.target.value)}
                    placeholder='Enter a note...'
                    className='w-full bg-gray-50 border-2 border-transparent focus:bg-white focus:border-green-500 rounded-2xl p-3 text-sm font-bold text-blue-900 outline-none transition-all min-h-[80px] resize-none'
                  />
                </div>

                <div className='flex flex-col gap-2 pt-1'>
                  <button
                    onClick={handleMarkDelivered}
                    disabled={actionLoading || !deliveryImage}
                    className='w-full bg-green-600 text-white font-black py-3 rounded-2xl shadow-xl hover:bg-green-700 transition-all flex items-center justify-center gap-2 text-base disabled:opacity-50'
                  >
                    {actionLoading ? (
                      <div className='w-5 h-5 border-3 border-white border-t-transparent rounded-full animate-spin'></div>
                    ) : (
                      <>
                        <CheckCircle className='w-5 h-5' />
                        Complete Delivery
                      </>
                    )}
                  </button>
                  <button
                    onClick={() => setIsDeliverModalOpen(false)}
                    disabled={actionLoading}
                    className='w-full py-2 rounded-xl font-black text-gray-400 hover:bg-gray-100 transition-all text-sm'
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>,
          document.body,
        )}

      {/* Preview Modal */}
      {previewDoc &&
        ReactDOM.createPortal(
          <div className='fixed inset-0 z-[100000] flex items-center justify-center p-4'>
            <div
              className='absolute inset-0 bg-blue-950/20 backdrop-blur-md animate-fade-in'
              onClick={() => setPreviewDoc(null)}
            ></div>
            <div className='bg-white w-full max-w-5xl rounded-[40px] shadow-2xl overflow-hidden relative z-10 animate-scale-up border border-gray-100 flex flex-col max-h-[95vh]'>
              <div className='p-8 border-b border-gray-100 flex justify-between items-center bg-white'>
                <div>
                  <h3 className='text-2xl font-black text-blue-900 tracking-tighter'>
                    {previewDoc.name}
                  </h3>
                  <p className='text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-1'>
                    {previewDoc.type}
                  </p>
                </div>
                <div className='flex items-center gap-3'>
                  <button
                    onClick={() =>
                      downloadFile(previewDoc.url, previewDoc.name)
                    }
                    className='p-4 bg-yellow-500 text-blue-900 rounded-2xl hover:bg-yellow-400 transition-all shadow-lg flex items-center gap-2 font-black text-sm uppercase tracking-widest'
                  >
                    <Download className='w-5 h-5' /> Download
                  </button>
                  <button
                    onClick={() => setPreviewDoc(null)}
                    className='p-4 bg-gray-100 hover:bg-gray-200 rounded-2xl transition-all text-gray-400 hover:text-blue-900'
                  >
                    <X className='w-6 h-6' />
                  </button>
                </div>
              </div>
              <div className='flex-1 overflow-auto bg-gray-50 flex items-center justify-center p-10'>
                {previewDoc.type.includes("image") ? (
                  <img
                    src={previewDoc.url}
                    alt={previewDoc.name}
                    className='max-w-full max-h-full object-contain rounded-2xl shadow-xl border border-gray-100'
                  />
                ) : previewDoc.type.includes("pdf") ? (
                  <iframe
                    src={previewDoc.url}
                    className='w-full h-full min-h-[70vh] rounded-2xl border border-gray-100 shadow-xl'
                    title='PDF Preview'
                  />
                ) : (
                  <div className='text-center p-20'>
                    <FileText className='w-32 h-32 text-blue-200 mx-auto mb-8' />
                    <p className='text-xl font-black text-blue-900'>
                      Preview not available for this file type.
                    </p>
                    <button
                      onClick={() =>
                        downloadFile(previewDoc.url, previewDoc.name)
                      }
                      className='mt-8 bg-yellow-500 text-blue-900 font-black px-10 py-4 rounded-2xl hover:bg-yellow-400 transition-all shadow-xl'
                    >
                      Download to View
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>,
          document.body,
        )}
    </div>
  );
};

export default TrackingDetails;
