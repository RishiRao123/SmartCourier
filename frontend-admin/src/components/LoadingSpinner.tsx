import React from 'react';

interface LoadingSpinnerProps {
  message?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ message = 'Loading...' }) => {
  return (
    <div className='flex flex-col items-center justify-center min-h-[40vh] py-20 animate-fade-in'>
      <div className='relative w-24 h-24 mb-10'>
        {/* outer ring */}
        <div className='absolute inset-0 border-[10px] border-gray-100 rounded-[30px] rotate-45'></div>
        {/* inner spinning ring */}
        <div className='absolute inset-0 border-[10px] border-yellow-500 border-t-transparent rounded-[30px] animate-spin rotate-45 shadow-[0_0_20px_rgba(255,199,44,0.4)]'></div>
      </div>
      <p className='text-blue-900 font-black text-2xl tracking-[0.2em] animate-pulse uppercase'>
        {message}
      </p>
      <div className="w-12 h-1 bg-yellow-500 rounded-full mt-4 animate-bounce"></div>
    </div>
  );
};

export default LoadingSpinner;
