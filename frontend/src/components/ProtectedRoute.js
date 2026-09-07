import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

/**
 * ProtectedRoute - guards routes that require authentication and a specific role.
 *
 * WHY THIS EXISTS:
 * Without protection, anyone could navigate to /teacher/dashboard directly.
 * This component checks:
 * 1. Is the user logged in? (is there a token in localStorage?)
 * 2. Does the user have the correct role? (ROLE_TEACHER vs ROLE_STUDENT)
 * If not, redirect them to login.
 *
 * HOW IT WORKS IN REACT ROUTER:
 * <Route path="/student" element={<ProtectedRoute role="ROLE_STUDENT" />}>
 *   <Route path="dashboard" element={<StudentDashboard />} />
 * </Route>
 *
 * <Outlet /> renders the matched child route.
 * If auth fails, we navigate to /login instead.
 *
 * @prop role - the required role ("ROLE_TEACHER" or "ROLE_STUDENT")
 */
function ProtectedRoute({ role }) {
  // Get auth data from localStorage (set during login)
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('role');

  // If not logged in, go to login
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // If logged in but wrong role, go to login
  if (role && userRole !== role) {
    return <Navigate to="/login" replace />;
  }

  // If all checks pass, render the actual route component
  return <Outlet />;
}

export default ProtectedRoute;
