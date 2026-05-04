import React, { useState, useRef, useEffect } from 'react';
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight } from 'lucide-react';

interface CustomDatePickerProps {
  value: string;
  onChange: (value: string) => void;
  label?: string;
  placeholder?: string;
}

const CustomDatePicker: React.FC<CustomDatePickerProps> = ({ value, onChange, label, placeholder }) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  
  const [viewDate, setViewDate] = useState(value ? new Date(value) : new Date());
  
  const daysInMonth = (year: number, month: number) => new Date(year, month + 1, 0).getDate();
  const firstDayOfMonth = (year: number, month: number) => new Date(year, month, 1).getDay();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleDateSelect = (day: number) => {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth() + 1; // Month is 0-indexed
    
    // Format to YYYY-MM-DD manually to prevent UTC timezone shifts
    const formatted = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    
    onChange(formatted);
    setIsOpen(false);
  };

  const changeMonth = (offset: number) => {
    setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() + offset, 1));
  };

  const renderDays = () => {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth();
    const days = [];
    const firstDay = firstDayOfMonth(year, month);
    const totalDays = daysInMonth(year, month);

    // Empty slots for days of previous month
    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="h-10 w-10"></div>);
    }

    for (let d = 1; d <= totalDays; d++) {
      const isSelected = value === `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      days.push(
        <div
          key={d}
          onClick={() => handleDateSelect(d)}
          className={`h-10 w-10 flex items-center justify-center rounded-xl cursor-pointer font-bold text-sm transition-all hover:bg-yellow-500 hover:text-blue-900 ${isSelected ? 'bg-yellow-500 text-blue-900' : 'text-gray-600'}`}
        >
          {d}
        </div>
      );
    }
    return days;
  };

  return (
    <div className="relative w-full" ref={dropdownRef}>
      {label && (
        <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1 mb-1.5">
          {label}
        </label>
      )}
      <div 
        onClick={() => setIsOpen(!isOpen)}
        className={`w-full bg-gray-50 border-2 rounded-2xl py-3 px-4 font-black text-blue-900 cursor-pointer flex justify-between items-center transition-all hover:border-gray-200 ${isOpen ? 'bg-white border-yellow-500 ring-4 ring-yellow-500/10 shadow-lg' : 'border-gray-100'}`}
      >
        <span className={!value ? 'text-gray-400' : ''}>
          {value ? new Date(value).toLocaleDateString() : placeholder || 'Select date'}
        </span>
        <CalendarIcon className={`w-5 h-5 text-gray-400 transition-transform ${isOpen ? 'text-yellow-500' : ''}`} />
      </div>

      {isOpen && (
        <div className="absolute z-[100] mt-2 w-72 bg-white border border-gray-100 rounded-[32px] shadow-2xl p-6 animate-scale-up origin-top">
          <div className="flex justify-between items-center mb-6">
            <button onClick={() => changeMonth(-1)} className="p-2 hover:bg-gray-100 rounded-xl text-gray-400 transition-all"><ChevronLeft className="w-5 h-5" /></button>
            <h4 className="font-black text-blue-900 tracking-tight">
              {viewDate.toLocaleString('default', { month: 'long', year: 'numeric' })}
            </h4>
            <button onClick={() => changeMonth(1)} className="p-2 hover:bg-gray-100 rounded-xl text-gray-400 transition-all"><ChevronRight className="w-5 h-5" /></button>
          </div>
          
          <div className="grid grid-cols-7 gap-1 text-center mb-2">
            {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(day => (
              <div key={day} className="text-[10px] font-black text-gray-300 uppercase tracking-widest">{day}</div>
            ))}
          </div>
          
          <div className="grid grid-cols-7 gap-1">
            {renderDays()}
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomDatePicker;
