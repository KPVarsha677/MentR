import React, { useState, useEffect } from 'react';
import StudentLayout from '../../layouts/StudentLayout';
import api from '../../services/api';

/**
 * StudentClassrooms - allows students to join and view their classrooms.
 *
 * WHY THIS PAGE EXISTS:
 * Students need to join their teacher's classroom using a join code.
 * Once joined, they can see all their classrooms here.
 */
function StudentClassrooms() {
  const [classrooms, setClassrooms] = useState([]);
  const [joinCode, setJoinCode]     = useState('');
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');

  const userId = localStorage.getItem('userId');

  useEffect(() => { loadClassrooms(); }, []);

  const loadClassrooms = () => {
    api.get(`/api/classrooms/student/${userId}`)
      .then(res => setClassrooms(res.data))
      .catch(() => setError('Could not load classrooms'))
      .finally(() => setLoading(false));
  };

  const handleJoin = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      await api.post(`/api/classrooms/join?joinCode=${joinCode.toUpperCase()}&studentId=${userId}`);
      setSuccess('Successfully joined the classroom!');
      setJoinCode('');
      loadClassrooms();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to join. Check the code and try again.');
    }
  };

  const handleLeave = async (classroomId) => {
    if (!window.confirm('Are you sure you want to leave this classroom?')) return;
    try {
      await api.delete(`/api/classrooms/${classroomId}/leave?studentId=${userId}`);
      loadClassrooms();
    } catch (err) {
      setError('Failed to leave classroom');
    }
  };

  return (
    <StudentLayout title="My Classrooms">
      {/* Join Classroom Form */}
      <div className="card mb-6">
        <h3 className="text-base font-semibold text-gray-700 mb-4">📚 Join a Classroom</h3>
        <p className="text-sm text-gray-500 mb-4">Enter the join code provided by your teacher to join their classroom.</p>

        {error   && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-3 text-sm">{error}</div>}
        {success && <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-3 text-sm">{success}</div>}

        <form onSubmit={handleJoin} className="flex gap-3">
          <input
            type="text"
            value={joinCode}
            onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
            className="input-field uppercase tracking-widest font-mono text-lg"
            placeholder="Enter code (e.g., ABC12345)"
            maxLength={8}
            required
          />
          <button type="submit" className="btn-primary whitespace-nowrap">Join Classroom</button>
        </form>
      </div>

      {/* Joined Classrooms */}
      <h3 className="text-base font-semibold text-gray-700 mb-4">My Classrooms ({classrooms.length})</h3>

      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading...</div>
      ) : classrooms.length === 0 ? (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">📚</p>
          <p>You haven't joined any classrooms yet.</p>
          <p className="text-sm mt-2">Ask your teacher for the join code.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {classrooms.map((membership) => (
            <div key={membership.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <h4 className="font-semibold text-gray-800">{membership.classroom.name}</h4>
                  {membership.classroom.subject      && <p className="text-sm text-gray-600 mt-1">{membership.classroom.subject}</p>}
                  {membership.classroom.academicYear && <p className="text-xs text-gray-400">{membership.classroom.academicYear}</p>}
                  <p className="text-xs text-gray-400 mt-2">
                    Teacher: {membership.classroom.teacher?.name}
                  </p>
                  <p className="text-xs text-gray-400">
                    Joined: {new Date(membership.joinedAt).toLocaleDateString()}
                  </p>
                </div>
                <button
                  onClick={() => handleLeave(membership.classroom.id)}
                  className="text-xs text-red-400 hover:text-red-600 hover:underline"
                >
                  Leave
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </StudentLayout>
  );
}

export default StudentClassrooms;
