import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

function StatusBadge({ status }) {
  const classes = { PENDING: 'badge-pending', APPROVED: 'badge-approved', REJECTED: 'badge-rejected' };
  return <span className={classes[status] || 'badge-pending'}>{status}</span>;
}

/**
 * StudentSkills - manage the student's skills section.
 */
function StudentSkills() {
  const [skills, setSkills]     = useState([]);
  const [loading, setLoading]   = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editSkill, setEditSkill] = useState(null);
  const [formData, setFormData] = useState({ name: '', category: '', proficiencyLevel: '' });
  const [error, setError]       = useState('');

  const userId = localStorage.getItem('userId');

  useEffect(() => { loadSkills(); }, []);

  const loadSkills = () => {
    api.get(`/api/student/${userId}/skills`)
      .then(res => setSkills(res.data))
      .catch(() => setError('Could not load skills'))
      .finally(() => setLoading(false));
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleAdd = () => {
    setFormData({ name: '', category: '', proficiencyLevel: '' });
    setEditSkill(null);
    setShowForm(true);
  };

  const handleEdit = (skill) => {
    setFormData({ name: skill.name || '', category: skill.category || '', proficiencyLevel: skill.proficiencyLevel || '' });
    setEditSkill(skill);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editSkill) {
        await api.put(`/api/student/${userId}/skills/${editSkill.id}`, formData);
      } else {
        await api.post(`/api/student/${userId}/skills`, formData);
      }
      setShowForm(false);
      loadSkills();
    } catch (err) {
      setError('Failed to save skill');
    }
  };

  const handleDelete = async (skillId) => {
    if (!window.confirm('Delete this skill?')) return;
    await api.delete(`/api/student/${userId}/skills/${skillId}`);
    loadSkills();
  };

  return (
    <StudentLayout title="My Skills">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}
      <div className="flex justify-between items-center mb-6">
        <p className="text-gray-500 text-sm">{skills.length} skill(s)</p>
        <button onClick={handleAdd} className="btn-primary">+ Add Skill</button>
      </div>

      {showForm && (
        <div className="card mb-6">
          <h3 className="text-base font-semibold mb-4">{editSkill ? 'Edit Skill' : 'Add Skill'}</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Skill Name *</label>
              <input name="name" value={formData.name} onChange={handleChange} className="input-field" required placeholder="e.g., React.js" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Category</label>
              <select name="category" value={formData.category} onChange={handleChange} className="input-field">
                <option value="">Select Category</option>
                <option value="Programming">Programming</option>
                <option value="Framework">Framework</option>
                <option value="Database">Database</option>
                <option value="Tools">Tools</option>
                <option value="Soft Skills">Soft Skills</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Proficiency Level</label>
              <select name="proficiencyLevel" value={formData.proficiencyLevel} onChange={handleChange} className="input-field">
                <option value="">Select Level</option>
                <option value="Beginner">Beginner</option>
                <option value="Intermediate">Intermediate</option>
                <option value="Advanced">Advanced</option>
              </select>
            </div>
            <div className="flex gap-3">
              <button type="submit" className="btn-primary">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? <div className="text-center py-12 text-gray-400">Loading...</div> :
       skills.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">⚡</p>
          <p>No skills added yet.</p>
        </div>
       ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {skills.map(skill => (
            <div key={skill.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <h4 className="font-semibold text-gray-800">{skill.name}</h4>
                    <StatusBadge status={skill.verificationStatus} />
                  </div>
                  {skill.category && <p className="text-xs text-gray-500">{skill.category}</p>}
                  {skill.proficiencyLevel && <p className="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded mt-1 inline-block">{skill.proficiencyLevel}</p>}
                  {skill.teacherComment && <p className="text-xs text-gray-500 mt-2 italic">"{skill.teacherComment}"</p>}
                </div>
                <div className="flex gap-2">
                  <button onClick={() => handleEdit(skill)} className="text-xs text-blue-600 hover:underline">Edit</button>
                  <button onClick={() => handleDelete(skill.id)} className="text-xs text-red-500 hover:underline">Del</button>
                </div>
              </div>
            </div>
          ))}
        </div>
       )}
    </StudentLayout>
  );
}

export default StudentSkills;
