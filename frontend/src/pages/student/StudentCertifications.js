import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

function StatusBadge({ status }) {
  const classes = { PENDING: 'badge-pending', APPROVED: 'badge-approved', REJECTED: 'badge-rejected' };
  return <span className={classes[status] || 'badge-pending'}>{status}</span>;
}

/**
 * StudentCertifications - manage student's certification records.
 */
function StudentCertifications() {
  const [certs, setCerts]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editCert, setEditCert] = useState(null);
  const [formData, setFormData] = useState({ name: '', issuingOrganization: '', issueDate: '', expirationDate: '', credentialUrl: '', credentialId: '' });
  const [error, setError]       = useState('');

  const userId = localStorage.getItem('userId');

  useEffect(() => { loadCerts(); }, []);

  const loadCerts = () => {
    api.get(`/api/student/${userId}/certifications`)
      .then(res => setCerts(res.data))
      .catch(() => setError('Could not load certifications'))
      .finally(() => setLoading(false));
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleAdd = () => {
    setFormData({ name: '', issuingOrganization: '', issueDate: '', expirationDate: '', credentialUrl: '', credentialId: '' });
    setEditCert(null);
    setShowForm(true);
  };

  const handleEdit = (cert) => {
    setFormData({
      name:                cert.name                || '',
      issuingOrganization: cert.issuingOrganization || '',
      issueDate:           cert.issueDate           || '',
      expirationDate:      cert.expirationDate      || '',
      credentialUrl:       cert.credentialUrl       || '',
      credentialId:        cert.credentialId        || ''
    });
    setEditCert(cert);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editCert) {
        await api.put(`/api/student/${userId}/certifications/${editCert.id}`, formData);
      } else {
        await api.post(`/api/student/${userId}/certifications`, formData);
      }
      setShowForm(false);
      loadCerts();
    } catch (err) {
      setError('Failed to save certification');
    }
  };

  const handleDelete = async (certId) => {
    if (!window.confirm('Delete this certification?')) return;
    await api.delete(`/api/student/${userId}/certifications/${certId}`);
    loadCerts();
  };

  return (
    <StudentLayout title="My Certifications">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}
      <div className="flex justify-between items-center mb-6">
        <p className="text-gray-500 text-sm">{certs.length} certification(s)</p>
        <button onClick={handleAdd} className="btn-primary">+ Add Certification</button>
      </div>

      {showForm && (
        <div className="card mb-6">
          <h3 className="text-base font-semibold mb-4">{editCert ? 'Edit Certification' : 'Add Certification'}</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Certification Name *</label>
              <input name="name" value={formData.name} onChange={handleChange} className="input-field" required placeholder="e.g., AWS Cloud Practitioner" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Issuing Organization</label>
              <input name="issuingOrganization" value={formData.issuingOrganization} onChange={handleChange} className="input-field" placeholder="e.g., Amazon Web Services" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Issue Date *</label>
                <input type="date" name="issueDate" value={formData.issueDate} onChange={handleChange} className="input-field" required />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Expiration Date *</label>
                <input type="date" name="expirationDate" value={formData.expirationDate} onChange={handleChange} className="input-field" required />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Credential URL</label>
              <input name="credentialUrl" value={formData.credentialUrl} onChange={handleChange} className="input-field" placeholder="https://..." />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Credential ID</label>
              <input name="credentialId" value={formData.credentialId} onChange={handleChange} className="input-field" placeholder="Certificate ID" />
            </div>
            <div className="flex gap-3">
              <button type="submit" className="btn-primary">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? <div className="text-center py-12 text-gray-400">Loading...</div> :
       certs.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">🏆</p>
          <p>No certifications yet.</p>
        </div>
       ) : (
        <div className="space-y-4">
          {certs.map(cert => (
            <div key={cert.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center gap-3 mb-1">
                    <h4 className="font-semibold text-gray-800">{cert.name}</h4>
                    <StatusBadge status={cert.verificationStatus} />
                  </div>
                  {cert.issuingOrganization && <p className="text-sm text-gray-600">{cert.issuingOrganization}</p>}
                  {cert.issueDate && <p className="text-xs text-gray-400 mt-1">Issued: {cert.issueDate}</p>}
                  {cert.credentialUrl && <a href={cert.credentialUrl} target="_blank" rel="noreferrer" className="text-xs text-blue-500 hover:underline mt-1 block">View Certificate</a>}
                  {cert.teacherComment && <p className="text-xs text-gray-500 mt-2 italic bg-gray-50 p-2 rounded">💬 {cert.teacherComment}</p>}
                </div>
                <div className="flex gap-2">
                  <button onClick={() => handleEdit(cert)} className="text-sm text-blue-600 hover:underline">Edit</button>
                  <button onClick={() => handleDelete(cert.id)} className="text-sm text-red-500 hover:underline">Delete</button>
                </div>
              </div>
            </div>
          ))}
        </div>
       )}
    </StudentLayout>
  );
}

export default StudentCertifications;
