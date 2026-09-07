import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// Auth Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Student Pages
import StudentDashboard from './pages/student/StudentDashboard';
import StudentProfile from './pages/student/StudentProfile';
import StudentProjects from './pages/student/StudentProjects';
import StudentSkills from './pages/student/StudentSkills';
import StudentCertifications from './pages/student/StudentCertifications';
import StudentInternships from './pages/student/StudentInternships';
import StudentAchievements from './pages/student/StudentAchievements';
import StudentClassrooms from './pages/student/StudentClassrooms';
import StudentMarks from './pages/student/StudentMarks';

// Teacher Pages
import TeacherDashboard from './pages/teacher/TeacherDashboard';
import TeacherClassrooms from './pages/teacher/TeacherClassrooms';
import TeacherStudentView from './pages/teacher/TeacherStudentView';
import TeacherSearch from './pages/teacher/TeacherSearch';
import TeacherReports from './pages/teacher/TeacherReports';
import TeacherNotifications from './pages/teacher/TeacherNotifications';

// Protected Route component
import ProtectedRoute from './components/ProtectedRoute';

/**
 * App.js - The root component that sets up all application routes.
 *
 * WHY REACT ROUTER?
 * Single Page Applications (SPAs) don't reload the page on navigation.
 * React Router handles URL changes without page reloads.
 * Each <Route> maps a URL path to a React component.
 *
 * ROUTE STRUCTURE:
 * / → redirects to login
 * /login → Login page
 * /register → Register page
 * /student/* → student pages (protected, requires ROLE_STUDENT)
 * /teacher/* → teacher pages (protected, requires ROLE_TEACHER)
 */
function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes - no login needed */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Student Routes - protected, only ROLE_STUDENT */}
        <Route path="/student" element={<ProtectedRoute role="ROLE_STUDENT" />}>
          <Route path="dashboard" element={<StudentDashboard />} />
          <Route path="profile" element={<StudentProfile />} />
          <Route path="projects" element={<StudentProjects />} />
          <Route path="skills" element={<StudentSkills />} />
          <Route path="certifications" element={<StudentCertifications />} />
          <Route path="internships" element={<StudentInternships />} />
          <Route path="achievements" element={<StudentAchievements />} />
          <Route path="classrooms" element={<StudentClassrooms />} />
          <Route path="marks" element={<StudentMarks />} />
        </Route>

        {/* Teacher Routes - protected, only ROLE_TEACHER */}
        <Route path="/teacher" element={<ProtectedRoute role="ROLE_TEACHER" />}>
          <Route path="dashboard" element={<TeacherDashboard />} />
          <Route path="classrooms" element={<TeacherClassrooms />} />
          <Route path="students/:studentId" element={<TeacherStudentView />} />
          <Route path="search" element={<TeacherSearch />} />
          <Route path="reports" element={<TeacherReports />} />
          <Route path="notifications" element={<TeacherNotifications />} />
        </Route>

        {/* Catch-all redirect to login */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
