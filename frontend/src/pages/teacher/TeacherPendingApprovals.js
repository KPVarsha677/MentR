import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * VerifyButton - lets the teacher approve or reject an item with a comment.
 * Same pattern as TeacherStudentView's VerifyButton.
 */
function VerifyButton({ endpoint, itemId, onVerified }) {
  const [showPanel, setShowPanel] = useState(false);
  const [status, setStatus]       = useState('APPROVED');
  const [comment, setComment]     = useState('');
  const teacherId = localStorage.getItem('userId');

  const handleVerify = async () => {
    await api.post(`${endpoint}/${itemId}?teacherId=${teacherId}`, { status, comment });
    setShowPanel(false);
    onVerified();
  };

  return (
    <div className="relative inline-block flex-shrink-0">
      <button onClick={() => setShowPanel(!showPanel)} className="text-xs bg-indigo-50 hover:bg-indigo-100 text-indigo-600 px-3 py-1.5 rounded-lg font-medium transition-colors">
        Review
      </button>
      {showPanel && (
        <div className="absolute right-0 z-10 bg-white border border-gray-200 rounded-lg shadow-lg p-4 mt-1 w-64">
          <p className="text-sm font-medium mb-2">Verification Decision</p>
          <select value={status} onChange={(e) => setStatus(e.target.value)} className="input-field text-sm mb-2">
            <option value="APPROVED">Approve</option>
            <option value="REJECTED">Reject</option>
          </select>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            className="input-field text-sm mb-2"
            rows="2"
            placeholder="Optional comment..."
          />
          <div className="flex gap-2">
            <button onClick={handleVerify} className="btn-primary text-xs py-1 px-3">Submit</button>
            <button onClick={() => setShowPanel(false)} className="btn-secondary text-xs py-1 px-3">Cancel</button>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * TeacherPendingApprovals - one place to review and approve/reject every
 * PENDING item (projects, certifications, internships, achievements)
 * submitted by students in this teacher's own classrooms.
 *
 * This is what the Dashboard's "Pending ..." stat cards link to.
 */
function TeacherPendingApprovals() {
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');
  const [pending, setPending] = useState({ projects: [], certifications: [], internships: [], achievements: [] });

  const teacherId = localStorage.getItem('userId');
  const location = useLocation();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      // Scope to this teacher's own classrooms first, exactly like the
      // dashboard stats do — /api/reports/pending-verifications itself
      // returns PENDING items for every student system-wide.
      const classroomsRes = await api.get(`/api/classrooms/teacher/${teacherId}`);
      const memberLists = await Promise.all(
        classroomsRes.data.map((c) => api.get(`/api/classrooms/${c.id}/members`))
      );
      const myStudentIds = new Set(
        memberLists.flatMap((res) => res.data.map((m) => m.student.id))
      );

      const pendingRes = await api.get('/api/reports/pending-verifications');
      const scoped = {};
      for (const key of ['projects', 'certifications', 'internships', 'achievements']) {
        scoped[key] = (pendingRes.data[key] || []).filter((item) => myStudentIds.has(item.student?.id));
      }
      setPending(scoped);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to load pending items');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  // Arriving from a specific Dashboard card (e.g. "Pending Internships" ->
  // /teacher/pending#internships) — jump straight to that section instead
  // of always showing the page from the top (Projects).
  useEffect(() => {
    if (loading || !location.hash) return;
    const target = document.getElementById(location.hash.slice(1));
    if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }, [loading, location.hash]);

  const sections = [
    { key: 'projects', title: 'Projects', endpoint: '/api/teacher/verify/project', nameOf: (i) => i.title, sub: (i) => i.techStack },
    { key: 'certifications', title: 'Certifications', endpoint: '/api/teacher/verify/certification', nameOf: (i) => i.name, sub: (i) => i.issuingOrganization },
    { key: 'internships', title: 'Internships', endpoint: '/api/teacher/verify/internship', nameOf: (i) => i.companyName, sub: (i) => i.role },
    { key: 'achievements', title: 'Achievements', endpoint: '/api/teacher/verify/achievement', nameOf: (i) => i.title, sub: (i) => i.issuingOrganization },
  ];

  const total = sections.reduce((acc, s) => acc + pending[s.key].length, 0);

  return (
    <TeacherLayout title="Pending Approvals">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}

      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading pending items...</div>
      ) : total === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">✅</p>
          <p>Nothing pending — you're all caught up!</p>
        </div>
      ) : (
        <div className="space-y-8">
          {sections.map(({ key, title, endpoint, nameOf, sub }) => (
            pending[key].length > 0 && (
              <div key={key} id={key} className="scroll-mt-6">
                <h3 className="text-sm font-bold text-slate-700 uppercase tracking-wide mb-3">
                  {title} <span className="text-xs bg-slate-200 text-slate-600 px-2 py-0.5 rounded-full font-semibold normal-case">{pending[key].length}</span>
                </h3>
                <div className="space-y-3">
                  {pending[key].map((item) => (
                    <div key={item.id} className="card flex items-center justify-between gap-4">
                      <div className="min-w-0">
                        <p className="text-xs font-bold text-indigo-600 uppercase tracking-wide mb-1">{item.student?.name}</p>
                        <p className="font-semibold text-gray-800">{nameOf(item)}</p>
                        {sub(item) && <p className="text-sm text-gray-500">{sub(item)}</p>}
                        <Link to={`/teacher/students/${item.student?.id}`} className="text-xs text-blue-600 hover:underline mt-1 inline-block">
                          View Profile →
                        </Link>
                      </div>
                      <VerifyButton endpoint={endpoint} itemId={item.id} onVerified={load} />
                    </div>
                  ))}
                </div>
              </div>
            )
          ))}
        </div>
      )}
    </TeacherLayout>
  );
}

export default TeacherPendingApprovals;
