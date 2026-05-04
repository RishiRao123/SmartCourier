import React, { useEffect, useState } from 'react';
import { formatDate } from '../utils/dateFormat';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { FileText, Package, ArrowLeft, CheckCircle, Clock, IndianRupee, User, MapPin, Scale, Printer } from 'lucide-react';
import { invoiceService } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import type { Invoice } from '../services/api';

const InvoicePage: React.FC = () => {
  const { trackingNumber } = useParams<{ trackingNumber: string }>();
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchInvoice = async () => {
      if (!trackingNumber) return;
      try {
        const data = await invoiceService.getInvoice(trackingNumber);
        setInvoice(data);
      } catch (err) {
        console.error('Failed to load invoice:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchInvoice();
  }, [trackingNumber]);

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <LoadingSpinner message="Printing your receipt..." />
      </div>
    );
  }

  if (!invoice) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <FileText className="w-20 h-20 text-gray-200 mx-auto mb-6" />
          <h2 className="text-2xl font-black text-[#071a2a] mb-2">Invoice Not Found</h2>
          <Link to="/dashboard" className="text-yellow-500 font-bold hover:underline">Back to Dashboard</Link>
        </div>
      </div>
    );
  }

  const isPaid = invoice.paymentStatus === 'PAID';

  return (
    <div className="min-h-screen bg-white py-12">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center mb-8">
          <Link to={`/track/${trackingNumber}`} className="flex items-center gap-2 text-[#071a2a] font-bold hover:text-yellow-500 transition-colors">
            <ArrowLeft className="w-5 h-5" /> Back to Tracking
          </Link>
          <button
            onClick={() => window.print()}
            className="flex items-center gap-2 bg-gray-100 text-[#071a2a] px-6 py-3 rounded-2xl font-bold hover:bg-gray-200 transition-all text-sm"
          >
            <Printer className="w-4 h-4" /> Print Invoice
          </button>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-[40px] border-4 border-gray-50 shadow-sm overflow-hidden"
        >
          {/* Invoice Header */}
          <div className="bg-[#071a2a] p-10 text-white">
            <div className="flex justify-between items-start">
              <div>
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 bg-yellow-500 rounded-2xl flex items-center justify-center">
                    <Package className="w-6 h-6 text-[#071a2a]" />
                  </div>
                  <span className="text-2xl font-black">Smart<span className="text-yellow-500">Courier</span></span>
                </div>
                <p className="text-gray-400 text-xs font-bold uppercase tracking-widest">Tax Invoice</p>
              </div>
              <div className="text-right">
                <p className="text-xs text-gray-400 font-bold uppercase tracking-widest mb-1">Invoice No.</p>
                <p className="text-2xl font-black text-yellow-500">{invoice.invoiceNumber}</p>
                <p className="text-xs text-gray-400 mt-2 font-medium">
                  {formatDate(invoice.createdAt)}
                </p>
              </div>
            </div>
          </div>

          {/* Payment Status Banner */}
          <div className={`px-10 py-4 flex items-center justify-between ${isPaid ? 'bg-green-50 border-b border-green-100' : 'bg-yellow-50 border-b border-yellow-100'}`}>
            <div className="flex items-center gap-3">
              {isPaid ? <CheckCircle className="w-5 h-5 text-green-500" /> : <Clock className="w-5 h-5 text-yellow-600" />}
              <span className={`font-black text-sm uppercase tracking-widest ${isPaid ? 'text-green-600' : 'text-yellow-600'}`}>
                {isPaid ? 'Payment Complete' : 'Pay on Delivery'}
              </span>
            </div>
            <span className={`text-xs font-bold ${isPaid ? 'text-green-500' : 'text-yellow-500'}`}>
              {invoice.paymentMethod?.replace('_', ' ')}
            </span>
          </div>

          {/* Details */}
          <div className="p-10">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10">
              <div className="bg-gray-50 p-6 rounded-3xl">
                <div className="flex items-center gap-3 mb-4">
                  <User className="w-5 h-5 text-yellow-500" />
                  <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">From (Sender)</p>
                </div>
                <p className="text-lg font-black text-[#071a2a]">{invoice.senderName}</p>
              </div>
              <div className="bg-gray-50 p-6 rounded-3xl">
                <div className="flex items-center gap-3 mb-4">
                  <MapPin className="w-5 h-5 text-yellow-500" />
                  <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">To (Receiver)</p>
                </div>
                <p className="text-lg font-black text-[#071a2a]">{invoice.receiverName}</p>
                <p className="text-sm text-gray-500 font-medium">{invoice.receiverCity}</p>
              </div>
            </div>

            {/* Line Items */}
            <div className="border-2 border-gray-50 rounded-3xl overflow-hidden mb-8">
              <table className="w-full">
                <thead>
                  <tr className="bg-gray-50">
                    <th className="text-left text-[10px] font-black text-gray-400 uppercase tracking-widest px-6 py-4">Description</th>
                    <th className="text-center text-[10px] font-black text-gray-400 uppercase tracking-widest px-6 py-4">Weight</th>
                    <th className="text-center text-[10px] font-black text-gray-400 uppercase tracking-widest px-6 py-4">Tracking</th>
                    <th className="text-right text-[10px] font-black text-gray-400 uppercase tracking-widest px-6 py-4">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  <tr className="border-t border-gray-50">
                    <td className="px-6 py-5">
                      <p className="font-bold text-[#071a2a]">{invoice.description || 'Standard Shipment'}</p>
                      <p className="text-xs text-gray-400 mt-1">Courier Delivery Service</p>
                    </td>
                    <td className="px-6 py-5 text-center">
                      <span className="font-bold text-[#071a2a]">{invoice.weightKg} kg</span>
                    </td>
                    <td className="px-6 py-5 text-center">
                      <span className="text-xs font-black text-yellow-500 bg-yellow-50 px-3 py-1 rounded-full">{invoice.trackingNumber}</span>
                    </td>
                    <td className="px-6 py-5 text-right">
                      <span className="font-black text-[#071a2a] text-lg">₹{invoice.amount}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            {/* Total */}
            <div className="flex justify-center">
              <div className="bg-[#071a2a] text-white p-8 rounded-3xl w-full md:w-80 shadow-xl border-4 border-yellow-500/20">
                <div className="flex justify-between items-center mb-4">
                  <span className="text-gray-400 text-sm font-bold">Subtotal</span>
                  <span className="font-bold text-lg">₹{invoice.amount}</span>
                </div>
                <div className="flex justify-between items-center mb-4">
                  <span className="text-gray-400 text-sm font-bold">GST (Included)</span>
                  <span className="font-bold text-gray-400">₹0</span>
                </div>
                <div className="border-t border-white/10 pt-4 flex justify-between items-center">
                  <span className="font-black text-yellow-500 uppercase tracking-widest text-sm">Total Amount</span>
                  <span className="text-3xl font-black text-yellow-500 flex items-center">
                    <IndianRupee className="w-5 h-5" />{invoice.amount}
                  </span>
                </div>
              </div>
            </div>

            {/* Footer */}
            <div className="mt-10 pt-8 border-t border-gray-100 text-center">
              <p className="text-xs text-gray-400 font-medium">SmartCourier Logistics India Pvt. Ltd.</p>
              <p className="text-xs text-gray-300 mt-1">This is a computer-generated invoice and does not require a signature.</p>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default InvoicePage;
