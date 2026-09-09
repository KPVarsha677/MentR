import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

/**
 * StudentProfile - allows students to view and edit their personal/academic info.
 *
 * WHY THIS PAGE EXISTS:
 * This is the student's personal information page.
 * It covers: Register Number, Department, Year, Section, Batch,
 * Phone, Address, LinkedIn, GitHub, Career Goal, About.
 *
 * HOW IT WORKS:
 * 1. Load current profile on page load
 * 2. Student edits the form
 * 3. Click Save → PUT /api/student/{id}/profile
 */
function StudentProfile() {
  const [profile, setProfile] = useState({
    registerNumber: '', department: '', year: '', section: '', batch: '',
    phone: '', address: '', linkedinUrl: '', githubUrl: '',
    careerGoal: '', about: ''
  });
  const [loading, setLoading]   = useState(true);
  const [saving, setSaving]     = useState(false);
  const [success, setSuccess]   = useState(false);
  const [error, setError]       = useState('');

  const userId = localStorage.getItem('userId');

  useEffect(() => {
    api.get(`/api/student/${userId}/profile`)
      .then(res => {
        // Fill in the form with existing data (replace null with empty string)
        const data = res.data;
        setProfile({
          registerNumber: data.registerNumber || '',
          department:     data.department     || '',
          year:           data.year           || '',
          section:        data.section        || '',
          batch:          data.batch          || '',
          phone:          data.phone          || '',
          address:        data.address        || '',
          linkedinUrl:    data.linkedinUrl    || '',
          githubUrl:      data.githubUrl      || '',
          careerGoal:     data.careerGoal     || '',
          about:          data.about          || ''
        });
      })
      .catch(() => setError('Could not load profile'))
      .finally(() => setLoading(false));
  }, [userId]);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSuccess(false);
    setError('');

    try {
      await api.put(`/api/student/${userId}/profile`, profile);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError('Failed to save profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <StudentLayout title="My Profile"><div className="text-center py-12 text-gray-400">Loading...</div></StudentLayout>;

  return (
    <StudentLayout title="My Profile">
      <form onSubmit={handleSave}>
        {success && <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4">Profile saved successfully!</div>}
        {error   && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}

        {/* Academic Information */}
        <div className="card mb-6">
          <h3 className="text-base font-semibold text-gray-700 mb-4">🎓 Academic Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Register Number *</label>
              <input name="registerNumber" value={profile.registerNumber} onChange={handleChange} className="input-field" placeholder="e.g., 21CS001" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Department *</label>
              <input name="department" value={profile.department} onChange={handleChange} className="input-field" placeholder="e.g., Computer Science" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Year *</label>
              <select name="year" value={profile.year} onChange={handleChange} className="input-field" required>
                <option value="">Select Year</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Section *</label>
              <input name="section" value={profile.section} onChange={handleChange} className="input-field" placeholder="e.g., A" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Batch *</label>
              <input name="batch" value={profile.batch} onChange={handleChange} className="input-field" placeholder="e.g., 2021-2025" required />
            </div>
          </div>
        </div>

        {/* Personal Information */}
        <div className="card mb-6">
          <h3 className="text-base font-semibold text-gray-700 mb-4">👤 Personal Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Phone *</label>
              <input name="phone" value={profile.phone} onChange={handleChange} className="input-field" placeholder="e.g., 9876543210" required />
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-600 mb-1">Address *</label>
              <input name="address" value={profile.address} onChange={handleChange} className="input-field" placeholder="Your address" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">LinkedIn URL *</label>
              <input name="linkedinUrl" value={profile.linkedinUrl} onChange={handleChange} className="input-field" placeholder="https://linkedin.com/in/..." required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">GitHub URL *</label>
              <input name="githubUrl" value={profile.githubUrl} onChange={handleChange} className="input-field" placeholder="https://github.com/..." required />
            </div>
          </div>
        </div>

        {/* Career Goals */}
        <div className="card mb-6">
          <h3 className="text-base font-semibold text-gray-700 mb-4">🎯 Career Goals</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Career Goal</label>
              <input name="careerGoal" value={profile.careerGoal} onChange={handleChange} className="input-field" placeholder="e.g., Software Engineer at a product company" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">About Me</label>
              <textarea name="about" value={profile.about} onChange={handleChange} className="input-field" rows="4" placeholder="Write a short bio about yourself..." />
            </div>
          </div>
        </div>

        <button type="submit" disabled={saving} className="btn-primary px-8">
          {saving ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </StudentLayout>
  );
}

export default StudentProfile;
