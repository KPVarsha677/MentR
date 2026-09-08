import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

/**
 * RegisterPage - Registration for new teachers and students.
 * The account is created unverified — registering no longer logs the user
 * in. Instead they're shown a "check your email" screen and must click the
 * verification link before they can sign in.
 */
function RegisterPage() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'ROLE_STUDENT',
    teacherInviteCode: ''
  });
  const [error, setError]               = useState('');
  const [loading, setLoading]           = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [registered, setRegistered]     = useState(false);
  const [resendStatus, setResendStatus] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await api.post('/api/auth/register', formData);
      setRegistered(true);
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResendStatus('sending');
    try {
      const response = await api.post('/api/auth/resend-verification', { email: formData.email });
      setResendStatus(response.data.message);
    } catch (err) {
      setResendStatus(err.response?.data?.error || 'Could not resend. Please try again.');
    }
  };

  const isTeacher = formData.role === 'ROLE_TEACHER';

  return (
    <div className="min-h-screen flex">
      {/* Left panel — branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-blue-600 flex-col justify-between p-12">
        <div>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center">
              <span className="text-blue-600 font-black text-lg">M</span>
            </div>
            <span className="text-white font-black text-2xl tracking-tight">MentR</span>
          </div>

          <div className="mt-16">
            <h2 className="text-4xl font-black text-white leading-tight">
              Join MentR<br />today.
            </h2>
            <p className="text-blue-200 mt-4 text-lg leading-relaxed max-w-sm">
              Connect with your mentor, showcase your work, and get your achievements officially verified.
            </p>
          </div>

          {/* Role info cards */}
          <div className="mt-10 space-y-4">
            <div className={`rounded-2xl p-4 border-2 transition-all ${!isTeacher ? 'border-white bg-white/20' : 'border-blue-500 bg-blue-500/30'}`}>
              <p className="text-white font-bold text-sm">👤 Students</p>
              <p className="text-blue-100 text-xs mt-1">Build your portfolio, track certifications and internships. Get verified by your mentor.</p>
            </div>
            <div className={`rounded-2xl p-4 border-2 transition-all ${isTeacher ? 'border-white bg-white/20' : 'border-blue-500 bg-blue-500/30'}`}>
              <p className="text-white font-bold text-sm">🏫 Teachers</p>
              <p className="text-blue-100 text-xs mt-1">Create classrooms, monitor student progress, verify achievements and give feedback.</p>
            </div>
          </div>
        </div>

        <p className="text-blue-300 text-xs">© 2024 MentR. Smart Mentor-Mentee Platform.</p>
      </div>

      {/* Right panel — register form */}
      <div className="flex-1 flex items-center justify-center p-8 bg-slate-50">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 mb-10 lg:hidden">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-black text-sm">M</span>
            </div>
            <span className="text-blue-600 font-black text-xl">MentR</span>
          </div>

          {registered ? (
            <>
              <h1 className="text-2xl font-black text-slate-800 mb-1">Check your email 📬</h1>
              <p className="text-slate-500 text-sm mb-6">
                We sent a verification link to <span className="font-semibold">{formData.email}</span>.
                Click it to activate your account, then sign in.
              </p>

              {resendStatus && (
                <div className="alert-error mb-4" style={{ background: '#eff6ff', color: '#1d4ed8', borderColor: '#bfdbfe' }}>
                  <span>{resendStatus}</span>
                </div>
              )}

              <button
                type="button"
                onClick={handleResend}
                disabled={resendStatus === 'sending'}
                className="btn-primary w-full py-3 text-base mb-4"
              >
                Resend verification email
              </button>

              <p className="text-center text-sm text-slate-500">
                <Link to="/login" className="text-blue-600 hover:text-blue-700 font-semibold">
                  Back to sign in
                </Link>
              </p>
            </>
          ) : (
          <>
          <h1 className="text-2xl font-black text-slate-800 mb-1">Create your account</h1>
          <p className="text-slate-500 text-sm mb-8">Start managing your academic journey with MentR.</p>

          {error && (
            <div className="alert-error mb-5">
              <span>⚠</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-4">
            {/* Role toggle — shown first so the page adapts */}
            <div>
              <label className="form-label">I am a</label>
              <div className="grid grid-cols-2 gap-2">
                {[['ROLE_STUDENT', '👤 Student'], ['ROLE_TEACHER', '🏫 Teacher']].map(([val, label]) => (
                  <button
                    key={val}
                    type="button"
                    onClick={() => setFormData({ ...formData, role: val })}
                    className={`py-2.5 rounded-xl text-sm font-semibold border-2 transition-all duration-150
                      ${formData.role === val
                        ? 'border-blue-600 bg-blue-600 text-white shadow-sm'
                        : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300'}`}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="form-label">Full Name</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="input-field"
                placeholder="Your full name"
                required
              />
            </div>

            <div>
              <label className="form-label">Email Address</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="input-field"
                placeholder="you@example.com"
                required
              />
            </div>

            <div>
              <label className="form-label">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="input-field pr-10"
                  placeholder="At least 6 characters"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  tabIndex={-1}
                >
                  {showPassword ? '🙈' : '👁'}
                </button>
              </div>
            </div>

            {isTeacher && (
              <div>
                <label className="form-label">Teacher Invite Code</label>
                <input
                  type="text"
                  name="teacherInviteCode"
                  value={formData.teacherInviteCode}
                  onChange={handleChange}
                  className="input-field"
                  placeholder="Provided by your administrator"
                  required
                />
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full py-3 text-base mt-2"
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                  Creating account...
                </span>
              ) : `Create ${isTeacher ? 'Teacher' : 'Student'} Account →`}
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 hover:text-blue-700 font-semibold">
              Sign in
            </Link>
          </p>
          </>
          )}
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
