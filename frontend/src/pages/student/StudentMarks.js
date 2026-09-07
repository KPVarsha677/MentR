import React, { useState, useEffect, useCallback } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

/**
 * StudentMarks - allows students to manage their academic marks across 8 semesters.
 *
 * Each semester tab contains a table of subjects with:
 * Subject Code | Subject Name | UT1 | UT2 | CAT1 | CAT2 | Semester Exam
 *
 * Students can add, edit, delete subjects. Data persists to the database.
 */
function StudentMarks() {
  const [activeSemester, setActiveSemester] = useState(1);
  const [records, setRecords]               = useState([]);        // records for active semester
  const [loading, setLoading]               = useState(true);
  const [saving, setSaving]                 = useState(false);
  const [showForm, setShowForm]             = useState(false);
  const [editRecord, setEditRecord]         = useState(null);      // null = adding new
  const [error, setError]                   = useState('');
  const [success, setSuccess]               = useState('');

  const userId = localStorage.getItem('userId');

  const emptyForm = {
    subjectCode:  '',
    subjectName:  '',
    unitTest1:    '',
    unitTest2:    '',
    cat1:         '',
    cat2:         '',
    semesterExam: '',
  };
  const [formData, setFormData] = useState(emptyForm);

  // Load records for the currently active semester
  const loadRecords = useCallback(() => {
    setLoading(true);
    api.get(`/api/student/${userId}/academic-records?semester=${activeSemester}`)
      .then(res => setRecords(res.data))
      .catch(() => setError('Failed to load marks'))
      .finally(() => setLoading(false));
  }, [userId, activeSemester]);

  useEffect(() => {
    loadRecords();
  }, [loadRecords]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const openAdd = () => {
    setFormData(emptyForm);
    setEditRecord(null);
    setShowForm(true);
    setError('');
  };

  const openEdit = (record) => {
    setFormData({
      subjectCode:  record.subjectCode  || '',
      subjectName:  record.subjectName  || '',
      unitTest1:    record.unitTest1    ?? '',
      unitTest2:    record.unitTest2    ?? '',
      cat1:         record.cat1         ?? '',
      cat2:         record.cat2         ?? '',
      semesterExam: record.semesterExam ?? '',
    });
    setEditRecord(record);
    setShowForm(true);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.subjectCode.trim() || !formData.subjectName.trim()) {
      setError('Subject Code and Subject Name are required.');
      return;
    }

    setSaving(true);
    setError('');

    const payload = {
      semester:     activeSemester,
      subjectCode:  formData.subjectCode.trim(),
      subjectName:  formData.subjectName.trim(),
      unitTest1:    formData.unitTest1    !== '' ? Number(formData.unitTest1)    : null,
      unitTest2:    formData.unitTest2    !== '' ? Number(formData.unitTest2)    : null,
      cat1:         formData.cat1         !== '' ? Number(formData.cat1)         : null,
      cat2:         formData.cat2         !== '' ? Number(formData.cat2)         : null,
      semesterExam: formData.semesterExam !== '' ? Number(formData.semesterExam) : null,
    };

    try {
      if (editRecord) {
        await api.put(`/api/student/${userId}/academic-records/${editRecord.id}`, payload);
      } else {
        await api.post(`/api/student/${userId}/academic-records`, payload);
      }
      setSuccess(editRecord ? 'Record updated!' : 'Subject added!');
      setShowForm(false);
      setFormData(emptyForm);
      setEditRecord(null);
      loadRecords();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      const msg = err.response?.data?.message
               || err.response?.data?.error
               || (editRecord ? 'Failed to update record.' : 'Failed to add subject.');
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (recordId) => {
    if (!window.confirm('Delete this subject record?')) return;
    try {
      await api.delete(`/api/student/${userId}/academic-records/${recordId}`);
      setSuccess('Record deleted.');
      loadRecords();
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to delete record.');
    }
  };

  // Helper — show dash if value is null/undefined
  const fmt = (v) => (v === null || v === undefined || v === '') ? '—' : v;

  return (
    <StudentLayout title="Academic Marks">
      {error   && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4 text-sm">{error}</div>}
      {success && <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4 text-sm">{success}</div>}

      {/* Semester Tabs */}
      <div className="flex gap-1 mb-5 overflow-x-auto border-b border-slate-200 pb-0">
        {[1, 2, 3, 4, 5, 6, 7, 8].map(sem => (
          <button
            key={sem}
            onClick={() => { setActiveSemester(sem); setShowForm(false); setError(''); }}
            className={`px-4 py-2 text-sm font-medium whitespace-nowrap border-b-2 transition-colors
              ${activeSemester === sem
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'}`}
          >
            Sem {sem}
          </button>
        ))}
      </div>

      {/* Action Row */}
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-sm font-semibold text-slate-700">
          📖 Semester {activeSemester} — {records.length} subject(s)
        </h3>
        {!showForm && (
          <button onClick={openAdd} className="btn-primary text-sm">+ Add Subject</button>
        )}
      </div>

      {/* Add / Edit Form */}
      {showForm && (
        <div className="card mb-5 border border-blue-100">
          <h4 className="text-sm font-semibold text-slate-700 mb-4">
            {editRecord ? '✏️ Edit Subject' : '➕ Add Subject — Semester ' + activeSemester}
          </h4>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Subject Code *</label>
                <input
                  name="subjectCode"
                  value={formData.subjectCode}
                  onChange={handleChange}
                  className="input-field"
                  placeholder="e.g., CS301"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Subject Name *</label>
                <input
                  name="subjectName"
                  value={formData.subjectName}
                  onChange={handleChange}
                  className="input-field"
                  placeholder="e.g., Data Structures"
                  required
                />
              </div>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
              {[
                { label: 'Unit Test 1', name: 'unitTest1' },
                { label: 'Unit Test 2', name: 'unitTest2' },
                { label: 'CAT 1',       name: 'cat1' },
                { label: 'CAT 2',       name: 'cat2' },
                { label: 'Sem Exam',    name: 'semesterExam' },
              ].map(field => (
                <div key={field.name}>
                  <label className="block text-xs font-medium text-gray-600 mb-1">{field.label}</label>
                  <input
                    name={field.name}
                    type="number"
                    min="0"
                    max="100"
                    value={formData[field.name]}
                    onChange={handleChange}
                    className="input-field"
                    placeholder="0–100"
                  />
                </div>
              ))}
            </div>
            <div className="flex gap-3">
              <button type="submit" disabled={saving} className="btn-primary">
                {saving ? 'Saving...' : editRecord ? 'Update' : 'Add Subject'}
              </button>
              <button
                type="button"
                onClick={() => { setShowForm(false); setEditRecord(null); setError(''); }}
                className="btn-secondary"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Marks Table */}
      {loading ? (
        <div className="text-center py-10 text-gray-400">Loading marks...</div>
      ) : records.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-4xl mb-3">📋</p>
          <p className="text-sm">No subjects added for Semester {activeSemester} yet.</p>
          <button onClick={openAdd} className="btn-primary mt-4 text-sm">+ Add First Subject</button>
        </div>
      ) : (
        <div className="card overflow-x-auto p-0">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200">
                <th className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Subject Code</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Subject Name</th>
                <th className="text-center px-3 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">UT 1</th>
                <th className="text-center px-3 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">UT 2</th>
                <th className="text-center px-3 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">CAT 1</th>
                <th className="text-center px-3 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">CAT 2</th>
                <th className="text-center px-3 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Sem Exam</th>
                <th className="text-right px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {records.map(record => (
                <tr key={record.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-blue-700">{record.subjectCode}</td>
                  <td className="px-4 py-3 font-medium text-slate-800">{record.subjectName}</td>
                  <td className="px-3 py-3 text-center text-slate-600">{fmt(record.unitTest1)}</td>
                  <td className="px-3 py-3 text-center text-slate-600">{fmt(record.unitTest2)}</td>
                  <td className="px-3 py-3 text-center text-slate-600">{fmt(record.cat1)}</td>
                  <td className="px-3 py-3 text-center text-slate-600">{fmt(record.cat2)}</td>
                  <td className="px-3 py-3 text-center text-slate-600">{fmt(record.semesterExam)}</td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-3">
                      <button
                        onClick={() => openEdit(record)}
                        className="text-xs text-blue-600 hover:underline"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(record.id)}
                        className="text-xs text-red-500 hover:underline"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Info tip */}
      {records.length > 0 && (
        <p className="text-xs text-slate-400 mt-3">
          💡 Marks are saved automatically to the database. They will be available after logging out and back in.
        </p>
      )}
    </StudentLayout>
  );
}

export default StudentMarks;
