import React, { useState, useEffect } from 'react';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * TeacherNotifications - shows all notifications for the teacher.
 * Supports marking individual notifications as read and sending direct notifications.
 */
function TeacherNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading]             = useState(true);
  const [showSendForm, setShowSendForm]   = useState(false);
  const [students, setStudents]           = useState([]);
  const [sendForm, setSendForm]           = useState({ studentId: '', message: '', itemType: 'GENERAL' });
  const [sending, setSending]             = useState(false);
  const [error, setError]                 = useState('');
  const [success, setSuccess]             = useState('');

  const teacherId = localStorage.getItem('userId');

  const loadNotifications = () => {
    setLoading(true);
    api.get(`/api/teacher/${teacherId}/notifications`)
      .then(res => setNotifications(res.data))
      .catch(() => setError('Failed to load notifications'))
      .finally(() => setLoading(false));
  };

  // Load students for the send-notification dropdown
  const loadStudents = () => {
    api.get(`/api/teacher/${teacherId}/students/search`)
      .then(res => setStudents(res.data))
      .catch(() => {});
  };

  useEffect(() => {
    loadNotifications();
    loadStudents();
    // Mark all as read when user opens this page
    api.put(`/api/teacher/${teacherId}/notifications/read`).catch(() => {});
  }, [teacherId]); // eslint-disable-line

  const markOneRead = async (notifId) => {
    await api.put(`/api/teacher/${teacherId}/notifications/${notifId}/read`).catch(() => {});
    setNotifications(prev => prev.map(n => n.id === notifId ? { ...n, read: true } : n));
  };

  const markAllRead = async () => {
    await api.put(`/api/teacher/${teacherId}/notifications/read`).catch(() => {});
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!sendForm.studentId || !sendForm.message.trim()) {
      setError('Please select a student and enter a message.');
      return;
    }
    setSending(true);
    setError('');
    try {
      await api.post(`/api/teacher/${teacherId}/notifications/send`, {
        studentId: sendForm.studentId,
        message:   sendForm.message,
        itemType:  sendForm.itemType,
        action:    'INFO',
      });
      setSuccess('Notification sent!');
      setSendForm({ studentId: '', message: '', itemType: 'GENERAL' });
      setShowSendForm(false);
      loadNotifications();
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to send notification.');
    } finally {
      setSending(false);
    }
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <TeacherLayout title="Notifications">
      {error   && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}
      {success && <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4">{success}</div>}

      {/* Header controls */}
      <div className="flex justify-between items-center mb-4">
        <p className="text-sm text-gray-500">
          {notifications.length} notification(s){unreadCount > 0 && ` — ${unreadCount} unread`}
        </p>
        <div className="flex gap-2">
          {unreadCount > 0 && (
            <button onClick={markAllRead} className="btn-secondary text-xs">Mark all read</button>
          )}
          <button onClick={() => setShowSendForm(!showSendForm)} className="btn-primary text-xs">
            {showSendForm ? '✕ Cancel' : '+ Send Notification'}
          </button>
        </div>
      </div>

      {/* Send notification form */}
      {showSendForm && (
        <div className="card mb-4 border border-indigo-100">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">📤 Send Notification to Student</h3>
          <form onSubmit={handleSend} className="space-y-3">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Select Student *</label>
              <select
                value={sendForm.studentId}
                onChange={e => setSendForm({ ...sendForm, studentId: e.target.value })}
                className="input-field text-sm"
                required
              >
                <option value="">Choose a student...</option>
                {students.map(s => (
                  <option key={s.user?.id} value={s.user?.id}>
                    {s.user?.name} {s.registerNumber ? `(${s.registerNumber})` : `— ${s.user?.email}`}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Type</label>
              <select
                value={sendForm.itemType}
                onChange={e => setSendForm({ ...sendForm, itemType: e.target.value })}
                className="input-field text-sm"
              >
                <option value="GENERAL">General</option>
                <option value="REMINDER">Reminder</option>
                <option value="ALERT">Alert</option>
                <option value="FEEDBACK">Feedback</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Message *</label>
              <textarea
                value={sendForm.message}
                onChange={e => setSendForm({ ...sendForm, message: e.target.value })}
                className="input-field text-sm"
                rows="3"
                placeholder="Enter your message..."
                required
              />
            </div>
            <div className="flex gap-2">
              <button type="submit" disabled={sending} className="btn-primary text-sm">
                {sending ? 'Sending...' : 'Send'}
              </button>
              <button type="button" onClick={() => setShowSendForm(false)} className="btn-secondary text-sm">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Notifications list */}
      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading notifications...</div>
      ) : notifications.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">🔔</p>
          <p>No notifications yet.</p>
          <p className="text-xs mt-2">Notifications appear here when students update their portfolio items.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map(notif => (
            <div
              key={notif.id}
              className={`card flex items-start gap-4 ${notif.read ? '' : 'border-l-4 border-blue-400'}`}
            >
              {/* Unread indicator */}
              <div className={`w-2.5 h-2.5 rounded-full mt-1.5 flex-shrink-0 ${notif.read ? 'bg-gray-200' : 'bg-blue-400'}`}></div>
              <div className="flex-1">
                <p className="text-sm text-gray-800">{notif.message}</p>
                <div className="flex items-center gap-3 mt-1 flex-wrap">
                  <span className="text-xs text-gray-400">{new Date(notif.createdAt).toLocaleString()}</span>
                  {notif.itemType && (
                    <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded">{notif.itemType}</span>
                  )}
                  {notif.action && (
                    <span className={`text-xs px-2 py-0.5 rounded ${
                      notif.action === 'ADDED'   ? 'bg-green-100 text-green-600' :
                      notif.action === 'UPDATED' ? 'bg-blue-100 text-blue-600'  :
                      notif.action === 'DELETED' ? 'bg-red-100 text-red-600'    :
                      'bg-gray-100 text-gray-600'
                    }`}>{notif.action}</span>
                  )}
                  {notif.student && (
                    <span className="text-xs text-indigo-500">From: {notif.student.name}</span>
                  )}
                </div>
              </div>
              {!notif.read && (
                <button
                  onClick={() => markOneRead(notif.id)}
                  className="text-xs text-gray-400 hover:text-blue-600 flex-shrink-0"
                  title="Mark as read"
                >
                  ✓
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </TeacherLayout>
  );
}

export default TeacherNotifications;
