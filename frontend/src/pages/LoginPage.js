import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

/**
 * LoginPage - Entry point for all users (teachers and students).
 * After successful login, redirects to the correct dashboard based on role.
 */
function LoginPage() {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/api/auth/login', { email, password });
      const data = response.data;

      localStorage.setItem('token',  data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('name',   data.name);
      localStorage.setItem('email',  data.email);
      localStorage.setItem('role',   data.role);

      if (data.role === 'ROLE_TEACHER') {
        navigate('/teacher/dashboard');
      } else {
        navigate('/student/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left panel — branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-blue-600 flex-col justify-between p-12">
        <div>
          {/* Logo */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center">
              <span className="text-blue-600 font-black text-lg">M</span>
            </div>
            <span className="text-white font-black text-2xl tracking-tight">MentR</span>
          </div>

          {/* Hero text */}
          <div className="mt-16">
            <h2 className="text-4xl font-black text-white leading-tight">
              Your digital<br />mentor journal.
            </h2>
            <p className="text-blue-200 mt-4 text-lg leading-relaxed max-w-sm">
              Track projects, certifications, and achievements — all verified by your mentor in one place.
            </p>
          </div>

          {/* Feature pills */}
          <div className="mt-10 space-y-3">
            {[
              ['📋', 'Manage academic portfolios'],
              ['✅', 'Teacher-verified submissions'],
              ['🔔', 'Real-time progress updates'],
              ['📊', 'Instant reports & analytics'],
            ].map(([icon, text]) => (
              <div key={text} className="flex items-center gap-3">
                <span className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center text-sm">{icon}</span>
                <span className="text-blue-100 text-sm font-medium">{text}</span>
              </div>
            ))}
          </div>
        </div>

        <p className="text-blue-300 text-xs">© 2024 MentR. Smart Mentor-Mentee Platform.</p>
      </div>

      {/* Right panel — login form */}
      <div className="flex-1 flex items-center justify-center p-8 bg-slate-50">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 mb-10 lg:hidden">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-black text-sm">M</span>
            </div>
            <span className="text-blue-600 font-black text-xl">MentR</span>
          </div>

          <h1 className="text-2xl font-black text-slate-800 mb-1">Welcome back</h1>
          <p className="text-slate-500 text-sm mb-8">Sign in to your account to continue.</p>

          {/* Error */}
          {error && (
            <div className="alert-error mb-5">
              <span>⚠</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="form-label">Email Address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input-field"
                placeholder="you@example.com"
                required
              />
            </div>

            <div>
              <label className="form-label">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="input-field"
                placeholder="Enter your password"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full py-3 text-base mt-2"
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                  Signing in...
                </span>
              ) : 'Sign In →'}
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-6">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-600 hover:text-blue-700 font-semibold">
              Create one free
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
