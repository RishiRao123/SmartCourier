import React from 'react';
import { AlertTriangle, CheckCircle, XCircle } from 'lucide-react';
import Modal from './Modal';

interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  type?: 'danger' | 'success' | 'info';
  loading?: boolean;
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title, 
  message, 
  confirmText = 'Confirm', 
  type = 'info',
  loading = false
}) => {
  const getIcon = () => {
    switch (type) {
      case 'danger': return <AlertTriangle className="w-12 h-12 text-red-500" />;
      case 'success': return <CheckCircle className="w-12 h-12 text-green-500" />;
      default: return <AlertTriangle className="w-12 h-12 text-yellow-500" />;
    }
  };

  const getButtonClass = () => {
    switch (type) {
      case 'danger': return 'bg-red-600 hover:bg-red-700 text-white';
      case 'success': return 'bg-green-600 hover:bg-green-700 text-white';
      default: return 'bg-blue-900 hover:bg-blue-800 text-white';
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <div className="flex flex-col items-center text-center space-y-6">
        <div className={`p-4 rounded-3xl ${type === 'danger' ? 'bg-red-50' : type === 'success' ? 'bg-green-50' : 'bg-yellow-50'}`}>
          {getIcon()}
        </div>
        
        <div>
          <p className="text-gray-600 font-bold leading-relaxed">
            {message}
          </p>
        </div>

        <div className="flex flex-col w-full gap-3">
          <button
            onClick={onConfirm}
            disabled={loading}
            className={`w-full py-4 rounded-2xl font-black text-lg shadow-lg transition-all hover:-translate-y-1 flex items-center justify-center gap-2 ${getButtonClass()}`}
          >
            {loading && <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>}
            {confirmText}
          </button>
          <button
            onClick={onClose}
            disabled={loading}
            className="w-full py-4 rounded-2xl font-black text-lg text-gray-500 hover:bg-gray-100 transition-all"
          >
            Cancel
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default ConfirmationModal;
