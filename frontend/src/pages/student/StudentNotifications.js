import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

/**
 * StudentNotifications - shows all notifications sent to the student
 * (direct messages from teachers).
 */
// Color coding per notification type, so a student can tell at a glance
// whether something is just informational or needs their attention.
const TYPE_STYLES = {
  REMINDER: 'bg-amber-100 text-amber-700',
  ALERT:    'bg-red-100 text-red-600',
  FEEDBACK: 'bg-purple-100 text-purple-600',
  GENERAL:  'bg-gray-100 text-gray-600',
};

function StudentNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading]             = useState(true);
  const [error, setError]                 = useState('');
  const [completingId, setCompletingId]   = useState(null);

  const studentId = localStorage.getItem('userId');

  const loadNotifications = () => {
    setLoading(true);
    api.get(`/api/student/${studentId}/notifications`)
      .then(res => setNotifications(res.data))
      .catch(() => setError('Failed to load notifications'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadNotifications();
    // Mark all as read when the student opens this page
    api.put(`/api/student/${studentId}/notifications/read`).catch(() => {});
  }, [studentId]); // eslint-disable-line

  const markOneRead = async (notifId) => {
    await api.put(`/api/student/${studentId}/notifications/${notifId}/read`).catch(() => {});
    setNotifications(prev => prev.map(n => n.id === notifId ? { ...n, read: true } : n));
  };

  const markAllRead = async () => {
    await api.put(`/api/student/${studentId}/notifications/read`).catch(() => {});
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const markDone = async (notifId) => {
    setCompletingId(notifId);
    try {
      await api.put(`/api/student/${studentId}/notifications/${notifId}/complete`);
      setNotifications(prev => prev.filter(n => n.id !== notifId));
    } catch {
      setError('Failed to mark notification as done.');
    } finally {
      setCompletingId(null);
    }
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <StudentLayout title="Notifications">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}

      <div className="flex justify-between items-center mb-4">
        <p className="text-sm text-gray-500">
          {notifications.length} notification(s){unreadCount > 0 && ` — ${unreadCount} unread`}
        </p>
        {unreadCount > 0 && (
          <button onClick={markAllRead} className="btn-secondary text-xs">Mark all read</button>
        )}
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading notifications...</div>
      ) : notifications.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">🔔</p>
          <p>No notifications yet.</p>
          <p className="text-xs mt-2">Notifications from your teachers will appear here.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map(notif => (
            <div
              key={notif.id}
              className={`card flex items-start gap-4 ${notif.read ? '' : 'border-l-4 border-blue-400'}`}
            >
              <div className={`w-2.5 h-2.5 rounded-full mt-1.5 flex-shrink-0 ${notif.read ? 'bg-gray-200' : 'bg-blue-400'}`}></div>
              <div className="flex-1">
                <p className="text-sm text-gray-800">{notif.message}</p>
                <div className="flex items-center gap-3 mt-1 flex-wrap">
                  <span className="text-xs text-gray-400">{new Date(notif.createdAt).toLocaleString()}</span>
                  {notif.itemType && (
                    <span className={`text-xs px-2 py-0.5 rounded font-medium ${TYPE_STYLES[notif.itemType] || TYPE_STYLES.GENERAL}`}>
                      {notif.itemType}
                    </span>
                  )}
                  {notif.teacher && (
                    <span className="text-xs text-blue-500">From: {notif.teacher.name}</span>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-2 flex-shrink-0">
                {!notif.read && (
                  <button
                    onClick={() => markOneRead(notif.id)}
                    className="text-xs text-gray-400 hover:text-blue-600"
                    title="Mark as read"
                  >
                    ✓
                  </button>
                )}
                <button
                  onClick={() => markDone(notif.id)}
                  disabled={completingId === notif.id}
                  className="btn-primary text-xs"
                  title="Mark this task as done — your teacher will be notified"
                >
                  {completingId === notif.id ? 'Marking...' : 'Done'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </StudentLayout>
  );
}

export default StudentNotifications;
