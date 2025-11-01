'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Navigation from '../components/Navigation';

interface MatchData {
  id: number;
  menteeId: number;
  menteeName: string;
  mentorId: number;
  mentorName: string;
  status: string;
  createdAt: string;
  algorithmicScore: number;
  chatEngagementScore: number;
  userFeedbackScore: number;
  finalScore: number;
  recommendation: string;
  menteeRating?: number;
  mentorRating?: number;
  menteeFinalDecision?: boolean;
  mentorFinalDecision?: boolean;
}

export default function AdminDashboard() {
  const [matches, setMatches] = useState<MatchData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<any>(null);
  const router = useRouter();

  useEffect(() => {
    // Check if user is admin
    const userData = localStorage.getItem('user');
    if (userData) {
      const parsedUser = JSON.parse(userData);
      if (parsedUser.role !== 'ADMIN') {
        router.push('/login');
        return;
      }
      setUser(parsedUser);
    } else {
      router.push('/login');
      return;
    }

    // Fetch dashboard data
    fetchDashboardData();
  }, [router]);

  const fetchDashboardData = async () => {
    try {
      const response = await fetch('/api/admin/dashboard');
      if (response.ok) {
        const data = await response.json();
        setMatches(data);
      }
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleApprove = async (matchId: number) => {
    try {
      const response = await fetch(`/api/admin/matches/${matchId}/approve`, {
        method: 'POST'
      });
      
      if (response.ok) {
        fetchDashboardData(); // Refresh data
      }
    } catch (error) {
      console.error('Error approving match:', error);
    }
  };

  const handleReject = async (matchId: number) => {
    try {
      const response = await fetch(`/api/admin/matches/${matchId}/reject`, {
        method: 'POST'
      });
      
      if (response.ok) {
        fetchDashboardData(); // Refresh data
      }
    } catch (error) {
      console.error('Error rejecting match:', error);
    }
  };

  const logout = () => {
    localStorage.removeItem('user');
    router.push('/login');
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getRecommendationBadge = (recommendation: string) => {
    const baseClasses = 'px-2 py-1 rounded-full text-xs font-medium';
    switch (recommendation) {
      case 'APPROVE':
        return `${baseClasses} bg-green-100 text-green-800`;
      case 'REVIEW':
        return `${baseClasses} bg-yellow-100 text-yellow-800`;
      case 'REJECT':
        return `${baseClasses} bg-red-100 text-red-800`;
      default:
        return `${baseClasses} bg-gray-100 text-gray-800`;
    }
  };

  const getFinalDecisionDisplay = (match: MatchData) => {
    const menteeDecision = match.menteeFinalDecision;
    const mentorDecision = match.mentorFinalDecision;
    
    if (menteeDecision == null && mentorDecision == null) {
      return (
        <div className="text-xs text-gray-500 text-center">
          ⏳ Pending
        </div>
      );
    }
    
    const bothWantToContinue = menteeDecision === true && mentorDecision === true;
    const bothWantToEnd = menteeDecision === false && mentorDecision === false;
    const mixedDecisions = (menteeDecision !== null && mentorDecision !== null) && 
                          (menteeDecision !== mentorDecision);
    
    return (
      <div className="text-xs space-y-1">
        <div className="flex items-center justify-between">
          <span className="text-gray-600">M-tee:</span>
          {menteeDecision == null ? (
            <span className="text-gray-400">⏳</span>
          ) : menteeDecision ? (
            <span className="text-green-600 font-medium">✓</span>
          ) : (
            <span className="text-red-600 font-medium">✗</span>
          )}
        </div>
        <div className="flex items-center justify-between">
          <span className="text-gray-600">M-tor:</span>
          {mentorDecision == null ? (
            <span className="text-gray-400">⏳</span>
          ) : mentorDecision ? (
            <span className="text-green-600 font-medium">✓</span>
          ) : (
            <span className="text-red-600 font-medium">✗</span>
          )}
        </div>
        
        {/* Compact Overall Status */}
        {(menteeDecision !== null && mentorDecision !== null) && (
          <div className="text-center mt-1">
            {bothWantToContinue && (
              <span className="text-green-700 font-bold text-xs bg-green-100 px-1 py-0.5 rounded">
                CONTINUE
              </span>
            )}
            {bothWantToEnd && (
              <span className="text-red-700 font-bold text-xs bg-red-100 px-1 py-0.5 rounded">
                END
              </span>
            )}
            {mixedDecisions && (
              <span className="text-yellow-700 font-bold text-xs bg-yellow-100 px-1 py-0.5 rounded">
                MIXED
              </span>
            )}
          </div>
        )}
        
        {/* Ratings if available */}
        {(match.menteeRating || match.mentorRating) && (
          <div className="text-center text-xs text-gray-500 mt-1">
            {match.menteeRating || 0}⭐ / {match.mentorRating || 0}⭐
          </div>
        )}
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center py-4 space-y-2 sm:space-y-0">
            <div>
              <h1 className="text-xl sm:text-2xl font-bold text-gray-900">Admin Dashboard</h1>
              <p className="text-sm sm:text-base text-gray-600">Monitor Real Scoring (70% Algorithm + 15% Chat + 15% Feedback) & Final Match Decisions</p>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-600">Welcome, {user?.firstName}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Dashboard Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Summary Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-8">
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-xl font-bold text-gray-900">{matches.length}</div>
            <div className="text-sm text-gray-600">Total Requests</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-xl font-bold text-green-600">
              {matches.filter(m => m.menteeFinalDecision === true && m.mentorFinalDecision === true).length}
            </div>
            <div className="text-sm text-gray-600">Both Continue</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-xl font-bold text-red-600">
              {matches.filter(m => m.menteeFinalDecision === false && m.mentorFinalDecision === false).length}
            </div>
            <div className="text-sm text-gray-600">Both End</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-xl font-bold text-yellow-600">
              {matches.filter(m => 
                (m.menteeFinalDecision !== null && m.mentorFinalDecision !== null) &&
                (m.menteeFinalDecision !== m.mentorFinalDecision)
              ).length}
            </div>
            <div className="text-sm text-gray-600">Mixed Decisions</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-xl font-bold text-blue-600">
              {matches.filter(m => m.recommendation === 'APPROVE').length}
            </div>
            <div className="text-sm text-gray-600">High Score (&gt;80%)</div>
          </div>
        </div>

        {/* Match Requests Table */}
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <div className="px-4 sm:px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-medium text-gray-900">Match Scoring & Final Decisions (70/15/15 Breakdown)</h2>
          </div>
          <div className="overflow-x-auto">
            <div className="inline-block min-w-full align-middle">
              <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Match Details
                  </th>
                  <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Algorithm<br/><span className="text-gray-400">(70%)</span>
                  </th>
                  <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Chat<br/><span className="text-gray-400">(15%)</span>
                  </th>
                  <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Feedback<br/><span className="text-gray-400">(15%)</span>
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User Decisions
                  </th>
                  <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Final Score
                  </th>
                  <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {matches.map((match) => (
                  <tr key={match.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {match.menteeName} → {match.mentorName}
                        </div>
                        <div className="text-xs text-gray-500">
                          ID: {match.id} | {new Date(match.createdAt).toLocaleDateString()}
                        </div>
                      </div>
                    </td>
                    <td className="px-3 py-3 text-center">
                      <div className={`text-sm font-bold ${getScoreColor(match.algorithmicScore)}`}>
                        {match.algorithmicScore.toFixed(1)}%
                      </div>
                    </td>
                    <td className="px-3 py-3 text-center">
                      <div className={`text-sm font-bold ${getScoreColor(match.chatEngagementScore)}`}>
                        {match.chatEngagementScore.toFixed(1)}%
                      </div>
                    </td>
                    <td className="px-3 py-3 text-center">
                      <div className={`text-sm font-bold ${getScoreColor(match.userFeedbackScore)}`}>
                        {match.userFeedbackScore.toFixed(1)}%
                      </div>
                      <div className="text-xs text-gray-500">
                        {(match.menteeRating || match.mentorRating) ? 
                          `${match.menteeRating || 0}⭐/${match.mentorRating || 0}⭐` : 
                          'No ratings'
                        }
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      {getFinalDecisionDisplay(match)}
                    </td>
                    <td className="px-3 py-3 text-center">
                      <div className={`text-lg font-bold ${getScoreColor(match.finalScore)}`}>
                        {match.finalScore.toFixed(1)}%
                      </div>
                      <div className="text-xs">
                        <span className={getRecommendationBadge(match.recommendation)}>
                          {match.recommendation}
                        </span>
                      </div>
                    </td>
                    <td className="px-3 py-3 text-center">
                      {match.status !== 'ADMIN_APPROVED' && match.status !== 'ADMIN_REJECTED' && (
                        <div className="flex flex-col space-y-1">
                          <button
                            onClick={() => handleApprove(match.id)}
                            className="text-xs text-green-600 hover:text-green-900 bg-green-50 px-2 py-1 rounded"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => handleReject(match.id)}
                            className="text-xs text-red-600 hover:text-red-900 bg-red-50 px-2 py-1 rounded"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                      {match.status === 'ADMIN_APPROVED' && (
                        <span className="text-green-600 font-medium text-xs">✓ Approved</span>
                      )}
                      {match.status === 'ADMIN_REJECTED' && (
                        <span className="text-red-600 font-medium text-xs">✗ Rejected</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {matches.length === 0 && (
              <div className="text-center py-12">
                <div className="text-gray-500">No match requests found</div>
              </div>
            )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}