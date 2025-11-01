'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import AuthWrapper from '../components/AuthWrapper';
import Navigation from '../components/Navigation';
import Link from 'next/link';

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
  status: string;
  menteeRating: number | null;
  mentorRating: number | null;
  menteeFinalDecision: boolean | null;
  mentorFinalDecision: boolean | null;
}

export default function FeedbackPage() {
  const [user, setUser] = useState<User | null>(null);
  const [match, setMatch] = useState<MatchRequest | null>(null);
  const [rating, setRating] = useState(0);
  const [finalDecision, setFinalDecision] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [notification, setNotification] = useState<{
    type: 'success' | 'error' | 'warning';
    message: string;
    show: boolean;
  }>({ type: 'success', message: '', show: false });
  const router = useRouter();
  const searchParams = useSearchParams();
  const matchId = searchParams.get('matchId');

  // Auto-hide notification after 5 seconds
  useEffect(() => {
    if (notification.show) {
      const timer = setTimeout(() => {
        setNotification(prev => ({ ...prev, show: false }));
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [notification.show]);

  const showNotification = (type: 'success' | 'error' | 'warning', message: string) => {
    setNotification({ type, message, show: true });
  };

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      const parsedUser = JSON.parse(userData);
      setUser(parsedUser);
      
      if (matchId) {
        loadMatch(matchId);
      } else {
        router.push('/dashboard');
      }
    } else {
      router.push('/login');
    }
  }, [matchId, router]);

  const loadMatch = async (matchId: string) => {
    setLoading(true);
    try {
      console.log('Loading match with ID:', matchId);
      const response = await fetch(`/api/match-requests/${matchId}`);
      console.log('Response status:', response.status);
      
      if (response.ok) {
        const matchData = await response.json();
        console.log('Match data:', matchData);
        setMatch(matchData);
        
        // Pre-populate existing feedback
        if (user?.id === matchData.mentee.id && matchData.menteeRating) {
          setRating(matchData.menteeRating);
          setFinalDecision(matchData.menteeFinalDecision);
        } else if (user?.id === matchData.mentor.id && matchData.mentorRating) {
          setRating(matchData.mentorRating);
          setFinalDecision(matchData.mentorFinalDecision);
        }
      } else {
        const errorText = await response.text();
        console.error('Failed to load match:', errorText);
        showNotification('error', `Failed to load match: ${errorText}`);
      }
    } catch (error) {
      console.error('Error loading match:', error);
      showNotification('error', 'Failed to load match data');
    } finally {
      setLoading(false);
    }
  };

  const submitFeedback = async () => {
    if (!matchId || rating === 0 || finalDecision === null) {
      showNotification('warning', 'Please provide both rating and final decision');
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch(`/api/match-requests/${matchId}/feedback`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: user?.id,
          rating: rating,
          continueDecision: finalDecision
        })
      });

      if (response.ok) {
        showNotification('success', 'Feedback submitted successfully!');
        setTimeout(() => router.push('/dashboard'), 2000); // Delay redirect to show success message
      } else {
        showNotification('error', 'Failed to submit feedback');
      }
    } catch (error) {
      console.error('Error submitting feedback:', error);
      showNotification('error', 'Failed to submit feedback');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <AuthWrapper>
        <div className="min-h-screen bg-gray-50">
          <Navigation />
          <div className="max-w-2xl mx-auto pt-8 px-4">
            <div className="text-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mx-auto"></div>
              <p className="mt-2 text-gray-600">Loading...</p>
            </div>
          </div>
        </div>
      </AuthWrapper>
    );
  }

  if (!match) {
    return (
      <AuthWrapper>
        <div className="min-h-screen bg-gray-50">
          <Navigation />
          <div className="max-w-2xl mx-auto pt-8 px-4">
            <div className="text-center py-8">
              <p className="text-gray-600">Match not found</p>
              <Link href="/dashboard" className="text-blue-600 hover:text-blue-500 mt-2 inline-block">
                ← Back to Dashboard
              </Link>
            </div>
          </div>
        </div>
      </AuthWrapper>
    );
  }

  const isUserMentee = user?.id === match.mentee.id;
  const partnerName = isUserMentee 
    ? `${match.mentor.firstName} ${match.mentor.lastName}`
    : `${match.mentee.firstName} ${match.mentee.lastName}`;

  return (
    <AuthWrapper>
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        
        {/* Notification Toast */}
        {notification.show && (
          <div className={`fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg max-w-sm w-full transform transition-all duration-300 ${
            notification.type === 'success' ? 'bg-green-50 border-l-4 border-green-400' :
            notification.type === 'error' ? 'bg-red-50 border-l-4 border-red-400' :
            'bg-yellow-50 border-l-4 border-yellow-400'
          }`}>
            <div className="flex items-center">
              <div className="flex-shrink-0">
                {notification.type === 'success' && (
                  <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                )}
                {notification.type === 'error' && (
                  <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                )}
                {notification.type === 'warning' && (
                  <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                )}
              </div>
              <div className="ml-3">
                <p className={`text-sm font-medium ${
                  notification.type === 'success' ? 'text-green-800' :
                  notification.type === 'error' ? 'text-red-800' :
                  'text-yellow-800'
                }`}>
                  {notification.message}
                </p>
              </div>
              <div className="ml-auto pl-3">
                <button
                  onClick={() => setNotification(prev => ({ ...prev, show: false }))}
                  className={`inline-flex rounded-md p-1.5 focus:outline-none focus:ring-2 focus:ring-offset-2 ${
                    notification.type === 'success' ? 'text-green-500 hover:bg-green-100 focus:ring-green-600' :
                    notification.type === 'error' ? 'text-red-500 hover:bg-red-100 focus:ring-red-600' :
                    'text-yellow-500 hover:bg-yellow-100 focus:ring-yellow-600'
                  }`}
                >
                  <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        )}
        
        <div className="max-w-2xl mx-auto pt-8 px-4">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="mb-6">
              <Link href="/dashboard" className="text-blue-600 hover:text-blue-500 text-sm">
                ← Back to Dashboard
              </Link>
            </div>

            <h1 className="text-2xl font-bold mb-6">
              Rate Your Experience with {partnerName}
            </h1>
            
            {/* Star Rating */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-3">
                How would you rate this mentoring match? (1-5 stars)
              </label>
              <div className="flex space-x-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    onClick={() => setRating(star)}
                    className={`text-3xl ${
                      star <= rating ? 'text-yellow-400' : 'text-gray-300'
                    } hover:text-yellow-400 transition-colors`}
                  >
                    ⭐
                  </button>
                ))}
              </div>
              {rating > 0 && (
                <p className="text-sm text-gray-600 mt-2">
                  You selected {rating} star{rating > 1 ? 's' : ''}
                </p>
              )}
            </div>

            {/* Final Decision */}
            <div className="mb-8">
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Would you like to continue this mentoring relationship?
              </label>
              <div className="flex space-x-4">
                <button
                  onClick={() => setFinalDecision(true)}
                  className={`px-6 py-3 rounded-md border transition-colors ${
                    finalDecision === true
                      ? 'bg-green-100 border-green-500 text-green-700'
                      : 'bg-gray-100 border-gray-300 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  ✅ Yes, Continue
                </button>
                <button
                  onClick={() => setFinalDecision(false)}
                  className={`px-6 py-3 rounded-md border transition-colors ${
                    finalDecision === false
                      ? 'bg-red-100 border-red-500 text-red-700'
                      : 'bg-gray-100 border-gray-300 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  ❌ No, Discontinue
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex space-x-4">
              <Link
                href="/dashboard"
                className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 text-center"
              >
                Cancel
              </Link>
              <button
                onClick={submitFeedback}
                disabled={rating === 0 || finalDecision === null || submitting}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
              >
                {submitting ? 'Submitting...' : 'Submit Feedback'}
              </button>
            </div>

          </div>
        </div>
      </div>
    </AuthWrapper>
  );
}
