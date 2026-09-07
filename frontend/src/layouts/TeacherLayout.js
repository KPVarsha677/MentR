import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

/**
 * TeacherLayout - sidebar + topbar shell for all teacher pages.
 */
function TeacherLayout({ children, title }) {
  const navigate  = useNavigate();
  const location  = useLocation();
  const name      = localStorage.getItem('name') || 'Teacher';

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const navLinks = [
    { path: '/teacher/dashboard',     label: 'Dashboard',       icon: '🏠' },
    { path: '/teacher/classrooms',    label: 'Classrooms',      icon: '📚' },
    { path: '/teacher/search',        label: 'Search Students', icon: '🔍' },
    { path: '/teacher/reports',       label: 'Reports',         icon: '📊' },
    { path: '/teacher/notifications', label: 'Notifications',   icon: '🔔' },
  ];

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      {/* ---- Sidebar ---- */}
      <aside className="w-60 bg-white border-r border-slate-100 flex flex-col flex-shrink-0">
        {/* Logo */}
        <div className="px-5 py-5 border-b border-slate-100 flex items-center gap-2.5">
          <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center flex-shrink-0">
            <span className="text-white font-black text-sm">M</span>
          </div>
          <div>
            <p className="text-indigo-600 font-black text-base leading-none tracking-tight">MentR</p>
            <p className="text-slate-400 text-[10px] font-medium mt-0.5">Teacher Portal</p>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
          {navLinks.map((link) => {
            const active = location.pathname === link.path;
            return (
              <Link
                key={link.path}
                to={link.path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150
                  ${active
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'}`}
              >
                <span className="text-base leading-none">{link.icon}</span>
                <span>{link.label}</span>
                {active && <span className="ml-auto w-1.5 h-1.5 bg-white rounded-full opacity-70"></span>}
              </Link>
            );
          })}
        </nav>

        {/* User footer */}
        <div className="px-3 py-4 border-t border-slate-100">
          <div className="flex items-center gap-2.5 px-3 py-2 mb-1">
            <div className="w-8 h-8 rounded-xl bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-sm flex-shrink-0">
              {name.charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-slate-800 truncate">{name}</p>
              <p className="text-xs text-slate-400">Teacher</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm font-medium text-slate-500
                       hover:text-red-600 hover:bg-red-50 rounded-xl transition-all duration-150"
          >
            <span>🚪</span>
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* ---- Main area ---- */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar */}
        <header className="bg-white border-b border-slate-100 px-6 py-4 flex-shrink-0">
          <h2 className="text-lg font-bold text-slate-800">{title}</h2>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}

export default TeacherLayout;
