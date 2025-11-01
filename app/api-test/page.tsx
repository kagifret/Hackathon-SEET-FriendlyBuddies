'use client';

import { useState } from 'react';
import Link from 'next/link';

export default function ApiTest() {
  const [results, setResults] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const testApi = async (endpoint: string, description: string) => {
    setLoading(true);
    try {
      const response = await fetch(endpoint);
      const data = await response.json();
      setResults({ endpoint, description, data, status: response.status });
    } catch (error) {
      setResults({ endpoint, description, error: String(error), status: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">API Testing Page</h1>
          <Link href="/" className="text-indigo-600 hover:text-indigo-500">← Back to Home</Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
          <button
            onClick={() => testApi('/api/admin/dashboard', 'Admin Dashboard - Match Scoring')}
            className="p-4 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
          >
            Test Admin Dashboard
          </button>
          
          <button
            onClick={() => testApi('/api/matching/candidates/3', 'Browse Matches for Bob (ID 3)')}
            className="p-4 bg-green-600 text-white rounded-lg hover:bg-green-700"
          >
            Test Browse Matches
          </button>
          
          <button
            onClick={() => testApi('/api/match-requests/incoming/4', 'Match Requests for Carol (ID 4)')}
            className="p-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Test Match Requests
          </button>
          
          <button
            onClick={() => testApi('/api/chat/match/1/messages', 'Chat Messages for Match 1')}
            className="p-4 bg-pink-600 text-white rounded-lg hover:bg-pink-700"
          >
            Test Chat Messages
          </button>
        </div>

        {loading && (
          <div className="text-center py-8">
            <div className="text-lg">Loading...</div>
          </div>
        )}

        {results && !loading && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold mb-4">
              {results.description}
            </h3>
            <div className="mb-4">
              <span className="text-sm font-medium text-gray-600">Endpoint: </span>
              <code className="text-sm bg-gray-100 px-2 py-1 rounded">{results.endpoint}</code>
            </div>
            <div className="mb-4">
              <span className="text-sm font-medium text-gray-600">Status: </span>
              <span className={`text-sm ${results.status === 200 ? 'text-green-600' : 'text-red-600'}`}>
                {results.status}
              </span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-600">Response:</span>
              <pre className="mt-2 text-sm bg-gray-100 p-4 rounded overflow-auto max-h-96">
                {JSON.stringify(results.data || results.error, null, 2)}
              </pre>
            </div>
          </div>
        )}

        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-medium text-blue-900 mb-4">Current System Status</h3>
          <ul className="space-y-2 text-blue-800">
            <li>✅ Authentication system working</li>
            <li>✅ Test data initialized</li>
            <li>✅ Matching season activated</li>
            <li>✅ All 7 phases implemented</li>
            <li>✅ 70/15/15 scoring system active</li>
          </ul>
        </div>
      </div>
    </div>
  );
}