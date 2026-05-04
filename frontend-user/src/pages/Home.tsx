import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Package, ArrowRight, ShieldCheck, Search, MapPin, ChevronRight, Truck, IndianRupee, Scale, Clock, CheckCircle } from 'lucide-react';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [trackingNumber, setTrackingNumber] = useState('');

  useEffect(() => {
    if (location.hash === '#tracking-section') {
      window.scrollTo(0, 0); // Force scroll to top as requested
      const input = document.getElementById('tracking-input');
      if (input) {
        input.focus();
        // Optional: add a slight highlight animation
        input.classList.add('ring-4', 'ring-yellow-500/50');
        setTimeout(() => input.classList.remove('ring-4', 'ring-yellow-500/50'), 2000);
      }
    } else if (location.hash === '#how-it-works') {
      const element = document.getElementById('how-it-works');
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, [location.hash]);

  const handleTrack = (e: React.FormEvent) => {
    e.preventDefault();
    if (trackingNumber.trim()) {
      navigate(`/track/${trackingNumber}`);
    }
  };

  return (
    <div className="bg-[#071a2a] text-white overflow-hidden selection:bg-yellow-500 selection:text-[#071a2a]">
      {/* Hero Section */}
      <section className="relative min-h-[90vh] flex items-center pt-20">
        <div className="absolute top-1/2 right-0 -translate-y-1/2 translate-x-1/4 w-[800px] h-[800px] border border-white/5 rounded-full pointer-events-none"></div>
        
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <motion.div
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8 }}
          >
            <p className="text-yellow-500 font-bold tracking-[0.2em] uppercase text-sm mb-6">India's Trusted Logistics Partner</p>
            <h1 className="text-6xl md:text-7xl font-black leading-tight mb-8 text-white">
              Fastest Courier Service Across <span className="text-yellow-500">India</span>
            </h1>
            <p className="text-gray-400 text-lg mb-12 max-w-lg leading-relaxed">
              Experience Pan-India delivery with real-time tracking and zero exceptions. We handle your packages with care, from Kanyakumari to Kashmir.
            </p>

            <div id="tracking-section" className="mb-16 relative">
              <form onSubmit={handleTrack} className="flex bg-white/5 backdrop-blur-xl border border-white/10 p-2 rounded-2xl w-full sm:w-[450px] focus-within:border-yellow-500/50 transition-all group shadow-2xl">
                <input
                  id="tracking-input"
                  type="text"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  placeholder="Track Package"
                  className="bg-transparent border-none outline-none flex-1 px-6 text-xl text-white placeholder-gray-500 font-bold transition-all"
                />
                <button type="submit" className="bg-yellow-500 text-[#071a2a] p-4 rounded-xl hover:bg-yellow-400 transition-all active:scale-95 shadow-lg group">
                  <ArrowRight className="w-6 h-6 group-hover:translate-x-1 transition-transform" />
                </button>
              </form>
            </div>

            <div className="grid grid-cols-2 gap-12">
              <div className="bg-white/5 border border-white/5 p-8 rounded-3xl backdrop-blur-sm">
                <p className="text-4xl font-black mb-2 text-yellow-500">28</p>
                <p className="text-gray-400 text-sm font-bold uppercase tracking-widest">States Covered</p>
              </div>
              <div className="bg-white/5 border border-white/5 p-8 rounded-3xl backdrop-blur-sm">
                <p className="text-4xl font-black mb-2 text-white">2150+</p>
                <p className="text-gray-400 text-sm font-bold uppercase tracking-widest">Pin-codes Active</p>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 1, ease: "easeOut" }}
            className="relative lg:h-[800px] flex items-center justify-center"
          >
            <img 
              src="/hero.png" 
              alt="Courier Hero" 
              className="relative z-10 w-full h-auto object-contain drop-shadow-[0_35px_35px_rgba(0,0,0,0.4)] transform hover:scale-105 transition-transform duration-700 animate-bulge"
            />
          </motion.div>
        </div>
      </section>

      {/* How it Works Section */}
      <section id="how-it-works" className="py-32 bg-[#071a2a] border-t border-white/5 relative overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-20">
            <h2 className="text-4xl md:text-5xl font-black mb-6 text-white">How it Works</h2>
            <p className="text-gray-400 max-w-2xl mx-auto">Simple four-step process to get your package delivered anywhere in India.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 relative">
            <div className="absolute top-1/2 left-0 w-full h-0.5 bg-white/5 hidden md:block -translate-y-1/2"></div>
            {[
              { icon: <Search className="w-8 h-8" />, title: 'Book Shipment', desc: 'Enter details and get an instant quote.' },
              { icon: <Clock className="w-8 h-8" />, title: 'Schedule Pickup', desc: 'Choose a convenient time for doorstep pickup.' },
              { icon: <Truck className="w-8 h-8" />, title: 'Fast Transit', desc: 'Your package moves through our express network.' },
              { icon: <CheckCircle className="w-8 h-8" />, title: 'Safe Delivery', desc: 'Delivered safely with real-time proof.' }
            ].map((step, i) => (
              <motion.div
                key={i}
                whileHover={{ y: -15, scale: 1.05 }}
                className="relative bg-[#071a2a] p-8 flex flex-col items-center text-center z-10 rounded-[40px] border border-white/5 hover:border-yellow-500/30 transition-all cursor-default"
              >
                <div className="w-20 h-20 bg-yellow-500 rounded-3xl flex items-center justify-center text-[#071a2a] mb-6 shadow-[0_0_30px_rgba(255,199,44,0.2)] border-4 border-[#071a2a]">
                  {step.icon}
                </div>
                <h3 className="text-xl font-bold mb-3 text-white">{step.title}</h3>
                <p className="text-gray-400 text-sm">{step.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section className="py-32 bg-[#071a2a] border-t border-white/5">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-20">
            <div className="flex items-center justify-center gap-2 text-yellow-500 font-bold mb-4 uppercase tracking-widest text-sm">
              <IndianRupee className="w-4 h-4" />
              Simple & Transparent Pricing
            </div>
            <h2 className="text-4xl md:text-5xl font-black mb-6 text-white">Choose a Plan that Suits You</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {[
              { title: 'Standard Light', weight: 'Up to 500g', price: '49' },
              { title: 'Standard Plus', weight: '500g - 2kg', price: '99' },
              { title: 'Medium Heavy', weight: '2kg - 5kg', price: '199' },
              { title: 'Industrial', weight: 'Above 5kg', price: '399+' }
            ].map((plan, i) => (
              <motion.div
                key={i}
                whileHover={{ y: -10 }}
                className="bg-white/5 border border-white/5 p-10 rounded-[40px] hover:border-yellow-500/30 transition-all flex flex-col items-center text-center"
              >
                <h3 className="text-xl font-bold text-gray-400 mb-2 uppercase tracking-widest text-xs">{plan.title}</h3>
                <p className="text-sm font-medium text-gray-500 mb-6">{plan.weight}</p>
                <div className="flex items-baseline gap-1 mb-4">
                  <span className="text-2xl font-bold text-yellow-500">₹</span>
                  <span className="text-5xl font-black text-white">{plan.price}</span>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;
