'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Navigation from '../components/Navigation';
import AuthWrapper from '../components/AuthWrapper';

interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

interface MatchRequest {
  id: number;
  mentee: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };
  mentor: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };
  status: 'MENTEE_INTERESTED' | 'MENTOR_ACCEPTED' | 'MENTOR_REJECTED' | 'ADMIN_APPROVED' | 'ADMIN_REJECTED';
  menteeFinalDecision: boolean | null;
  mentorFinalDecision: boolean | null;
  adminApproved: boolean | null;
  adminNotes: string | null;
  createdAt: string;
  updatedAt: string;
}

export default function Dashboard() {
  const [user, setUser] = useState<User | null>(null);
  const [matches, setMatches] = useState<MatchRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      const parsedUser = JSON.parse(userData);
      console.log('Dashboard - Parsed user:', parsedUser);
      setUser(parsedUser);
      // Load user's matches
      loadMatches(parsedUser.id);
    } else {
      router.push('/login');
    }
  }, [router]);

  const loadMatches = async (userId: number) => {
    setLoading(true);
    try {
      const response = await fetch(`/api/match-requests?userId=${userId}`);
      if (response.ok) {
        const matchesData = await response.json();
        // Filter for accepted matches only (where actual mentoring happens)
        const acceptedMatches = matchesData.filter((match: MatchRequest) => 
          match.status === 'MENTOR_ACCEPTED' || match.status === 'ADMIN_APPROVED'
        );
        setMatches(acceptedMatches);
      }
    } catch (error) {
      console.error('Error loading matches:', error);
    } finally {
      setLoading(false);
    }
  };

  const getMatchPartnerName = (match: MatchRequest) => {
    const isCurrentUserMentee = match.mentee.id === user?.id;
    return isCurrentUserMentee 
      ? `${match.mentor.firstName} ${match.mentor.lastName}`
      : `${match.mentee.firstName} ${match.mentee.lastName}`;
  };

  const getMatchPartnerRole = (match: MatchRequest) => {
    const isCurrentUserMentee = match.mentee.id === user?.id;
    return isCurrentUserMentee ? 'Mentor' : 'Mentee';
  };

  const logout = () => {
    localStorage.removeItem('user');
    router.push('/');
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  const getRoleDescription = () => {
    switch (user?.role) {
      case 'ADMIN':
        return 'Manage the platform, review matches, and oversee the mentoring program';
      case 'MENTEE':
        return 'Find mentors, engage in conversations, and grow your skills';
      case 'MENTOR':
        return 'Guide mentees, share your expertise, and make a difference';
      default:
        return 'Welcome to the mentoring platform';
    }
  };

  const getRoleActions = () => {
    console.log('Getting role actions for user:', user);
    switch (user?.role) {
      case 'ADMIN':
        return [
          { title: 'Admin Dashboard', description: 'Monitor final decisions from users', href: '/admin', color: 'bg-purple-600 hover:bg-purple-700' },
          { title: 'Admin Settings', description: 'Configure platform settings', href: '/admin/settings', color: 'bg-indigo-600 hover:bg-indigo-700' }
        ];
      case 'MENTEE':
        return [
          { title: 'Complete Profile', description: 'Set up your mentee profile', href: '/profile', color: 'bg-blue-600 hover:bg-blue-700' },
          { title: 'Browse Matches', description: 'Find potential mentors', href: '/browse-matches', color: 'bg-green-600 hover:bg-green-700' },
          { title: 'Chat', description: 'Message with your mentors', href: '/chat', color: 'bg-pink-600 hover:bg-pink-700' }
        ];
      case 'MENTOR':
        return [
          { title: 'Complete Profile', description: 'Set up your mentor profile', href: '/profile', color: 'bg-blue-600 hover:bg-blue-700' },
          { title: 'Review Requests', description: 'Accept or decline mentee requests', href: '/match-requests', color: 'bg-orange-600 hover:bg-orange-700' },
          { title: 'Chat', description: 'Message with your mentees', href: '/chat', color: 'bg-pink-600 hover:bg-pink-700' }
        ];
      default:
        console.log('Unknown role or no user:', user?.role);
        return [];
    }
  };

  return (
    <AuthWrapper>
    <div className={`min-h-screen ${
      user?.role === 'ADMIN' ? 'bg-purple-50' : 
      user?.role === 'MENTOR' ? 'bg-green-50' : 
      'bg-blue-50'
    }`}>
      <Navigation />
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Welcome, {user?.firstName}!
              </h1>
              <p className="text-sm text-gray-600 capitalize">
                {user?.role?.toLowerCase()} Dashboard ({user?.role})
              </p>
            </div>

          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Role Description */}
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <h2 className="text-lg font-medium text-gray-900 mb-2">
            Your Role: <span className="font-bold text-indigo-600">{user?.role}</span>
          </h2>
          <p className="text-gray-600">{getRoleDescription()}</p>
          <div className="mt-2 text-sm text-gray-500">
            User ID: {user?.id} | Email: {user?.email}
          </div>
        </div>

        {/* Matches Section - Only show for MENTEE and MENTOR roles */}
        {user?.role !== 'ADMIN' && (
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Your Matches</h2>
          
          {loading ? (
              <div className="text-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mx-auto"></div>
                <p className="mt-2 text-gray-600">Loading matches...</p>
              </div>
            ) : matches.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-600">No active matches yet</p>
                <p className="text-sm text-gray-500 mt-2">
                  {user?.role === 'MENTEE' 
                    ? 'Browse mentors and send requests to get started!'
                    : 'Review incoming requests from mentees!'
                  }
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {matches.map((match) => (
                  <div key={match.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-4">
                          <div>
                            <h3 className="text-lg font-medium text-gray-900">
                              {getMatchPartnerName(match)}
                            </h3>
                            <p className="text-sm text-gray-600">
                              {getMatchPartnerRole(match)} • Match ID: {match.id}
                            </p>
                            <p className="text-xs text-gray-500 mt-1">
                              Status: {match.status.replace('_', ' ')}
                            </p>
                          </div>
                        </div>

                      </div>

                      {/* Action Buttons */}
                      <div className="flex space-x-2 ml-4">
                        <Link
                          href={`/chat?matchId=${match.id}`}
                          prefetch={false}
                          className="px-3 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors"
                        >
                          Chat
                        </Link>
                        
                        {/* Only show feedback button for non-admin users */}
                        {user?.role !== 'ADMIN' && (
                          <Link
                            href={`/feedback?matchId=${match.id}`}
                            className="px-3 py-2 bg-green-600 text-white text-sm rounded-md hover:bg-green-700"
                          >
                            Feedback
                          </Link>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {getRoleActions().map((action, index) => (
            <Link
              key={index}
              href={action.href}
              className={`${action.color} text-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow`}
            >
              <h3 className="text-lg font-semibold mb-2">{action.title}</h3>
              <p className="text-white/90">{action.description}</p>
            </Link>
          ))}
        </div>

        {/* Getting Started Guide */}
        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-medium text-blue-900 mb-4">
            🚀 Getting Started ({user?.role})
          </h3>
          
          {user?.role === 'MENTEE' && (
            <ol className="list-decimal list-inside space-y-2 text-blue-800">
              <li>Complete your profile with your goals and background</li>
              <li>Browse available mentors and request matches</li>
              <li>Wait for mentor approval of your request</li>
              <li>Start chatting once a match is accepted</li>
              <li>Provide feedback after your mentoring sessions</li>
            </ol>
          )}
          
          {user?.role === 'MENTOR' && (
            <ol className="list-decimal list-inside space-y-2 text-blue-800">
              <li>Complete your profile with your expertise and philosophy</li>
              <li>Review incoming match requests from mentees</li>
              <li>Accept requests that align with your skills</li>
              <li>Chat with your mentees and provide guidance</li>
              <li>Rate your mentoring experience</li>
            </ol>
          )}
          
          {user?.role === 'ADMIN' && (
            <ol className="list-decimal list-inside space-y-2 text-blue-800">
              <li>Monitor mentor and mentee final decisions in the dashboard</li>
              <li>Review match requests and their status</li>
              <li>Make final approval/rejection decisions based on user feedback</li>
              <li>Configure platform settings as needed</li>
              <li>Track platform metrics and user engagement</li>
            </ol>
          )}
        </div>
      </div>
    </div>
    </AuthWrapper>
  );
}