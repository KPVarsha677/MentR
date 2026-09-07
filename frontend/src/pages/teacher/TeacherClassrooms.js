import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * TeacherClassrooms - allows teachers to create, manage, and view classrooms.
 *
 * WHY THIS PAGE EXISTS:
 * Teachers create virtual classrooms that students join.
 * This page shows all classrooms and lets teachers:
 * - Create new classrooms
 * - See the join code for each classroom
 * - View students in each classroom
 * - Edit or delete classrooms
 */
function TeacherClassrooms() {
  const [classrooms, setClassrooms] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [showForm, setShowForm]     = useState(false);
  const [editClassroom, setEditClassroom] = useState(null);
  const [formData, setFormData] = useState({ name: '', description: '', subject: '', academicYear: '' });
  const [selectedClassroom, setSelectedClassroom] = useState(null);
  const [students, setStudents]     = useState([]);
  const [error, setError]           = useState('');

  const teacherId = localStorage.getItem('userId');

  useEffect(() => { loadClassrooms(); }, []);

  const loadClassrooms = () => {
    if (!teacherId || teacherId === 'null') {
      setError('Session error: please log out and log in again.');
      setLoading(false);
      return;
    }
    api.get(`/api/classrooms/teacher/${teacherId}`)
      .then(res => setClassrooms(res.data))
      .catch(err => {
        const msg = err.response?.data?.error || 'Could not load classrooms';
        setError(msg);
      })
      .finally(() => setLoading(false));
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleAdd = () => {
    setFormData({ name: '', description: '', subject: '', academicYear: '' });
    setEditClassroom(null);
    setShowForm(true);
  };

  const handleEdit = (classroom) => {
    setFormData({
      name:         classroom.name         || '',
      description:  classroom.description  || '',
      subject:      classroom.subject      || '',
      academicYear: classroom.academicYear || ''
    });
    setEditClassroom(classroom);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Guard: teacherId must be a real number stored in localStorage.
    // localStorage.getItem() returns null or the string "null" if the key
    // was never set or was set to null — both of which break the ?teacherId= param.
    if (!teacherId || teacherId === 'null') {
      setError('Session error: could not identify your account. Please log out and log in again.');
      return;
    }

    try {
      if (editClassroom) {
        await api.put(`/api/classrooms/${editClassroom.id}?teacherId=${teacherId}`, formData);
      } else {
        await api.post(`/api/classrooms?teacherId=${teacherId}`, formData);
      }
      setShowForm(false);
      loadClassrooms();
    } catch (err) {
      // Show the actual server error message so the failure is visible and debuggable.
      // Fall back to a generic message only if the server sent no details.
      const serverMsg = err.response?.data?.error
                     || err.response?.data?.name
                     || err.message
                     || 'Failed to save classroom';
      setError(serverMsg);
    }
  };

  const handleDelete = async (classroomId) => {
    if (!window.confirm('Delete this classroom? All students will be removed.')) return;
    await api.delete(`/api/classrooms/${classroomId}?teacherId=${teacherId}`);
    loadClassrooms();
  };

  const viewStudents = async (classroom) => {
    setSelectedClassroom(classroom);
    const res = await api.get(`/api/classrooms/${classroom.id}/members`);
    setStudents(res.data);
  };

  return (
    <TeacherLayout title="My Classrooms">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}

      <div className="flex justify-between items-center mb-6">
        <p className="text-gray-500 text-sm">{classrooms.length} classroom(s)</p>
        <button onClick={handleAdd} className="btn-primary">+ Create Classroom</button>
      </div>

      {/* Create/Edit Form */}
      {showForm && (
        <div className="card mb-6">
          <h3 className="text-base font-semibold mb-4">{editClassroom ? 'Edit Classroom' : 'Create New Classroom'}</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Classroom Name *</label>
              <input name="name" value={formData.name} onChange={handleChange} className="input-field" required placeholder="e.g., CS 2nd Year - Section A" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Description</label>
              <input name="description" value={formData.description} onChange={handleChange} className="input-field" placeholder="Brief description" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Subject</label>
                <input name="subject" value={formData.subject} onChange={handleChange} className="input-field" placeholder="e.g., Data Structures" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Academic Year</label>
                <input name="academicYear" value={formData.academicYear} onChange={handleChange} className="input-field" placeholder="e.g., 2024-2025" />
              </div>
            </div>
            <div className="flex gap-3">
              <button type="submit" className="btn-primary">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {/* Classroom List */}
      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading classrooms...</div>
      ) : classrooms.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">📚</p>
          <p>No classrooms yet. Create your first classroom!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {classrooms.map(classroom => (
            <div key={classroom.id} className="card">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <h4 className="font-semibold text-gray-800">{classroom.name}</h4>
                  {classroom.subject && <p className="text-sm text-gray-500">{classroom.subject}</p>}
                  {classroom.academicYear && <p className="text-xs text-gray-400">{classroom.academicYear}</p>}
                </div>
                <div className="flex gap-2 text-sm">
                  <button onClick={() => handleEdit(classroom)} className="text-blue-600 hover:underline">Edit</button>
                  <button onClick={() => handleDelete(classroom.id)} className="text-red-500 hover:underline">Delete</button>
                </div>
              </div>

              {/* Join Code - the most important piece of info */}
              <div className="bg-indigo-50 border border-indigo-100 rounded-lg p-3 mb-3">
                <p className="text-xs text-indigo-500 font-medium mb-1">JOIN CODE</p>
                <p className="text-2xl font-mono font-bold text-indigo-700 tracking-widest">{classroom.joinCode}</p>
                <p className="text-xs text-indigo-400 mt-1">Share this code with your students</p>
              </div>

              <button
                onClick={() => viewStudents(classroom)}
                className="w-full btn-secondary text-sm"
              >
                👥 View Students
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Student List Modal */}
      {selectedClassroom && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg max-h-screen overflow-y-auto p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">Students in {selectedClassroom.name}</h3>
              <button onClick={() => setSelectedClassroom(null)} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
            </div>

            {students.length === 0 ? (
              <p className="text-gray-400 text-center py-8">No students have joined yet.</p>
            ) : (
              <div className="space-y-2">
                {students.map((member, idx) => (
                  <div key={member.id} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <div>
                      <p className="font-medium text-gray-800">{idx + 1}. {member.student.name}</p>
                      <p className="text-xs text-gray-400">{member.student.email}</p>
                    </div>
                    <Link
                      to={`/teacher/students/${member.student.id}`}
                      className="text-sm text-blue-600 hover:underline"
                    >
                      View Profile →
                    </Link>
                  </div>
                ))}
                <p className="text-xs text-gray-400 text-right pt-1">Total: {students.length} student(s)</p>
              </div>
            )}
          </div>
        </div>
      )}
    </TeacherLayout>
  );
}

export default TeacherClassrooms;
