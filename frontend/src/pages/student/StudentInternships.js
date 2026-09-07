import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

function StatusBadge({ status }) {
  const classes = { PENDING: 'badge-pending', APPROVED: 'badge-approved', REJECTED: 'badge-rejected' };
  return <span className={classes[status] || 'badge-pending'}>{status}</span>;
}

/**
 * StudentInternships - manage the student's internship records.
 */
function StudentInternships() {
  const [internships, setInternships] = useState([]);
  const [loading, setLoading]         = useState(true);
  const [showForm, setShowForm]       = useState(false);
  const [editItem, setEditItem]       = useState(null);
  const [formData, setFormData] = useState({ companyName: '', role: '', description: '', startDate: '', endDate: '', ongoing: false, location: '', stipend: '' });
  const [error, setError]             = useState('');

  const userId = localStorage.getItem('userId');

  useEffect(() => { loadInternships(); }, []);

  const loadInternships = () => {
    api.get(`/api/student/${userId}/internships`)
      .then(res => setInternships(res.data))
      .catch(() => setError('Could not load internships'))
      .finally(() => setLoading(false));
  };

  const handleChange = (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setFormData({ ...formData, [e.target.name]: val });
  };

  const handleAdd = () => {
    setFormData({ companyName: '', role: '', description: '', startDate: '', endDate: '', ongoing: false, location: '', stipend: '' });
    setEditItem(null);
    setShowForm(true);
  };

  const handleEdit = (item) => {
    setFormData({
      companyName: item.companyName || '', role:        item.role        || '',
      description: item.description || '', startDate:   item.startDate   || '',
      endDate:     item.endDate     || '', ongoing:     item.ongoing     || false,
      location:    item.location    || '', stipend:     item.stipend     || ''
    });
    setEditItem(item);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editItem) {
        await api.put(`/api/student/${userId}/internships/${editItem.id}`, formData);
      } else {
        await api.post(`/api/student/${userId}/internships`, formData);
      }
      setShowForm(false);
      loadInternships();
    } catch (err) {
      setError('Failed to save internship');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this internship?')) return;
    await api.delete(`/api/student/${userId}/internships/${id}`);
    loadInternships();
  };

  return (
    <StudentLayout title="My Internships">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}
      <div className="flex justify-between items-center mb-6">
        <p className="text-gray-500 text-sm">{internships.length} internship(s)</p>
        <button onClick={handleAdd} className="btn-primary">+ Add Internship</button>
      </div>

      {showForm && (
        <div className="card mb-6">
          <h3 className="text-base font-semibold mb-4">{editItem ? 'Edit Internship' : 'Add Internship'}</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Company Name *</label>
              <input name="companyName" value={formData.companyName} onChange={handleChange} className="input-field" required placeholder="e.g., Infosys" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Role</label>
              <input name="role" value={formData.role} onChange={handleChange} className="input-field" placeholder="e.g., Software Development Intern" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Description</label>
              <textarea name="description" value={formData.description} onChange={handleChange} className="input-field" rows="3" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Start Date</label>
                <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">End Date</label>
                <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} className="input-field" disabled={formData.ongoing} />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <input type="checkbox" name="ongoing" checked={formData.ongoing} onChange={handleChange} id="ongoing" />
              <label htmlFor="ongoing" className="text-sm text-gray-600">Currently ongoing</label>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Location</label>
                <input name="location" value={formData.location} onChange={handleChange} className="input-field" placeholder="e.g., Chennai / Remote" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Stipend (per month)</label>
                <input name="stipend" value={formData.stipend} onChange={handleChange} className="input-field" placeholder="e.g., ₹10,000" />
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
       internships.length === 0 ? (
        <div className="text-center py-12 text-gray-400"><p className="text-5xl mb-4">🏢</p><p>No internships yet.</p></div>
       ) : (
        <div className="space-y-4">
          {internships.map(item => (
            <div key={item.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center gap-3 mb-1">
                    <h4 className="font-semibold text-gray-800">{item.companyName}</h4>
                    <StatusBadge status={item.verificationStatus} />
                    {item.ongoing && <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">Ongoing</span>}
                  </div>
                  {item.role     && <p className="text-sm text-gray-600">{item.role}</p>}
                  {item.location && <p className="text-xs text-gray-400">{item.location}</p>}
                  {item.startDate && <p className="text-xs text-gray-400 mt-1">{item.startDate} → {item.ongoing ? 'Present' : item.endDate}</p>}
                  {item.stipend  && <p className="text-xs text-green-600 mt-1">Stipend: {item.stipend}</p>}
                  {item.teacherComment && <p className="text-xs text-gray-500 mt-2 italic bg-gray-50 p-2 rounded">💬 {item.teacherComment}</p>}
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

export default StudentInternships;
