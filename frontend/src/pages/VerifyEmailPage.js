import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../services/api';

/**
 * VerifyEmailPage - lands here from the link in the verification email
 * (e.g. /verify-email?token=...). Confirms the token with the backend and
 * reports success or failure.
 */
function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus]   = useState('checking'); // checking | success | error
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('This verification link is missing its token.');
      return;
    }

    api.post('/api/auth/verify-email', { token })
      .then((response) => {
        setStatus('success');
        setMessage(response.data.message);
      })
      .catch((err) => {
        setStatus('error');
        setMessage(err.response?.data?.error || 'This verification link is invalid or has expired.');
      });
  }, [token]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-8">
      <div className="w-full max-w-md text-center">
        <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mx-auto mb-6">
          <span className="text-white font-black text-lg">M</span>
        </div>

        {status === 'checking' && (
          <>
            <div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-slate-500">Verifying your email...</p>
          </>
        )}

        {status === 'success' && (
          <>
            <h1 className="text-2xl font-black text-slate-800 mb-2">Email verified ✅</h1>
            <p className="text-slate-500 mb-6">{message}</p>
            <Link to="/login" className="btn-primary inline-block py-3 px-6">
              Sign in →
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <h1 className="text-2xl font-black text-slate-800 mb-2">Verification failed</h1>
            <p className="text-slate-500 mb-6">{message}</p>
            <Link to="/login" className="text-blue-600 hover:text-blue-700 font-semibold">
              Back to sign in
            </Link>
          </>
        )}
      </div>
    </div>
  );
}

export default VerifyEmailPage;
