import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * StatusBadge - shows verification status with color coding.
 */
function StatusBadge({ status }) {
  const classes = { PENDING: 'badge-pending', APPROVED: 'badge-approved', REJECTED: 'badge-rejected' };
  return <span className={classes[status] || 'badge-pending'}>{status}</span>;
}

/**
 * VerifyButton - allows teacher to approve or reject an item with a comment.
 */
function VerifyButton({ endpoint, itemId, onVerified }) {
  const [showPanel, setShowPanel] = useState(false);
  const [status, setStatus]       = useState('APPROVED');
  const [comment, setComment]     = useState('');
  const teacherId = localStorage.getItem('userId');

  const handleVerify = async () => {
    await api.post(`${endpoint}/${itemId}?teacherId=${teacherId}`, { status, comment });
    setShowPanel(false);
    onVerified(); // Refresh data
  };

  return (
    <div className="inline-block">
      <button onClick={() => setShowPanel(!showPanel)} className="text-xs bg-indigo-50 hover:bg-indigo-100 text-indigo-600 px-2 py-1 rounded transition-colors">
        Verify
      </button>
      {showPanel && (
        <div className="absolute z-10 bg-white border border-gray-200 rounded-lg shadow-lg p-4 mt-1 w-64">
          <p className="text-sm font-medium mb-2">Verification Decision</p>
          <select value={status} onChange={(e) => setStatus(e.target.value)} className="input-field text-sm mb-2">
            <option value="APPROVED">Approve</option>
            <option value="REJECTED">Reject</option>
          </select>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            className="input-field text-sm mb-2"
            rows="2"
            placeholder="Optional comment..."
          />
          <div className="flex gap-2">
            <button onClick={handleVerify} className="btn-primary text-xs py-1 px-3">Submit</button>
            <button onClick={() => setShowPanel(false)} className="btn-secondary text-xs py-1 px-3">Cancel</button>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * TeacherStudentView - shows a complete student portfolio to the teacher.
 *
 * WHY THIS PAGE EXISTS:
 * Teachers need to see all the information a student has added —
 * their profile, projects, certifications, etc. —
 * and verify each item as approved or rejected.
 *
 * The :studentId comes from the URL parameter.
 * e.g., /teacher/students/5 → studentId = 5
 */
function TeacherStudentView() {
  const { studentId } = useParams();
  const [activeTab, setActiveTab] = useState('profile');
  const [profile, setProfile]     = useState(null);
  const [projects, setProjects]   = useState([]);
  const [certs, setCerts]         = useState([]);
  const [internships, setInternships] = useState([]);
  const [achievements, setAchievements] = useState([]);
  const [skills, setSkills]       = useState([]);
  const [loading, setLoading]     = useState(true);

  const loadAll = async () => {
    try {
      const [profileRes, projectRes, certRes, intRes, achRes, skillRes] = await Promise.all([
        api.get(`/api/teacher/students/${studentId}/profile`),
        api.get(`/api/teacher/students/${studentId}/projects`),
        api.get(`/api/teacher/students/${studentId}/certifications`),
        api.get(`/api/teacher/students/${studentId}/internships`),
        api.get(`/api/teacher/students/${studentId}/achievements`),
        api.get(`/api/teacher/students/${studentId}/skills`)
      ]);
      setProfile(profileRes.data);
      setProjects(projectRes.data);
      setCerts(certRes.data);
      setInternships(intRes.data);
      setAchievements(achRes.data);
      setSkills(skillRes.data);
    } catch (err) {
      console.error('Failed to load student data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); }, [studentId]);

  const tabs = ['profile', 'projects', 'skills', 'certifications', 'internships', 'achievements'];

  if (loading) return <TeacherLayout title="Student Profile"><div className="text-center py-12 text-gray-400">Loading student data...</div></TeacherLayout>;
  if (!profile) return <TeacherLayout title="Student Profile"><div className="text-center py-12 text-gray-400">Student not found.</div></TeacherLayout>;

  return (
    <TeacherLayout title={`Student: ${profile.user?.name}`}>
      {/* Student Header */}
      <div className="card mb-6">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-xl">
            {profile.user?.name?.charAt(0)}
          </div>
          <div>
            <h3 className="text-xl font-bold text-gray-800">{profile.user?.name}</h3>
            <p className="text-sm text-gray-500">{profile.user?.email}</p>
            <div className="flex gap-4 text-xs text-gray-400 mt-1">
              {profile.department     && <span>{profile.department}</span>}
              {profile.year           && <span>Year {profile.year}</span>}
              {profile.section        && <span>Section {profile.section}</span>}
              {profile.registerNumber && <span>Reg: {profile.registerNumber}</span>}
            </div>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex gap-2 mb-6 border-b border-gray-200 overflow-x-auto">
        {tabs.map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 text-sm font-medium capitalize whitespace-nowrap border-b-2 transition-colors ${
              activeTab === tab ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* TAB: Profile */}
      {activeTab === 'profile' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[['Register No', profile.registerNumber], ['Department', profile.department], ['Year', profile.year], ['Section', profile.section], ['Batch', profile.batch], ['Phone', profile.phone], ['Address', profile.address], ['LinkedIn', profile.linkedinUrl], ['GitHub', profile.githubUrl], ['Career Goal', profile.careerGoal]].map(([label, value]) => (
            value && (
              <div key={label} className="card">
                <p className="text-xs font-medium text-gray-400 mb-1">{label}</p>
                <p className="text-sm text-gray-800">{value}</p>
              </div>
            )
          ))}
          {profile.about && (
            <div className="card md:col-span-2">
              <p className="text-xs font-medium text-gray-400 mb-1">About</p>
              <p className="text-sm text-gray-800">{profile.about}</p>
            </div>
          )}
        </div>
      )}

      {/* TAB: Projects */}
      {activeTab === 'projects' && (
        <div className="space-y-4">
          {projects.length === 0 ? <p className="text-gray-400 text-center py-8">No projects added.</p> :
            projects.map(p => (
              <div key={p.id} className="card relative">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-3 mb-1">
                      <h4 className="font-semibold text-gray-800">{p.title}</h4>
                      <StatusBadge status={p.verificationStatus} />
                    </div>
                    {p.description  && <p className="text-sm text-gray-600 mt-1">{p.description}</p>}
                    {p.techStack    && <p className="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded mt-2 inline-block">{p.techStack}</p>}
                    {p.teacherComment && <p className="text-xs text-gray-500 mt-2 italic bg-gray-50 p-2 rounded">Your comment: {p.teacherComment}</p>}
                  </div>
                  <VerifyButton endpoint="/api/teacher/verify/project" itemId={p.id} onVerified={loadAll} />
                </div>
              </div>
            ))
          }
        </div>
      )}

      {/* TAB: Skills */}
      {activeTab === 'skills' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {skills.length === 0 ? <p className="text-gray-400 col-span-3 text-center py-8">No skills added.</p> :
            skills.map(s => (
              <div key={s.id} className="card relative">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <h4 className="font-semibold text-gray-800">{s.name}</h4>
                      <StatusBadge status={s.verificationStatus} />
                    </div>
                    {s.category         && <p className="text-xs text-gray-500">{s.category}</p>}
                    {s.proficiencyLevel && <p className="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded mt-1 inline-block">{s.proficiencyLevel}</p>}
                  </div>
                  <VerifyButton endpoint="/api/teacher/verify/skill" itemId={s.id} onVerified={loadAll} />
                </div>
              </div>
            ))
          }
        </div>
      )}

      {/* TAB: Certifications */}
      {activeTab === 'certifications' && (
        <div className="space-y-4">
          {certs.length === 0 ? <p className="text-gray-400 text-center py-8">No certifications added.</p> :
            certs.map(c => (
              <div key={c.id} className="card relative">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-3 mb-1">
                      <h4 className="font-semibold text-gray-800">{c.name}</h4>
                      <StatusBadge status={c.verificationStatus} />
                    </div>
                    {c.issuingOrganization && <p className="text-sm text-gray-600">{c.issuingOrganization}</p>}
                    {c.issueDate           && <p className="text-xs text-gray-400">Issued: {c.issueDate}</p>}
                    {c.credentialUrl       && <a href={c.credentialUrl} target="_blank" rel="noreferrer" className="text-xs text-blue-500 hover:underline">View Certificate</a>}
                  </div>
                  <VerifyButton endpoint="/api/teacher/verify/certification" itemId={c.id} onVerified={loadAll} />
                </div>
              </div>
            ))
          }
        </div>
      )}

      {/* TAB: Internships */}
      {activeTab === 'internships' && (
        <div className="space-y-4">
          {internships.length === 0 ? <p className="text-gray-400 text-center py-8">No internships added.</p> :
            internships.map(i => (
              <div key={i.id} className="card relative">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-3 mb-1">
                      <h4 className="font-semibold text-gray-800">{i.companyName}</h4>
                      <StatusBadge status={i.verificationStatus} />
                    </div>
                    {i.role     && <p className="text-sm text-gray-600">{i.role}</p>}
                    {i.location && <p className="text-xs text-gray-400">{i.location}</p>}
                    {i.startDate && <p className="text-xs text-gray-400">{i.startDate} → {i.ongoing ? 'Present' : i.endDate}</p>}
                  </div>
                  <VerifyButton endpoint="/api/teacher/verify/internship" itemId={i.id} onVerified={loadAll} />
                </div>
              </div>
            ))
          }
        </div>
      )}

      {/* TAB: Achievements */}
      {activeTab === 'achievements' && (
        <div className="space-y-4">
          {achievements.length === 0 ? <p className="text-gray-400 text-center py-8">No achievements added.</p> :
            achievements.map(a => (
              <div key={a.id} className="card relative">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-3 mb-1">
                      <h4 className="font-semibold text-gray-800">{a.title}</h4>
                      <StatusBadge status={a.verificationStatus} />
                    </div>
                    {a.category            && <span className="text-xs bg-orange-50 text-orange-600 px-2 py-0.5 rounded-full">{a.category}</span>}
                    {a.issuingOrganization && <p className="text-sm text-gray-600 mt-1">{a.issuingOrganization}</p>}
                    {a.achievementDate     && <p className="text-xs text-gray-400">{a.achievementDate}</p>}
                  </div>
                  <VerifyButton endpoint="/api/teacher/verify/achievement" itemId={a.id} onVerified={loadAll} />
                </div>
              </div>
            ))
          }
        </div>
      )}
    </TeacherLayout>
  );
}

export default TeacherStudentView;
