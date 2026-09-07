import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * TeacherReports - generate and view professional reports.
 *
 * Reports are rendered as clean HTML tables with proper headings,
 * institution name, teacher name, and date generated.
 * A Print button lets the user save as PDF from the browser.
 */
function TeacherReports() {
  const [classrooms, setClassrooms]     = useState([]);
  const [allStudents, setAllStudents]   = useState([]);
  const [selectedReport, setSelectedReport] = useState('');
  const [reportData, setReportData]     = useState(null);
  const [loading, setLoading]           = useState(false);
  const [searchReg, setSearchReg]       = useState('');     // register number search
  const [resolvedStudentId, setResolvedStudentId] = useState(null);
  const [classroomId, setClassroomId]   = useState('');
  const [searchError, setSearchError]   = useState('');

  const teacherId   = localStorage.getItem('userId');
  const teacherName = localStorage.getItem('name') || 'Teacher';
  const today       = new Date().toLocaleDateString('en-IN', { year: 'numeric', month: 'long', day: 'numeric' });
  const location    = useLocation();

  useEffect(() => {
    api.get(`/api/classrooms/teacher/${teacherId}`)
      .then(res => setClassrooms(res.data))
      .catch(() => {});
    // load all students so we can resolve register number → student id
    api.get(`/api/teacher/${teacherId}/students/search`)
      .then(res => setAllStudents(res.data))
      .catch(() => {});
  }, [teacherId]);

  // Arriving from a Dashboard "Pending ..." card: preselect and auto-generate
  // the Pending Verifications report so the click lands on real data.
  useEffect(() => {
    if (location.state?.preset === 'pending') {
      setSelectedReport('pending');
      setLoading(true);
      api.get('/api/reports/pending-verifications')
        .then(res => setReportData({ type: 'pending', data: res.data }))
        .catch(err => setSearchError('Failed to generate report: ' + (err.response?.data?.error || err.message)))
        .finally(() => setLoading(false));
    }
  }, [location.state]);

  // Resolve register number to student ID when user changes the input
  const resolveStudent = () => {
    setSearchError('');
    setResolvedStudentId(null);
    const trimmed = searchReg.trim();
    if (!trimmed) return;

    const match = allStudents.find(s =>
      s.registerNumber && s.registerNumber.toLowerCase() === trimmed.toLowerCase()
    );
    if (match) {
      setResolvedStudentId(match.user?.id);
    } else {
      setSearchError(`No student found with register number "${trimmed}".`);
    }
  };

  const generateReport = async () => {
    setLoading(true);
    setReportData(null);
    setSearchError('');

    // For student-level reports, resolve register number first
    if (['portfolio', 'certifications', 'internships', 'achievements'].includes(selectedReport)) {
      resolveStudent();
      if (!resolvedStudentId) {
        // Re-attempt inline
        const match = allStudents.find(s =>
          s.registerNumber && s.registerNumber.toLowerCase() === searchReg.trim().toLowerCase()
        );
        if (!match) {
          setSearchError(`No student found with register number "${searchReg.trim()}".`);
          setLoading(false);
          return;
        }
        setResolvedStudentId(match.user?.id);
        var sid = match.user?.id;
      } else {
        var sid = resolvedStudentId; // eslint-disable-line no-redeclare
      }
    }

    try {
      let res;
      if (selectedReport === 'portfolio' && sid) {
        res = await api.get(`/api/reports/student/${sid}/portfolio`);
      } else if (selectedReport === 'certifications' && sid) {
        res = await api.get(`/api/reports/student/${sid}/certifications`);
      } else if (selectedReport === 'internships' && sid) {
        res = await api.get(`/api/reports/student/${sid}/internships`);
      } else if (selectedReport === 'achievements' && sid) {
        res = await api.get(`/api/reports/student/${sid}/achievements`);
      } else if (selectedReport === 'classroom' && classroomId) {
        res = await api.get(`/api/reports/classroom/${classroomId}/summary`);
      } else if (selectedReport === 'pending') {
        res = await api.get(`/api/reports/pending-verifications`);
      }
      if (res) setReportData({ type: selectedReport, data: res.data });
    } catch (err) {
      setSearchError('Failed to generate report: ' + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <TeacherLayout title="Reports">
      {/* Report Selection */}
      <div className="card mb-6">
        <h3 className="text-base font-semibold text-gray-700 mb-4">📊 Generate Report</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Report Type</label>
            <select
              value={selectedReport}
              onChange={(e) => { setSelectedReport(e.target.value); setReportData(null); setSearchError(''); }}
              className="input-field"
            >
              <option value="">Select report type...</option>
              <option value="portfolio">Student Portfolio</option>
              <option value="certifications">Student Certifications</option>
              <option value="internships">Student Internships</option>
              <option value="achievements">Student Achievements</option>
              <option value="classroom">Classroom Summary</option>
              <option value="pending">Pending Verifications</option>
            </select>
          </div>

          {/* Register Number input for student-level reports */}
          {['portfolio', 'certifications', 'internships', 'achievements'].includes(selectedReport) && (
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Student Register Number</label>
              <div className="flex gap-2">
                <input
                  value={searchReg}
                  onChange={(e) => { setSearchReg(e.target.value); setResolvedStudentId(null); setSearchError(''); }}
                  onBlur={resolveStudent}
                  className="input-field flex-1"
                  placeholder="e.g., 21CS001"
                />
              </div>
              <p className="text-xs text-gray-400 mt-1">
                Enter the student's unique register number to generate their report.
              </p>
              {resolvedStudentId && (
                <p className="text-xs text-green-600 mt-1">
                  ✓ Student found — ready to generate report.
                </p>
              )}
              {searchError && (
                <p className="text-xs text-red-500 mt-1">{searchError}</p>
              )}
            </div>
          )}

          {/* Classroom dropdown for classroom report */}
          {selectedReport === 'classroom' && (
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Classroom</label>
              <select value={classroomId} onChange={(e) => setClassroomId(e.target.value)} className="input-field">
                <option value="">Select classroom...</option>
                {classrooms.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
          )}

          <div className="flex gap-3">
            <button onClick={generateReport} disabled={loading || !selectedReport} className="btn-primary">
              {loading ? 'Generating...' : 'Generate Report'}
            </button>
            {reportData && (
              <button onClick={() => window.print()} className="btn-secondary">
                🖨️ Print / Save PDF
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Professional Report Output */}
      {reportData && <ReportOutput reportData={reportData} teacherName={teacherName} today={today} />}
    </TeacherLayout>
  );
}

/* ============================================================
   ReportOutput — renders professional formatted report HTML
   ============================================================ */
function ReportOutput({ reportData, teacherName, today }) {
  const { type, data } = reportData;

  const titleMap = {
    portfolio:      'Student Portfolio Report',
    certifications: 'Student Certifications Report',
    internships:    'Student Internships Report',
    achievements:   'Student Achievements Report',
    classroom:      'Classroom Summary Report',
    pending:        'Pending Verifications Report',
  };

  const ReportHeader = ({ title, subTitle }) => (
    <div className="border-b-2 border-slate-800 pb-4 mb-6 print:mb-4">
      <div className="text-center">
        <p className="text-xs text-slate-500 uppercase tracking-widest mb-1">MentorHub Academic Management System</p>
        <h2 className="text-xl font-bold text-slate-800">{title}</h2>
        {subTitle && <p className="text-sm text-slate-500 mt-1">{subTitle}</p>}
      </div>
      <div className="flex justify-between mt-4 text-xs text-slate-500">
        <span><strong>Teacher:</strong> {teacherName}</span>
        <span><strong>Date Generated:</strong> {today}</span>
      </div>
    </div>
  );

  const StatusBadge = ({ status }) => {
    const cls = status === 'APPROVED' ? 'bg-green-100 text-green-700'
              : status === 'REJECTED' ? 'bg-red-100 text-red-700'
              : 'bg-amber-100 text-amber-700';
    return <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${cls}`}>{status || 'PENDING'}</span>;
  };

  // ── Portfolio Report ────────────────────────────────────────
  if (type === 'portfolio') {
    const { profile, projects, skills, certifications, internships, achievements } = data;
    return (
      <div id="report-output" className="card print:shadow-none print:border-none">
        <ReportHeader
          title={titleMap.portfolio}
          subTitle={profile?.user?.name ? `Student: ${profile.user.name}` : undefined}
        />

        {/* Student Info */}
        <section className="mb-6">
          <h3 className="text-sm font-bold text-slate-700 uppercase tracking-wide mb-3 border-b border-slate-200 pb-1">
            Student Information
          </h3>
          <table className="w-full text-sm">
            <tbody>
              <tr className="border-b border-slate-100">
                <td className="py-1.5 pr-4 text-slate-500 font-medium w-40">Name</td>
                <td className="py-1.5 text-slate-800">{profile?.user?.name || '—'}</td>
                <td className="py-1.5 pr-4 text-slate-500 font-medium w-40">Email</td>
                <td className="py-1.5 text-slate-800">{profile?.user?.email || '—'}</td>
              </tr>
              <tr className="border-b border-slate-100">
                <td className="py-1.5 pr-4 text-slate-500 font-medium">Register No.</td>
                <td className="py-1.5 text-slate-800">{profile?.registerNumber || '—'}</td>
                <td className="py-1.5 pr-4 text-slate-500 font-medium">Department</td>
                <td className="py-1.5 text-slate-800">{profile?.department || '—'}</td>
              </tr>
              <tr className="border-b border-slate-100">
                <td className="py-1.5 pr-4 text-slate-500 font-medium">Year</td>
                <td className="py-1.5 text-slate-800">{profile?.year || '—'}</td>
                <td className="py-1.5 pr-4 text-slate-500 font-medium">Section</td>
                <td className="py-1.5 text-slate-800">{profile?.section || '—'}</td>
              </tr>
              <tr>
                <td className="py-1.5 pr-4 text-slate-500 font-medium">Batch</td>
                <td className="py-1.5 text-slate-800">{profile?.batch || '—'}</td>
                <td className="py-1.5 pr-4 text-slate-500 font-medium">Career Goal</td>
                <td className="py-1.5 text-slate-800">{profile?.careerGoal || '—'}</td>
              </tr>
            </tbody>
          </table>
        </section>

        {/* Projects */}
        <SectionTable title="Projects" count={projects?.length}>
          <thead>
            <tr className="bg-slate-50">
              <Th>Title</Th><Th>Tech Stack</Th><Th>Status</Th><Th>Dates</Th>
            </tr>
          </thead>
          <tbody>
            {(projects || []).length === 0
              ? <tr><td colSpan="4" className="text-center py-3 text-slate-400 text-xs">No projects</td></tr>
              : projects.map(p => (
                <tr key={p.id} className="border-b border-slate-100">
                  <Td>{p.title}</Td>
                  <Td>{p.techStack || '—'}</Td>
                  <Td><StatusBadge status={p.verificationStatus} /></Td>
                  <Td>{p.startDate ? `${p.startDate} – ${p.endDate || 'Ongoing'}` : '—'}</Td>
                </tr>
              ))
            }
          </tbody>
        </SectionTable>

        {/* Certifications */}
        <SectionTable title="Certifications" count={certifications?.length}>
          <thead>
            <tr className="bg-slate-50">
              <Th>Name</Th><Th>Issuer</Th><Th>Issue Date</Th><Th>Status</Th>
            </tr>
          </thead>
          <tbody>
            {(certifications || []).length === 0
              ? <tr><td colSpan="4" className="text-center py-3 text-slate-400 text-xs">No certifications</td></tr>
              : certifications.map(c => (
                <tr key={c.id} className="border-b border-slate-100">
                  <Td>{c.name}</Td>
                  <Td>{c.issuingOrganization || '—'}</Td>
                  <Td>{c.issueDate || '—'}</Td>
                  <Td><StatusBadge status={c.verificationStatus} /></Td>
                </tr>
              ))
            }
          </tbody>
        </SectionTable>

        {/* Internships */}
        <SectionTable title="Internships" count={internships?.length}>
          <thead>
            <tr className="bg-slate-50">
              <Th>Company</Th><Th>Role</Th><Th>Duration</Th><Th>Status</Th>
            </tr>
          </thead>
          <tbody>
            {(internships || []).length === 0
              ? <tr><td colSpan="4" className="text-center py-3 text-slate-400 text-xs">No internships</td></tr>
              : internships.map(i => (
                <tr key={i.id} className="border-b border-slate-100">
                  <Td>{i.companyName}</Td>
                  <Td>{i.role || '—'}</Td>
                  <Td>{i.startDate ? `${i.startDate} – ${i.ongoing ? 'Present' : (i.endDate || '—')}` : '—'}</Td>
                  <Td><StatusBadge status={i.verificationStatus} /></Td>
                </tr>
              ))
            }
          </tbody>
        </SectionTable>

        {/* Achievements */}
        <SectionTable title="Achievements" count={achievements?.length}>
          <thead>
            <tr className="bg-slate-50">
              <Th>Title</Th><Th>Category</Th><Th>Issuer</Th><Th>Date</Th><Th>Status</Th>
            </tr>
          </thead>
          <tbody>
            {(achievements || []).length === 0
              ? <tr><td colSpan="5" className="text-center py-3 text-slate-400 text-xs">No achievements</td></tr>
              : achievements.map(a => (
                <tr key={a.id} className="border-b border-slate-100">
                  <Td>{a.title}</Td>
                  <Td>{a.category || '—'}</Td>
                  <Td>{a.issuingOrganization || '—'}</Td>
                  <Td>{a.achievementDate || '—'}</Td>
                  <Td><StatusBadge status={a.verificationStatus} /></Td>
                </tr>
              ))
            }
          </tbody>
        </SectionTable>

        {/* Summary */}
        <section className="mt-6 bg-slate-50 rounded-lg p-4">
          <h3 className="text-sm font-bold text-slate-700 mb-2">Summary</h3>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3 text-center">
            {[
              { label: 'Projects',       count: projects?.length       || 0 },
              { label: 'Certifications', count: certifications?.length || 0 },
              { label: 'Internships',    count: internships?.length    || 0 },
              { label: 'Achievements',   count: achievements?.length   || 0 },
              { label: 'Skills',         count: data.skills?.length    || 0 },
            ].map(s => (
              <div key={s.label} className="bg-white rounded-lg p-3 border border-slate-200">
                <p className="text-2xl font-bold text-slate-800">{s.count}</p>
                <p className="text-xs text-slate-500 mt-0.5">{s.label}</p>
              </div>
            ))}
          </div>
        </section>
      </div>
    );
  }

  // ── Certifications / Internships / Achievements ─────────────
  if (['certifications', 'internships', 'achievements'].includes(type)) {
    const items = data;
    const profileItem = items[0];
    const studentName = profileItem?.student?.name || 'Unknown Student';

    const columns = {
      certifications: [
        ['Name', i => i.name],
        ['Issuing Organisation', i => i.issuingOrganization || '—'],
        ['Issue Date', i => i.issueDate || '—'],
        ['Expiry Date', i => i.expirationDate || '—'],
        ['Credential ID', i => i.credentialId || '—'],
        ['Status', i => i.verificationStatus],
      ],
      internships: [
        ['Company', i => i.companyName],
        ['Role', i => i.role || '—'],
        ['Location', i => i.location || '—'],
        ['Start Date', i => i.startDate || '—'],
        ['End Date', i => i.ongoing ? 'Present' : (i.endDate || '—')],
        ['Status', i => i.verificationStatus],
      ],
      achievements: [
        ['Title', i => i.title],
        ['Category', i => i.category || '—'],
        ['Issuer', i => i.issuingOrganization || '—'],
        ['Date', i => i.achievementDate || '—'],
        ['Status', i => i.verificationStatus],
      ],
    };

    const cols = columns[type];

    return (
      <div id="report-output" className="card print:shadow-none print:border-none">
        <ReportHeader title={titleMap[type]} subTitle={`Student: ${studentName}`} />
        {items.length === 0 ? (
          <p className="text-center text-slate-400 py-8">No records found.</p>
        ) : (
          <table className="w-full text-sm border border-slate-200 rounded-lg overflow-hidden">
            <thead>
              <tr className="bg-slate-700 text-white">
                <Th dark>S.No</Th>
                {cols.map(([label]) => <Th dark key={label}>{label}</Th>)}
              </tr>
            </thead>
            <tbody>
              {items.map((item, idx) => (
                <tr key={item.id} className={idx % 2 === 0 ? '' : 'bg-slate-50'}>
                  <Td>{idx + 1}</Td>
                  {cols.map(([label, fn]) => (
                    <Td key={label}>
                      {label === 'Status'
                        ? <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                            fn(item) === 'APPROVED' ? 'bg-green-100 text-green-700' :
                            fn(item) === 'REJECTED' ? 'bg-red-100 text-red-700' :
                            'bg-amber-100 text-amber-700'
                          }`}>{fn(item) || 'PENDING'}</span>
                        : fn(item)
                      }
                    </Td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <p className="text-xs text-slate-400 mt-4 text-right">Total: {items.length} record(s)</p>
      </div>
    );
  }

  // ── Classroom Summary ───────────────────────────────────────
  if (type === 'classroom') {
    const { classroom, subject, academicYear, totalStudents, students } = data;
    return (
      <div id="report-output" className="card print:shadow-none print:border-none">
        <ReportHeader
          title={titleMap.classroom}
          subTitle={`${classroom} — ${subject || ''} ${academicYear ? `(${academicYear})` : ''}`}
        />
        <div className="grid grid-cols-3 gap-4 mb-6">
          <InfoBox label="Classroom" value={classroom} />
          <InfoBox label="Subject" value={subject || '—'} />
          <InfoBox label="Academic Year" value={academicYear || '—'} />
        </div>
        <table className="w-full text-sm border border-slate-200 rounded-lg overflow-hidden mb-4">
          <thead>
            <tr className="bg-slate-700 text-white">
              <Th dark>S.No</Th>
              <Th dark>Register No.</Th>
              <Th dark>Student Name</Th>
              <Th dark>Email</Th>
              <Th dark>Projects</Th>
              <Th dark>Certifications</Th>
              <Th dark>Internships</Th>
              <Th dark>Achievements</Th>
              <Th dark>Total Items</Th>
            </tr>
          </thead>
          <tbody>
            {(students || []).map((s, idx) => {
              const total = (s.projects || 0) + (s.certifications || 0) + (s.internships || 0) + (s.achievements || 0);
              return (
                <tr key={s.studentId} className={idx % 2 === 0 ? '' : 'bg-slate-50'}>
                  <Td>{idx + 1}</Td>
                  <Td>{s.registerNumber || '—'}</Td>
                  <Td bold>{s.studentName}</Td>
                  <Td>{s.email}</Td>
                  <Td center>{s.projects}</Td>
                  <Td center>{s.certifications}</Td>
                  <Td center>{s.internships}</Td>
                  <Td center>{s.achievements}</Td>
                  <Td center bold>{total}</Td>
                </tr>
              );
            })}
          </tbody>
          <tfoot>
            <tr className="bg-slate-100 font-semibold">
              <td colSpan="4" className="px-4 py-2 text-sm text-slate-700">Total ({totalStudents} students)</td>
              <Td center bold>{students?.reduce((acc, s) => acc + (s.projects || 0), 0)}</Td>
              <Td center bold>{students?.reduce((acc, s) => acc + (s.certifications || 0), 0)}</Td>
              <Td center bold>{students?.reduce((acc, s) => acc + (s.internships || 0), 0)}</Td>
              <Td center bold>{students?.reduce((acc, s) => acc + (s.achievements || 0), 0)}</Td>
              <Td center bold>
                {students?.reduce((acc, s) => acc + (s.projects || 0) + (s.certifications || 0) + (s.internships || 0) + (s.achievements || 0), 0)}
              </Td>
            </tr>
          </tfoot>
        </table>
        <p className="text-xs text-slate-400 text-right">Total Students: {totalStudents}</p>
      </div>
    );
  }

  // ── Pending Verifications ───────────────────────────────────
  if (type === 'pending') {
    const { projects, certifications, internships, achievements } = data;
    const total = (projects?.length || 0) + (certifications?.length || 0) +
                  (internships?.length || 0) + (achievements?.length || 0);
    return (
      <div id="report-output" className="card print:shadow-none print:border-none">
        <ReportHeader title={titleMap.pending} />
        <div className="grid grid-cols-4 gap-4 mb-6">
          <InfoBox label="Pending Projects"       value={projects?.length       || 0} />
          <InfoBox label="Pending Certifications" value={certifications?.length || 0} />
          <InfoBox label="Pending Internships"    value={internships?.length    || 0} />
          <InfoBox label="Pending Achievements"   value={achievements?.length   || 0} />
        </div>

        {[
          { title: 'Pending Projects',       items: projects,       cols: [['Title', i => i.title], ['Tech Stack', i => i.techStack || '—'], ['Student', i => i.student?.name || '—']] },
          { title: 'Pending Certifications', items: certifications, cols: [['Name', i => i.name], ['Issuer', i => i.issuingOrganization || '—'], ['Student', i => i.student?.name || '—']] },
          { title: 'Pending Internships',    items: internships,    cols: [['Company', i => i.companyName], ['Role', i => i.role || '—'], ['Student', i => i.student?.name || '—']] },
          { title: 'Pending Achievements',   items: achievements,   cols: [['Title', i => i.title], ['Category', i => i.category || '—'], ['Student', i => i.student?.name || '—']] },
        ].map(({ title, items, cols }) => items?.length > 0 && (
          <SectionTable key={title} title={title} count={items.length}>
            <thead>
              <tr className="bg-slate-50">
                <Th>S.No</Th>
                {cols.map(([label]) => <Th key={label}>{label}</Th>)}
              </tr>
            </thead>
            <tbody>
              {items.map((item, idx) => (
                <tr key={item.id} className="border-b border-slate-100">
                  <Td>{idx + 1}</Td>
                  {cols.map(([label, fn]) => <Td key={label}>{fn(item)}</Td>)}
                </tr>
              ))}
            </tbody>
          </SectionTable>
        ))}
        <p className="text-xs text-slate-400 mt-4 text-right font-semibold">Total Pending Items: {total}</p>
      </div>
    );
  }

  return null;
}

// ─── Table helper components ─────────────────────────────────
function SectionTable({ title, count, children }) {
  return (
    <section className="mb-6">
      <h3 className="text-sm font-bold text-slate-700 uppercase tracking-wide mb-2 flex items-center gap-2">
        {title}
        {count !== undefined && (
          <span className="text-xs bg-slate-200 text-slate-600 px-2 py-0.5 rounded-full font-semibold normal-case">{count}</span>
        )}
      </h3>
      <table className="w-full text-sm border border-slate-200 rounded-lg overflow-hidden">
        {children}
      </table>
    </section>
  );
}

function Th({ children, dark }) {
  return (
    <th className={`text-left px-3 py-2.5 text-xs font-semibold uppercase tracking-wide ${dark ? 'text-white' : 'text-slate-500'}`}>
      {children}
    </th>
  );
}

function Td({ children, center, bold }) {
  return (
    <td className={`px-3 py-2 text-sm ${center ? 'text-center' : ''} ${bold ? 'font-semibold' : ''} text-slate-700`}>
      {children}
    </td>
  );
}

function InfoBox({ label, value }) {
  return (
    <div className="bg-slate-50 border border-slate-200 rounded-lg p-3 text-center">
      <p className="text-xs text-slate-500 font-medium mb-1">{label}</p>
      <p className="text-lg font-bold text-slate-800">{value}</p>
    </div>
  );
}

export default TeacherReports;
