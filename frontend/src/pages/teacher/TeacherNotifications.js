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
  const [classrooms, setClassrooms]       = useState([]);
  const [recipientMode, setRecipientMode] = useState('student'); // 'student' | 'classroom'
  const [sendForm, setSendForm]           = useState({ studentId: '', classroomId: '', message: '', itemType: 'GENERAL' });
  const [sending, setSending]             = useState(false);
  const [error, setError]                 = useState('');
  const [success, setSuccess]             = useState('');
  const [studentQuery, setStudentQuery]   = useState('');
  const [showStudentList, setShowStudentList] = useState(false);
  const [dismissingId, setDismissingId]   = useState(null);

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

  // Load this teacher's classrooms for the "whole class" option
  const loadClassrooms = () => {
    api.get(`/api/classrooms/teacher/${teacherId}`)
      .then(res => setClassrooms(res.data))
      .catch(() => {});
  };

  useEffect(() => {
    loadNotifications();
    loadStudents();
    loadClassrooms();
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

  const dismissNotification = async (notifId) => {
    setDismissingId(notifId);
    try {
      await api.delete(`/api/teacher/${teacherId}/notifications/${notifId}`);
      setNotifications(prev => prev.filter(n => n.id !== notifId));
    } catch {
      setError('Failed to remove notification.');
    } finally {
      setDismissingId(null);
    }
  };

  const clearAllNotifications = async () => {
    if (!window.confirm('Clear all notifications? This cannot be undone.')) return;
    try {
      await api.delete(`/api/teacher/${teacherId}/notifications`);
      setNotifications([]);
    } catch {
      setError('Failed to clear notifications.');
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();

    if (recipientMode === 'classroom') {
      if (!sendForm.classroomId || !sendForm.message.trim()) {
        setError('Please select a classroom and enter a message.');
        return;
      }
      setSending(true);
      setError('');
      try {
        const res = await api.post(`/api/teacher/${teacherId}/notifications/send-classroom`, {
          classroomId: sendForm.classroomId,
          message:     sendForm.message,
          itemType:    sendForm.itemType,
          action:      'INFO',
        });
        setSuccess(`Notification sent to ${res.data.studentsNotified} student(s)!`);
        setSendForm({ studentId: '', classroomId: '', message: '', itemType: 'GENERAL' });
        setShowSendForm(false);
        loadNotifications();
        setTimeout(() => setSuccess(''), 3000);
      } catch {
        setError('Failed to send notification.');
      } finally {
        setSending(false);
      }
      return;
    }

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
      setSendForm({ studentId: '', classroomId: '', message: '', itemType: 'GENERAL' });
      setStudentQuery('');
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
          {notifications.length > 0 && (
            <button onClick={clearAllNotifications} className="btn-secondary text-xs">Clear all</button>
          )}
          <button onClick={() => setShowSendForm(!showSendForm)} className="btn-primary text-xs">
            {showSendForm ? '✕ Cancel' : '+ Send Notification'}
          </button>
        </div>
      </div>

      {/* Send notification form */}
      {showSendForm && (
        <div className="card mb-4 border border-indigo-100">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">📤 Send Notification</h3>
          <form onSubmit={handleSend} className="space-y-3">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Send to</label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setRecipientMode('student')}
                  className={`text-xs px-3 py-1.5 rounded-lg border ${
                    recipientMode === 'student'
                      ? 'bg-indigo-600 text-white border-indigo-600'
                      : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
                  }`}
                >
                  Individual Student
                </button>
                <button
                  type="button"
                  onClick={() => setRecipientMode('classroom')}
                  className={`text-xs px-3 py-1.5 rounded-lg border ${
                    recipientMode === 'classroom'
                      ? 'bg-indigo-600 text-white border-indigo-600'
                      : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
                  }`}
                >
                  Whole Classroom
                </button>
              </div>
            </div>

            {recipientMode === 'classroom' ? (
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Select Classroom *</label>
                <select
                  value={sendForm.classroomId}
                  onChange={e => setSendForm({ ...sendForm, classroomId: e.target.value })}
                  className="input-field text-sm"
                  required
                >
                  <option value="">Choose a classroom...</option>
                  {classrooms.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
            ) : (
            <div className="relative">
              <label className="block text-xs font-medium text-gray-600 mb-1">Select Student *</label>
              <input
                type="text"
                value={studentQuery}
                onChange={e => {
                  setStudentQuery(e.target.value);
                  setShowStudentList(true);
                  setSendForm({ ...sendForm, studentId: '' });
                }}
                onFocus={() => setShowStudentList(true)}
                onBlur={() => setTimeout(() => setShowStudentList(false), 150)}
                className="input-field text-sm"
                placeholder="Type a student name to search..."
                autoComplete="off"
                required={!sendForm.studentId}
              />
              {showStudentList && (
                <div className="absolute z-10 mt-1 w-full max-h-48 overflow-y-auto bg-white border border-gray-200 rounded-lg shadow-lg">
                  {students
                    .filter(s => {
                      const q = studentQuery.trim().toLowerCase();
                      if (!q) return true;
                      return s.user?.name?.toLowerCase().includes(q) ||
                             s.registerNumber?.toLowerCase().includes(q);
                    })
                    .map(s => (
                      <div
                        key={s.user?.id}
                        onMouseDown={() => {
                          setSendForm({ ...sendForm, studentId: s.user?.id });
                          setStudentQuery(`${s.user?.name}${s.registerNumber ? ` (${s.registerNumber})` : ''}`);
                          setShowStudentList(false);
                        }}
                        className="px-3 py-2 text-sm hover:bg-indigo-50 cursor-pointer"
                      >
                        {s.user?.name} {s.registerNumber ? `(${s.registerNumber})` : `— ${s.user?.email}`}
                      </div>
                    ))}
                  {students.filter(s => {
                    const q = studentQuery.trim().toLowerCase();
                    if (!q) return true;
                    return s.user?.name?.toLowerCase().includes(q) ||
                           s.registerNumber?.toLowerCase().includes(q);
                  }).length === 0 && (
                    <div className="px-3 py-2 text-sm text-gray-400">No matching students</div>
                  )}
                </div>
              )}
            </div>
            )}
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
              <button type="button" onClick={() => { setShowSendForm(false); setStudentQuery(''); setSendForm({ studentId: '', classroomId: '', message: '', itemType: 'GENERAL' }); }} className="btn-secondary text-sm">
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
                      notif.action === 'ADDED'     ? 'bg-green-100 text-green-600' :
                      notif.action === 'UPDATED'   ? 'bg-blue-100 text-blue-600'  :
                      notif.action === 'DELETED'   ? 'bg-red-100 text-red-600'    :
                      notif.action === 'COMPLETED' ? 'bg-teal-100 text-teal-700' :
                      'bg-gray-100 text-gray-600'
                    }`}>{notif.action === 'COMPLETED' ? '✓ COMPLETED' : notif.action}</span>
                  )}
                  {notif.student && (
                    <span className="text-xs text-indigo-500">From: {notif.student.name}</span>
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
                {notif.action === 'COMPLETED' ? (
                  <button
                    onClick={() => dismissNotification(notif.id)}
                    disabled={dismissingId === notif.id}
                    className="btn-primary text-xs"
                    title="Remove this notification"
                  >
                    {dismissingId === notif.id ? 'Removing...' : 'Done'}
                  </button>
                ) : (
                  <button
                    onClick={() => dismissNotification(notif.id)}
                    disabled={dismissingId === notif.id}
                    className="text-xs text-gray-300 hover:text-red-500"
                    title="Remove this notification"
                  >
                    ✕
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </TeacherLayout>
  );
}

export default TeacherNotifications;
