import { Bell } from 'lucide-react';
import { useLocation } from 'react-router-dom';

const Header = () => {
  const location = useLocation();
  
  // Format pathname to display as title
  const getTitle = () => {
    const path = location.pathname.split('/')[1];
    if (!path) return 'Dashboard';
    return path.charAt(0).toUpperCase() + path.slice(1);
  };

  return (
    <header className="bg-white h-16 flex items-center justify-between px-6 shadow-sm border-b border-gray-200 z-10">
      <div className="flex items-center">
        <h2 className="text-xl font-semibold text-blue-900">{getTitle()}</h2>
      </div>
      <div className="flex items-center space-x-4">
        <button className="p-2 text-gray-500 hover:text-blue-900 transition-colors rounded-full hover:bg-gray-100">
          <Bell className="w-5 h-5" />
        </button>
        <div className="flex items-center space-x-2">
          <div className="w-8 h-8 bg-yellow-500 rounded-full flex items-center justify-center text-blue-900 font-bold shadow-md">
            A
          </div>
          <span className="text-sm font-medium text-gray-700 hidden sm:block">Admin User</span>
        </div>
      </div>
    </header>
  );
};

export default Header;
