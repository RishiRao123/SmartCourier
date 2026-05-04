import React from 'react';
import { CreditCard, CheckCircle, X, ShieldCheck } from 'lucide-react';

interface PaymentModalProps {
  isOpen: boolean;
  amount: number;
  onConfirm: () => void;
  onClose: () => void;
  loading: boolean;
}

const PaymentModal: React.FC<PaymentModalProps> = ({ isOpen, amount, onConfirm, onClose, loading }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[10000] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose}></div>
      <div className="bg-white w-full max-w-md rounded-[40px] shadow-2xl overflow-hidden relative z-10 animate-in fade-in zoom-in duration-300">
        
        {/* Header */}
        <div className="bg-[#071a2a] p-8 text-center relative overflow-hidden">
          <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-white transition-colors">
            <X className="w-6 h-6" />
          </button>
          <div className="w-20 h-20 bg-yellow-500 rounded-3xl flex items-center justify-center mx-auto mb-4 shadow-[0_0_40px_rgba(234,179,8,0.3)]">
            <CreditCard className="w-10 h-10 text-[#071a2a]" />
          </div>
          <h2 className="text-2xl font-black text-white mb-1">Online Payment</h2>
          <p className="text-gray-400 text-sm font-medium">Razorpay Integration Coming Soon</p>
        </div>

        {/* Body */}
        <div className="p-8">
          <div className="bg-yellow-500/10 border-2 border-yellow-500/30 rounded-3xl p-6 mb-6 text-center">
            <p className="text-xs font-black text-yellow-600 uppercase tracking-widest mb-2">Amount to Pay</p>
            <p className="text-5xl font-black text-[#071a2a]">₹{amount}</p>
          </div>
          
          <div className="bg-blue-50 rounded-2xl p-4 mb-6 flex items-start gap-3">
            <ShieldCheck className="w-5 h-5 text-blue-500 mt-0.5 flex-shrink-0" />
            <p className="text-sm text-blue-700 font-medium">
              This is a simulated payment. In production, this will connect to Razorpay's secure payment gateway.
            </p>
          </div>

          <button
            onClick={onConfirm}
            disabled={loading}
            className="w-full bg-yellow-500 text-[#071a2a] font-black py-5 rounded-2xl hover:bg-yellow-400 transition-all shadow-xl flex items-center justify-center gap-3 text-lg disabled:opacity-50 active:scale-95"
          >
            {loading ? 'Processing...' : (
              <>
                <CheckCircle className="w-6 h-6" />
                Simulate Payment
              </>
            )}
          </button>
          
          <button
            onClick={onClose}
            className="w-full mt-3 text-gray-400 font-bold py-3 hover:text-gray-600 transition-colors text-sm"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentModal;
