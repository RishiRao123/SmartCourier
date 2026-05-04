import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  Truck,
  ArrowRight,
  Mail,
  KeyRound,
  Eye,
  EyeOff,
  ShieldCheck,
  ArrowLeft,
  Shield,
} from "lucide-react";
import toast from "react-hot-toast";
import api from "../services/api";

type Step = "email" | "otp" | "newPassword";

const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSendCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      toast.error("Please enter your email.");
      return;
    }
    setLoading(true);
    try {
      await api.post("/auth/forgot-password", { email });
      toast.success(
        "If this email is registered, you'll receive a reset code.",
      );
      setStep("otp");
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp) {
      toast.error("Please enter the reset code.");
      return;
    }
    if (!newPassword) {
      toast.error("Please enter a new password.");
      return;
    }
    if (newPassword.length < 6) {
      toast.error("Password must be at least 6 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error("Passwords do not match.");
      return;
    }
    setLoading(true);
    try {
      await api.post("/auth/reset-password", { email, otp, newPassword });
      toast.success("Password reset successfully! Please log in.");
      navigate("/login");
    } catch (err: any) {
      toast.error(
        err.response?.data?.message || "Invalid or expired reset code.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className='min-h-screen flex font-sans bg-white'>
      {/* Left Pane - Branding & Image */}
      <div className='hidden lg:flex w-1/2 bg-[#071a2a] flex-col justify-center items-center relative overflow-hidden p-12'>
        <div className='absolute top-0 left-0 w-full h-full pointer-events-none'>
          <div className='absolute top-0 left-0 w-[50%] h-[50%] bg-blue-500/10 rounded-full blur-[100px]'></div>
          <div className='absolute bottom-0 right-0 w-[50%] h-[50%] bg-yellow-500/10 rounded-full blur-[100px]'></div>
        </div>

        <div className='relative z-10 w-full max-w-lg flex flex-col items-center text-center'>
          <div
            className='flex items-center justify-center gap-4 mb-8 group cursor-pointer'
            onClick={() => navigate("/")}
          >
            <div className='w-16 h-16 bg-yellow-500 rounded-2xl flex items-center justify-center shadow-[0_0_40px_rgba(255,199,44,0.3)] transform group-hover:-translate-y-2 group-hover:rotate-[5deg] transition-all duration-500'>
              <Truck className='w-8 h-8 text-[#071a2a]' />
            </div>
            <h1 className='text-4xl font-extrabold text-white tracking-tight'>
              Smart<span className='text-yellow-500'>Courier</span>
              <span className='text-xs ml-2 text-yellow-500 font-extrabold tracking-widest uppercase align-top'>
                Admin
              </span>
            </h1>
          </div>
          <p className='text-gray-300 text-lg mb-12 max-w-md'>
            Admin Security. Reset your administrative credentials to maintain full control over the delivery network.
          </p>

          <img 
            src="/delivery-hero.png" 
            alt="Delivery Hero" 
            className="w-full max-w-sm h-auto object-contain hover:scale-105 transition-transform duration-700 pointer-events-none rounded-[60px] mb-8 animate-bulge"
            style={{ 
              maskImage: 'radial-gradient(circle, black 60%, transparent 100%)',
              WebkitMaskImage: 'radial-gradient(circle, black 60%, transparent 100%)'
            }}
          />
        </div>
      </div>

      {/* Right Pane - Form */}
      <div className='w-full lg:w-1/2 flex flex-col justify-center items-center p-8 sm:p-12 lg:p-24 relative'>
        <div className='absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0 lg:hidden'>
          <div className='absolute -top-[10%] -left-[10%] w-[50%] h-[50%] bg-yellow-500/5 rounded-full blur-[100px]'></div>
        </div>

        <div className='w-full max-w-md relative z-10'>
          <div className='mb-10 text-center lg:text-left'>
            <div className='inline-flex items-center gap-2 bg-yellow-50 text-yellow-600 px-4 py-2 rounded-full text-xs font-black uppercase tracking-widest mb-6 lg:hidden'>
              <Shield className='w-4 h-4' /> Admin Portal
            </div>
            <h2 className='text-3xl lg:text-4xl font-black text-[#071a2a] mb-3'>
              {step === "email" ? "Password" : "Reset"}{" "}
              <span className='text-yellow-500'>
                {step === "email" ? "Recovery" : "Password"}
              </span>
            </h2>
            <p className='text-gray-500 font-medium'>
              {step === "email"
                ? "Enter your admin email to receive a reset code."
                : `Reset code sent to ${email}`}
            </p>
          </div>

          {/* Step 1: Email */}
          {step === "email" && (
            <form onSubmit={handleSendCode} className='space-y-6'>
              <div>
                <label className='block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest'>
                  Admin Email
                </label>
                <div className='relative'>
                  <Mail className='absolute left-6 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400' />
                  <input
                    type='email'
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className='w-full pl-14 pr-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]'
                    placeholder='admin@email.com'
                    required
                  />
                </div>
              </div>

              <button
                type='submit'
                disabled={loading}
                className='w-full bg-[#071a2a] hover:bg-[#0a243a] text-white font-black py-5 rounded-2xl shadow-xl hover:-translate-y-1 transition-all flex items-center justify-center gap-3 text-lg disabled:opacity-50 mt-8 group'
              >
                {loading ? "Sending..." : "Send Reset Code"}
                {!loading && (
                  <ArrowRight className='w-6 h-6 transform group-hover:translate-x-1.5 transition-transform' />
                )}
              </button>
            </form>
          )}

          {/* Step 2: OTP + New Password */}
          {(step === "otp" || step === "newPassword") && (
            <form onSubmit={handleResetPassword} className='space-y-6'>
              <div>
                <label className='block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest'>
                  Reset Code
                </label>
                <input
                  type='text'
                  maxLength={6}
                  value={otp}
                  onChange={(e) => {
                    setOtp(e.target.value.replace(/\D/g, ""));
                    if (e.target.value.length === 6) setStep("newPassword");
                  }}
                  className='w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-black text-center text-2xl tracking-[0.5em] text-[#071a2a]'
                  placeholder='000000'
                  required
                />
              </div>

              {step === "newPassword" && (
                <div className='space-y-6 animate-in fade-in slide-in-from-top-4 duration-500'>
                  <div>
                    <label className='block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest'>
                      New Password
                    </label>
                    <div className='relative'>
                      <KeyRound className='absolute left-6 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400' />
                      <input
                        type={showPassword ? "text" : "password"}
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        className='w-full pl-14 pr-12 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]'
                        placeholder='••••••••'
                        required
                      />
                      <button
                        type='button'
                        onClick={() => setShowPassword(!showPassword)}
                        className='absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-yellow-500 transition-colors'
                      >
                        {showPassword ? (
                          <EyeOff className='w-5 h-5' />
                        ) : (
                          <Eye className='w-5 h-5' />
                        )}
                      </button>
                    </div>
                  </div>

                  <div>
                    <label className='block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest'>
                      Confirm Password
                    </label>
                    <div className='relative'>
                      <KeyRound className='absolute left-6 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400' />
                      <input
                        type={showPassword ? "text" : "password"}
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className='w-full pl-14 pr-12 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]'
                        placeholder='••••••••'
                        required
                      />
                    </div>
                  </div>
                </div>
              )}

              <button
                type='submit'
                disabled={loading || (step === "newPassword" && !newPassword)}
                className='w-full bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-5 rounded-2xl shadow-xl hover:-translate-y-1 transition-all flex items-center justify-center gap-3 text-lg disabled:opacity-50 mt-8 group'
              >
                {loading ? "Resetting..." : "Reset Password"}
                {!loading && (
                  <ArrowRight className='w-6 h-6 transform group-hover:translate-x-1.5 transition-transform' />
                )}
              </button>
            </form>
          )}

          {/* Back to Login */}
          <div className='mt-8 text-center'>
            <Link
              to='/login'
              className='inline-flex items-center gap-2 text-sm font-bold text-gray-400 hover:text-[#071a2a] transition-colors'
            >
              <ArrowLeft className='w-4 h-4' /> Back to Admin Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
