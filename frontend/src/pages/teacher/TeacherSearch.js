import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import TeacherLayout from '../../layouts/TeacherLayout';
import api from '../../services/api';

/**
 * TeacherSearch - allows teachers to search for students.
 *
 * WHY THIS PAGE EXISTS:
 * Teachers might have hundreds of students. They need to quickly find
 * a specific student by name, register number, department, year, or section.
 *
 * HOW IT WORKS:
 * 1. Teacher fills in one or more search fields
 * 2. Click Search → GET /api/teacher/{id}/students/search?name=...&department=...
 * 3. Results appear below
 * 4. Click "View Profile" to go to the student's full portfolio
 */
function TeacherSearch() {
  const [filters, setFilters] = useState({ name: '', registerNumber: '', department: '', year: '', section: '' });
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading]   = useState(false);

  const teacherId = localStorage.getItem('userId');

  const handleChange = (e) => setFilters({ ...filters, [e.target.name]: e.target.value });

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSearched(false);

    // Build query parameters, only include non-empty values
    const params = new URLSearchParams();
    if (filters.name)           params.append('name',           filters.name);
    if (filters.registerNumber) params.append('registerNumber', filters.registerNumber);
    if (filters.department)     params.append('department',     filters.department);
    if (filters.year)           params.append('year',           filters.year);
    if (filters.section)        params.append('section',        filters.section);

    try {
      const res = await api.get(`/api/teacher/${teacherId}/students/search?${params.toString()}`);
      setResults(res.data);
      setSearched(true);
    } catch (err) {
      console.error('Search failed:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFilters({ name: '', registerNumber: '', department: '', year: '', section: '' });
    setResults([]);
    setSearched(false);
  };

  return (
    <TeacherLayout title="Search Students">
      {/* Search Form */}
      <div className="card mb-6">
        <h3 className="text-base font-semibold text-gray-700 mb-4">🔍 Find Students</h3>
        <form onSubmit={handleSearch} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Name</label>
              <input name="name" value={filters.name} onChange={handleChange} className="input-field" placeholder="Student name..." />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Register Number</label>
              <input name="registerNumber" value={filters.registerNumber} onChange={handleChange} className="input-field" placeholder="e.g., 21CS001" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Department</label>
              <input name="department" value={filters.department} onChange={handleChange} className="input-field" placeholder="e.g., Computer Science" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Year</label>
              <select name="year" value={filters.year} onChange={handleChange} className="input-field">
                <option value="">Any Year</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Section</label>
              <input name="section" value={filters.section} onChange={handleChange} className="input-field" placeholder="e.g., A" />
            </div>
          </div>
          <div className="flex gap-3">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Searching...' : 'Search'}
            </button>
            <button type="button" onClick={handleReset} className="btn-secondary">Reset</button>
          </div>
        </form>
      </div>

      {/* Search Results */}
      {searched && (
        <div>
          <p className="text-sm text-gray-500 mb-4">{results.length} result(s) found</p>
          {results.length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              <p className="text-4xl mb-4">🔍</p>
              <p>No students match your search criteria.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {results.map(profile => (
                <div key={profile.id} className="card flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold">
                      {profile.user?.name?.charAt(0)}
                    </div>
                    <div>
                      <p className="font-semibold text-gray-800">{profile.user?.name}</p>
                      <p className="text-xs text-gray-500">{profile.user?.email}</p>
                      <div className="flex gap-3 text-xs text-gray-400 mt-0.5">
                        {profile.department     && <span>{profile.department}</span>}
                        {profile.year           && <span>Year {profile.year}</span>}
                        {profile.section        && <span>Sec {profile.section}</span>}
                        {profile.registerNumber && <span>{profile.registerNumber}</span>}
                      </div>
                    </div>
                  </div>
                  <Link to={`/teacher/students/${profile.user?.id}`} className="btn-secondary text-sm">
                    View Profile →
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </TeacherLayout>
  );
}

export default TeacherSearch;
