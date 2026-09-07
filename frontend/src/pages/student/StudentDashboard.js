import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

/**
 * StudentDashboard - main landing page for students after login.
 * Shows portfolio summary counts and quick-action cards.
 */
function StudentDashboard() {
  const [stats, setStats] = useState({
    projects: 0, certifications: 0, internships: 0, achievements: 0, skills: 0
  });
  const [loading, setLoading] = useState(true);

  const userId = localStorage.getItem('userId');
  const name   = localStorage.getItem('name') || 'Student';

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [projects, certs, internships, achievements, skills] = await Promise.all([
          api.get(`/api/student/${userId}/projects`),
          api.get(`/api/student/${userId}/certifications`),
          api.get(`/api/student/${userId}/internships`),
          api.get(`/api/student/${userId}/achievements`),
          api.get(`/api/student/${userId}/skills`),
        ]);
        setStats({
          projects:       projects.data.length,
          certifications: certs.data.length,
          internships:    internships.data.length,
          achievements:   achievements.data.length,
          skills:         skills.data.length
        });
      } catch (err) {
        console.error('Error loading stats:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, [userId]);

  const cards = [
    { label: 'Projects',       count: stats.projects,       icon: '💻', bg: 'bg-blue-50',   icon_bg: 'bg-blue-100',   text: 'text-blue-700',   link: '/student/projects' },
    { label: 'Skills',         count: stats.skills,         icon: '⚡', bg: 'bg-amber-50',  icon_bg: 'bg-amber-100',  text: 'text-amber-700',  link: '/student/skills' },
    { label: 'Certifications', count: stats.certifications, icon: '🏆', bg: 'bg-emerald-50',icon_bg: 'bg-emerald-100',text: 'text-emerald-700',link: '/student/certifications' },
    { label: 'Internships',    count: stats.internships,    icon: '🏢', bg: 'bg-purple-50', icon_bg: 'bg-purple-100', text: 'text-purple-700', link: '/student/internships' },
    { label: 'Achievements',   count: stats.achievements,   icon: '🌟', bg: 'bg-rose-50',   icon_bg: 'bg-rose-100',   text: 'text-rose-700',   link: '/student/achievements' },
  ];

  return (
    <StudentLayout title="Dashboard">
      {/* Welcome banner */}
      <div className="bg-blue-600 rounded-2xl p-6 mb-6 text-white">
        <h3 className="text-xl font-black">
          Hey, {name.split(' ')[0]}! 👋
        </h3>
        <p className="text-blue-200 text-sm mt-1">
          Here's a snapshot of your academic portfolio.
        </p>
        <div className="flex gap-3 mt-4">
          <Link to="/student/profile" className="bg-white text-blue-600 text-xs font-bold px-3 py-1.5 rounded-lg hover:bg-blue-50 transition-colors">
            Update Profile →
          </Link>
          <Link to="/student/classrooms" className="bg-blue-500 text-white text-xs font-bold px-3 py-1.5 rounded-lg hover:bg-blue-400 transition-colors">
            My Classrooms
          </Link>
        </div>
      </div>

      {/* Stats grid */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="w-11 h-11 bg-slate-100 rounded-xl mb-3"></div>
              <div className="h-8 w-12 bg-slate-100 rounded mb-2"></div>
              <div className="h-3 w-20 bg-slate-100 rounded"></div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
          {cards.map((card) => (
            <Link key={card.label} to={card.link}
              className={`card block no-underline ${card.bg} border-transparent hover:shadow-md transition-all duration-200`}
            >
              <div className={`stat-icon-wrap ${card.icon_bg} mb-3`}>
                <span>{card.icon}</span>
              </div>
              <p className={`stat-value ${card.text}`}>{card.count}</p>
              <p className="stat-label mt-1">{card.label}</p>
            </Link>
          ))}
        </div>
      )}

      {/* Quick add section */}
      <div className="card">
        <h4 className="text-sm font-bold text-slate-700 mb-4">Quick Add</h4>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          {[
            { icon: '💻', label: 'Project',       link: '/student/projects' },
            { icon: '⚡', label: 'Skill',         link: '/student/skills' },
            { icon: '🏆', label: 'Certification', link: '/student/certifications' },
            { icon: '🏢', label: 'Internship',    link: '/student/internships' },
            { icon: '🌟', label: 'Achievement',   link: '/student/achievements' },
          ].map(item => (
            <Link key={item.label} to={item.link}
              className="flex flex-col items-center gap-1.5 py-4 rounded-xl border-2 border-dashed border-slate-200
                         hover:border-blue-400 hover:bg-blue-50 transition-all duration-150 text-center"
            >
              <span className="text-2xl">{item.icon}</span>
              <span className="text-xs font-semibold text-slate-600">{item.label}</span>
            </Link>
          ))}
        </div>
      </div>

      {/* Tip box */}
      <div className="alert-info mt-4">
        <span className="text-lg flex-shrink-0">💡</span>
        <div>
          <p className="font-semibold">Tips for a great portfolio</p>
          <ul className="mt-1 space-y-0.5 text-xs">
            <li>• All items start as <strong>PENDING</strong> until your teacher verifies them.</li>
            <li>• Join your teacher's classroom using their unique join code.</li>
            <li>• Your teacher is notified each time you add or update an item.</li>
          </ul>
        </div>
      </div>
    </StudentLayout>
  );
}

export default StudentDashboard;
