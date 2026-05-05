import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  Truck,
  ArrowRight,
  ShieldCheck,
  Eye,
  EyeOff,
  KeyRound,
} from "lucide-react";
import toast from "react-hot-toast";
import { api, authService } from "../services/api";
import LoadingSpinner from "../components/LoadingSpinner";

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otp, setOtp] = useState("");
  const [verifying, setVerifying] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      toast.error("Email and password are required.");
      return;
    }
    setLoading(true);
    try {
      const response = await api.post("/auth/login", { email, password });
      let token = response.data?.data || response.data?.token || response.data;
      if (typeof token === "string" && token.startsWith("token: ")) {
        token = token.replace("token: ", "").trim();
      }

      // Role check
      try {
        const payloadBase64 = token.split(".")[1];
        const payload = JSON.parse(atob(payloadBase64));
        if (
          payload.role === "ROLE_ADMIN" ||
          payload.role === "ROLE_SUPER_ADMIN"
        ) {
          toast.error("Admins are not allowed to login here.");
          setLoading(false);
          return;
        }
      } catch (e) {
        // Ignore parse errors, fallback to normal flow
      }

      localStorage.setItem("user_token", token);
      toast.success("Login Successful!");
      setTimeout(() => navigate("/"), 1000);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || "Login failed.";
      if (errorMessage.includes("Please verify your email address")) {
        toast.error(
          "Account not verified. Please check your email for the OTP.",
        );
        // Try resending automatically or just show modal
        try {
          await authService.resendOtp(email);
          toast.success("A new OTP has been sent to your email.");
        } catch (e) {}
        setShowOtpModal(true);
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp) {
      toast.error("Please enter the OTP");
      return;
    }
    setVerifying(true);
    try {
      await authService.verifyOtp(email, otp);
      toast.success("Email verified successfully! Logging you in...");
      setShowOtpModal(false);
      // Automatically attempt login again
      await handleLogin(new Event("submit") as unknown as React.FormEvent);
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Verification failed.");
    } finally {
      setVerifying(false);
    }
  };

  const handleResendOtp = async () => {
    try {
      await authService.resendOtp(email);
      toast.success("New OTP sent to your email!");
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to resend OTP.");
    }
  };

  return (
    <div className='min-h-screen flex font-sans bg-white relative'>
      {loading && (
        <div className='fixed inset-0 z-[100] bg-white/80 backdrop-blur-sm flex items-center justify-center'>
          <LoadingSpinner message='Checking your keys...' />
        </div>
      )}
      {verifying && (
        <div className='fixed inset-0 z-[100] bg-white/80 backdrop-blur-sm flex items-center justify-center'>
          <LoadingSpinner message="Making sure it's you..." />
        </div>
      )}
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
            <h1 className='text-4xl font-black text-white tracking-tight'>
              Smart<span className='text-yellow-500'>Courier</span>
            </h1>
          </div>
          <p className='text-gray-300 text-lg mb-12 max-w-md'>
            The most reliable delivery network in India. Sign in to manage your
            shipments and track in real-time.
          </p>

          <img
            src='/delivery-hero.png'
            alt='Delivery Hero'
            className='w-full max-w-sm h-auto object-contain hover:scale-105 transition-transform duration-700 pointer-events-none rounded-[60px] mb-8 animate-bulge'
            style={{
              maskImage: "radial-gradient(circle, black 60%, transparent 100%)",
              WebkitMaskImage:
                "radial-gradient(circle, black 60%, transparent 100%)",
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
              <ShieldCheck className='w-4 h-4' /> Customer Portal
            </div>
            <h2 className='text-3xl lg:text-4xl font-black text-[#071a2a] mb-3'>
              Welcome <span className='text-yellow-500'>Back</span>
            </h2>
            <p className='text-gray-500 font-medium'>
              Securely sign in to your courier dashboard.
            </p>
          </div>

          <form onSubmit={handleLogin} className='space-y-6'>
            <div>
              <label className='block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest'>
                Email Address
              </label>
              <input
                type='email'
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className='w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]'
                placeholder='customer@gmail.com'
              />
            </div>
            <div>
              <label className='block text-xs font-black text-gray-400 mb-2 uppercase tracking-widest'>
                Password
              </label>
              <div className='relative'>
                <input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className='w-full px-6 py-4 pr-12 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-bold text-[#071a2a]'
                  placeholder='••••••••'
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

            <button
              type='submit'
              disabled={loading}
              className='w-full bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-5 rounded-2xl shadow-xl hover:-translate-y-1 transition-all flex items-center justify-center gap-3 text-lg disabled:opacity-50 mt-8 overflow-hidden relative group'
            >
              <div className='absolute inset-0 bg-white/20 transform -skew-x-12 -translate-x-full group-hover:translate-x-full transition-transform duration-700'></div>
              {loading ? "Authenticating..." : "Secure Sign In"}
              {!loading && (
                <ArrowRight className='w-6 h-6 transform group-hover:translate-x-1.5 transition-transform' />
              )}
            </button>

            <div className='text-center mt-4'>
              <Link
                to='/forgot-password'
                className='text-sm text-gray-400 hover:text-yellow-500 font-semibold transition-colors'
              >
                Forgot your password?
              </Link>
            </div>

            <p className='text-center text-gray-500 mt-6 text-sm font-medium'>
              Don't have an account?{" "}
              <Link
                to='/signup'
                className='text-[#071a2a] font-black hover:text-yellow-500 transition-colors'
              >
                Sign up
              </Link>
            </p>
          </form>
        </div>
      </div>

      {/* OTP Verification Modal */}
      {showOtpModal && (
        <div className='fixed inset-0 bg-[#071a2a]/80 backdrop-blur-sm z-50 flex items-center justify-center p-4'>
          <div className='bg-white rounded-3xl p-8 max-w-sm w-full shadow-2xl relative'>
            {/* Close button */}
            <button
              onClick={() => setShowOtpModal(false)}
              className='absolute top-4 right-4 text-gray-400 hover:text-[#071a2a] transition-colors'
            >
              ✕
            </button>
            <div className='w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-6 text-yellow-600'>
              <KeyRound className='w-8 h-8' />
            </div>
            <h3 className='text-2xl font-black text-center text-[#071a2a] mb-2'>
              Verify Your Email
            </h3>
            <p className='text-center text-gray-500 mb-8 font-medium'>
              Please enter the 6-digit OTP sent to <br />
              <span className='text-[#071a2a] font-black'>{email}</span>
            </p>

            <form onSubmit={handleVerifyOtp} className='space-y-6'>
              <div>
                <input
                  type='text'
                  maxLength={6}
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
                  className='w-full px-6 py-4 bg-gray-50 rounded-2xl border-2 border-transparent focus:border-yellow-500 focus:bg-white outline-none transition-all font-black text-center text-2xl tracking-[0.5em] text-[#071a2a]'
                  placeholder='------'
                  required
                />
              </div>
              <button
                type='submit'
                disabled={verifying || otp.length < 6}
                className='w-full bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-4 rounded-2xl shadow-xl transition-all disabled:opacity-50'
              >
                {verifying ? "Verifying..." : "Verify Email"}
              </button>
            </form>

            <div className='mt-6 text-center'>
              <p className='text-sm text-gray-500 font-medium'>
                Didn't receive the code?{" "}
                <button
                  type='button'
                  onClick={handleResendOtp}
                  className='text-[#071a2a] font-black hover:text-yellow-500 transition-colors underline'
                >
                  Resend OTP
                </button>
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Login;
