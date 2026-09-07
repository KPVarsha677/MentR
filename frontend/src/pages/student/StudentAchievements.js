import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

function StatusBadge({ status }) {
  const classes = { PENDING: 'badge-pending', APPROVED: 'badge-approved', REJECTED: 'badge-rejected' };
  return <span className={classes[status] || 'badge-pending'}>{status}</span>;
}

/**
 * StudentAchievements - manage the student's achievements, awards, honors.
 */
function StudentAchievements() {
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [showForm, setShowForm]         = useState(false);
  const [editItem, setEditItem]         = useState(null);
  const [formData, setFormData] = useState({ title: '', category: '', description: '', achievementDate: '', issuingOrganization: '' });
  const [error, setError]               = useState('');

  const userId = localStorage.getItem('userId');

  useEffect(() => { loadAchievements(); }, []);

  const loadAchievements = () => {
    api.get(`/api/student/${userId}/achievements`)
      .then(res => setAchievements(res.data))
      .catch(() => setError('Could not load achievements'))
      .finally(() => setLoading(false));
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleAdd = () => {
    setFormData({ title: '', category: '', description: '', achievementDate: '', issuingOrganization: '' });
    setEditItem(null);
    setShowForm(true);
  };

  const handleEdit = (item) => {
    setFormData({
      title:               item.title               || '',
      category:            item.category            || '',
      description:         item.description         || '',
      achievementDate:     item.achievementDate     || '',
      issuingOrganization: item.issuingOrganization || ''
    });
    setEditItem(item);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editItem) {
        await api.put(`/api/student/${userId}/achievements/${editItem.id}`, formData);
      } else {
        await api.post(`/api/student/${userId}/achievements`, formData);
      }
      setShowForm(false);
      loadAchievements();
    } catch (err) {
      setError('Failed to save achievement');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this achievement?')) return;
    await api.delete(`/api/student/${userId}/achievements/${id}`);
    loadAchievements();
  };

  return (
    <StudentLayout title="My Achievements">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}
      <div className="flex justify-between items-center mb-6">
        <p className="text-gray-500 text-sm">{achievements.length} achievement(s)</p>
        <button onClick={handleAdd} className="btn-primary">+ Add Achievement</button>
      </div>

      {showForm && (
        <div className="card mb-6">
          <h3 className="text-base font-semibold mb-4">{editItem ? 'Edit Achievement' : 'Add Achievement'}</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Title *</label>
              <input name="title" value={formData.title} onChange={handleChange} className="input-field" required placeholder="e.g., First Place - Hackathon 2024" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Category</label>
              <select name="category" value={formData.category} onChange={handleChange} className="input-field">
                <option value="">Select Category</option>
                <option value="Technical">Technical</option>
                <option value="Academic">Academic</option>
                <option value="Sports">Sports</option>
                <option value="Cultural">Cultural</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Description</label>
              <textarea name="description" value={formData.description} onChange={handleChange} className="input-field" rows="3" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Date</label>
                <input type="date" name="achievementDate" value={formData.achievementDate} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Issuing Organization</label>
                <input name="issuingOrganization" value={formData.issuingOrganization} onChange={handleChange} className="input-field" placeholder="e.g., IEEE" />
              </div>
            </div>
            <div className="flex gap-3">
              <button type="submit" className="btn-primary">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? <div className="text-center py-12 text-gray-400">Loading...</div> :
       achievements.length === 0 ? (
        <div className="text-center py-12 text-gray-400"><p className="text-5xl mb-4">🌟</p><p>No achievements yet.</p></div>
       ) : (
        <div className="space-y-4">
          {achievements.map(item => (
            <div key={item.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center gap-3 mb-1">
                    <h4 className="font-semibold text-gray-800">{item.title}</h4>
                    <StatusBadge status={item.verificationStatus} />
                  </div>
                  {item.category            && <span className="text-xs bg-orange-50 text-orange-600 px-2 py-0.5 rounded-full">{item.category}</span>}
                  {item.issuingOrganization && <p className="text-sm text-gray-600 mt-1">{item.issuingOrganization}</p>}
                  {item.achievementDate     && <p className="text-xs text-gray-400 mt-1">Date: {item.achievementDate}</p>}
                  {item.teacherComment      && <p className="text-xs text-gray-500 mt-2 italic bg-gray-50 p-2 rounded">💬 {item.teacherComment}</p>}
                </div>
                <div className="flex gap-2">
                  <button onClick={() => handleEdit(item)} className="text-sm text-blue-600 hover:underline">Edit</button>
                  <button onClick={() => handleDelete(item.id)} className="text-sm text-red-500 hover:underline">Delete</button>
                </div>
              </div>
            </div>
          ))}
        </div>
       )}
    </StudentLayout>
  );
}

export default StudentAchievements;
