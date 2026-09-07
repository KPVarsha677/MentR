import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * TeacherDashboard - main landing page for teachers after login.
 * Shows summary stats and recent student activity notifications.
 */
function TeacherDashboard() {
  const [stats, setStats]   = useState({});
  const [loading, setLoading] = useState(true);

  const teacherId = localStorage.getItem('userId');
  const name      = localStorage.getItem('name') || 'Teacher';

  useEffect(() => {
    const fetchData = async () => {
      try {
        const statsRes = await api.get(`/api/teacher/${teacherId}/dashboard`);
        setStats(statsRes.data);
      } catch (err) {
        console.error('Error loading dashboard:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [teacherId]);

  const statCards = [
    { label: 'Total Students',      value: stats.totalStudents,         icon: '👥', bg: 'bg-blue-50',   icon_bg: 'bg-blue-100',   text: 'text-blue-700',   path: '/teacher/search' },
    { label: 'Total Classrooms',    value: stats.totalClassrooms,       icon: '📚', bg: 'bg-indigo-50', icon_bg: 'bg-indigo-100', text: 'text-indigo-700', path: '/teacher/classrooms' },
    { label: 'Pending Reviews',     value: stats.totalPending,          icon: '⏳', bg: 'bg-amber-50',  icon_bg: 'bg-amber-100',  text: 'text-amber-700',  path: '/teacher/reports', state: { preset: 'pending' } },
    { label: 'Pending Projects',    value: stats.pendingProjects,       icon: '💻', bg: 'bg-purple-50', icon_bg: 'bg-purple-100', text: 'text-purple-700', path: '/teacher/reports', state: { preset: 'pending' } },
    { label: 'Pending Certs',       value: stats.pendingCertifications, icon: '🏆', bg: 'bg-emerald-50',icon_bg: 'bg-emerald-100',text: 'text-emerald-700', path: '/teacher/reports', state: { preset: 'pending' } },
    { label: 'Pending Internships', value: stats.pendingInternships,    icon: '🏢', bg: 'bg-rose-50',   icon_bg: 'bg-rose-100',   text: 'text-rose-700',   path: '/teacher/reports', state: { preset: 'pending' } },
    { label: 'Pending Achievements',value: stats.pendingAchievements,   icon: '🌟', bg: 'bg-orange-50', icon_bg: 'bg-orange-100', text: 'text-orange-700', path: '/teacher/reports', state: { preset: 'pending' } },
  ];

  const actionLinks = [
    { icon: '📚', label: 'Manage Classrooms', desc: 'Create & share join codes', path: '/teacher/classrooms', color: 'bg-indigo-600' },
    { icon: '🔍', label: 'Search Students',   desc: 'Find by name, dept, year',  path: '/teacher/search',    color: 'bg-blue-600' },
    { icon: '📊', label: 'Generate Reports',  desc: 'Portfolio & summary PDFs',  path: '/teacher/reports',   color: 'bg-purple-600' },
  ];

  return (
    <TeacherLayout title="Dashboard">
      {/* Welcome banner */}
      <div className="bg-indigo-600 rounded-2xl p-6 mb-6 text-white">
        <h3 className="text-xl font-black">Welcome, {name.split(' ')[0]}! 👋</h3>
        <p className="text-indigo-200 text-sm mt-1">
          Here's what's happening across your classrooms.
        </p>
        <div className="flex gap-3 mt-4">
          <Link to="/teacher/classrooms" className="bg-white text-indigo-600 text-xs font-bold px-3 py-1.5 rounded-lg hover:bg-indigo-50 transition-colors">
            My Classrooms →
          </Link>
          <Link to="/teacher/search" className="bg-indigo-500 text-white text-xs font-bold px-3 py-1.5 rounded-lg hover:bg-indigo-400 transition-colors">
            Search Students
          </Link>
        </div>
      </div>

      {/* Stats grid */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          {[...Array(7)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="w-11 h-11 bg-slate-100 rounded-xl mb-3"></div>
              <div className="h-8 w-12 bg-slate-100 rounded mb-2"></div>
              <div className="h-3 w-24 bg-slate-100 rounded"></div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          {statCards.map((card) => (
            <Link
              key={card.label}
              to={card.path}
              state={card.state}
              className={`card ${card.bg} border-transparent no-underline hover:shadow-md transition-all duration-200 block`}
            >
              <div className={`stat-icon-wrap ${card.icon_bg} mb-3`}>
                <span>{card.icon}</span>
              </div>
              <p className={`stat-value ${card.text}`}>{card.value ?? 0}</p>
              <p className="stat-label mt-1">{card.label}</p>
            </Link>
          ))}
        </div>
      )}

      {/* Quick actions */}
      <div className="mt-2">
        <h4 className="text-sm font-bold text-slate-700 mb-3">Quick Actions</h4>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {actionLinks.map(item => (
            <Link key={item.path} to={item.path}
              className="card flex items-center gap-4 hover:shadow-md transition-all duration-200 no-underline"
            >
              <div className={`w-10 h-10 ${item.color} rounded-xl flex items-center justify-center text-xl flex-shrink-0`}>
                {item.icon}
              </div>
              <div>
                <p className="text-sm font-semibold text-slate-800">{item.label}</p>
                <p className="text-xs text-slate-400">{item.desc}</p>
              </div>
              <span className="ml-auto text-slate-300">›</span>
            </Link>
          ))}
        </div>
      </div>
    </TeacherLayout>
  );
}

export default TeacherDashboard;
