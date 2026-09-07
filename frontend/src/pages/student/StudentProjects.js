import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

/**
 * StatusBadge - a reusable component to show verification status.
 * PENDING → yellow, APPROVED → green, REJECTED → red
 */
function StatusBadge({ status }) {
  const classes = {
    PENDING:  'badge-pending',
    APPROVED: 'badge-approved',
    REJECTED: 'badge-rejected'
  };
  return <span className={classes[status] || 'badge-pending'}>{status}</span>;
}

/**
 * StudentProjects - manage the student's project portfolio.
 *
 * WHY THIS PAGE EXISTS:
 * Students can add, view, edit, and delete their projects here.
 * Every project shows its verification status and teacher comment.
 */
function StudentProjects() {
  const [projects, setProjects]       = useState([]);
  const [loading, setLoading]         = useState(true);
  const [showForm, setShowForm]       = useState(false);
  const [editProject, setEditProject] = useState(null);
  const [formData, setFormData]       = useState({ title: '', description: '', techStack: '', projectUrl: '', startDate: '', endDate: '' });
  const [error, setError]             = useState('');

  const userId = localStorage.getItem('userId');

  // Load projects on page mount
  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = () => {
    api.get(`/api/student/${userId}/projects`)
      .then(res => setProjects(res.data))
      .catch(() => setError('Could not load projects'))
      .finally(() => setLoading(false));
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Open "Add" form with empty data
  const handleAdd = () => {
    setFormData({ title: '', description: '', techStack: '', projectUrl: '', startDate: '', endDate: '' });
    setEditProject(null);
    setShowForm(true);
  };

  // Open "Edit" form with existing project data
  const handleEdit = (project) => {
    setFormData({
      title:      project.title       || '',
      description:project.description || '',
      techStack:  project.techStack   || '',
      projectUrl: project.projectUrl  || '',
      startDate:  project.startDate   || '',
      endDate:    project.endDate     || ''
    });
    setEditProject(project);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editProject) {
        // UPDATE existing project
        await api.put(`/api/student/${userId}/projects/${editProject.id}`, formData);
      } else {
        // CREATE new project
        await api.post(`/api/student/${userId}/projects`, formData);
      }
      setShowForm(false);
      loadProjects(); // Refresh the list
    } catch (err) {
      setError(err.response?.data?.title || 'Failed to save project');
    }
  };

  const handleDelete = async (projectId) => {
    if (!window.confirm('Are you sure you want to delete this project?')) return;
    try {
      await api.delete(`/api/student/${userId}/projects/${projectId}`);
      loadProjects();
    } catch (err) {
      setError('Failed to delete project');
    }
  };

  return (
    <StudentLayout title="My Projects">
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">{error}</div>}

      {/* Add Button */}
      <div className="flex justify-between items-center mb-6">
        <p className="text-gray-500 text-sm">{projects.length} project(s)</p>
        <button onClick={handleAdd} className="btn-primary">+ Add Project</button>
      </div>

      {/* Add/Edit Form */}
      {showForm && (
        <div className="card mb-6">
          <h3 className="text-base font-semibold mb-4">{editProject ? 'Edit Project' : 'Add New Project'}</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Project Title *</label>
              <input name="title" value={formData.title} onChange={handleChange} className="input-field" required placeholder="e.g., Library Management System" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Description</label>
              <textarea name="description" value={formData.description} onChange={handleChange} className="input-field" rows="3" placeholder="What does this project do?" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Tech Stack</label>
              <input name="techStack" value={formData.techStack} onChange={handleChange} className="input-field" placeholder="e.g., Java, Spring Boot, MySQL" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Project URL</label>
              <input name="projectUrl" value={formData.projectUrl} onChange={handleChange} className="input-field" placeholder="https://github.com/..." />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Start Date</label>
                <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">End Date</label>
                <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} className="input-field" />
              </div>
            </div>
            <div className="flex gap-3">
              <button type="submit" className="btn-primary">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {/* Project List */}
      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading projects...</div>
      ) : projects.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">💻</p>
          <p>No projects yet. Add your first project!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {projects.map((project) => (
            <div key={project.id} className="card">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-1">
                    <h4 className="font-semibold text-gray-800">{project.title}</h4>
                    <StatusBadge status={project.verificationStatus} />
                  </div>
                  {project.description && <p className="text-sm text-gray-600 mt-1">{project.description}</p>}
                  {project.techStack   && <p className="text-xs text-blue-600 mt-2 bg-blue-50 inline-block px-2 py-1 rounded">{project.techStack}</p>}
                  {project.projectUrl  && <a href={project.projectUrl} target="_blank" rel="noreferrer" className="block text-xs text-gray-400 mt-1 hover:underline">{project.projectUrl}</a>}
                  {project.teacherComment && (
                    <div className="mt-3 bg-gray-50 rounded-lg p-3">
                      <p className="text-xs font-medium text-gray-500">Teacher's Comment:</p>
                      <p className="text-sm text-gray-700 mt-1">{project.teacherComment}</p>
                    </div>
                  )}
                </div>
                <div className="flex gap-2 ml-4">
                  <button onClick={() => handleEdit(project)} className="text-sm text-blue-600 hover:underline">Edit</button>
                  <button onClick={() => handleDelete(project.id)} className="text-sm text-red-500 hover:underline">Delete</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </StudentLayout>
  );
}

export default StudentProjects;
