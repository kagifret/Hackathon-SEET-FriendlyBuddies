'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import Navigation from '../components/Navigation'
import { API_BASE_URL } from '../config/api'

interface MatchRequestInfo {
  requestId: number
  menteeId: number
  academicField: string
  mentorshipGoals: string
  createdAt: string
}

export default function MatchRequests() {
  const [requests, setRequests] = useState<MatchRequestInfo[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [userId, setUserId] = useState<string>('')
  const [userName, setUserName] = useState<string>('')
  const [userRole, setUserRole] = useState<string>('')
  const [responding, setResponding] = useState<number | null>(null)
  const router = useRouter()

  useEffect(() => {
    const userData = localStorage.getItem('user')
    if (!userData) {
      router.push('/login')
      return
    }
    
    try {
      const user = JSON.parse(userData)
      setUserId(user.id.toString())
      setUserName(user.firstName + ' ' + user.lastName)
      setUserRole(user.role)
      
      if (user.role === 'MENTOR') {
        fetchRequests(user.id.toString(), user.role)
      } else if (user.role === 'MENTEE') {
        // Redirect mentees to browse matches instead
        router.push('/browse-matches')
        return
      } else {
        router.push('/dashboard')
      }
    } catch (error) {
      console.error('Error parsing user data:', error)
      router.push('/login')
      return
    }
  }, [router])

  const fetchRequests = async (userId: string, role: string = userRole) => {
    try {
      let response;
      if (role === 'MENTOR') {
        response = await fetch(`${API_BASE_URL}/api/match-requests/incoming/${userId}`)
      } else {
        response = await fetch(`${API_BASE_URL}/api/match-requests/user/${userId}`)
      }
      
      if (response.ok) {
        const data = await response.json()
        console.log('Loaded requests data:', data);
        setRequests(data)
      } else {
        const errorMsg = await response.text()
        setError(errorMsg)
      }
    } catch (error) {
      setError(`Failed to load ${userRole === 'MENTOR' ? 'incoming' : 'your'} requests`)
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  const respondToRequest = async (requestId: number, accept: boolean) => {
    console.log('respondToRequest called with:', { requestId, accept });
    
    if (!requestId) {
      console.error('Request ID is undefined or null:', requestId);
      setError('Invalid request ID');
      return;
    }
    
    setResponding(requestId)

    try {
      const response = await fetch(`${API_BASE_URL}/api/match-requests/${requestId}/respond`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ accept }),
      })

      if (response.ok) {
        // Remove the request from the list
        setRequests(prev => prev.filter(r => r.requestId !== requestId))
        
        if (accept) {
          // Redirect to chat if accepted with the correct match ID
          const matchId = userRole === 'MENTOR' ? requestId : requestId
          setTimeout(() => {
            router.push(`/chat?matchId=${matchId}`)
          }, 1000)
        }
      } else {
        const errorMsg = await response.text()
        setError(errorMsg)
      }
    } catch (error) {
      setError('Failed to respond to request')
      console.error('Error:', error)
    } finally {
      setResponding(null)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600 mx-auto mb-4"></div>
          <div className="text-xl font-semibold text-gray-700">Loading match requests...</div>
          <div className="text-sm text-gray-500 mt-2">Please wait while we fetch your data</div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      <Navigation />
      
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
              Incoming Match Requests 📋
            </h1>
            <p className="text-xl text-blue-100 max-w-3xl mx-auto leading-relaxed">
              Mentees who are interested in being mentored by you. Personal details are hidden until you accept.
            </p>
            <div className="mt-6 flex justify-center">
              <div className="bg-white/20 backdrop-blur-sm rounded-full px-6 py-3">
                <span className="text-lg font-semibold">🌟 Shape the Future Together</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {error && (
          <div className="mb-8 p-6 rounded-xl bg-red-50 border border-red-200 shadow-sm">
            <div className="flex items-center">
              <div className="text-red-500 text-2xl mr-3">⚠️</div>
              <div className="text-red-700 font-medium">{error}</div>
            </div>
          </div>
        )}

        {requests.length === 0 ? (
          <div className="text-center py-16">
            <div className="bg-white shadow-xl rounded-2xl p-12 max-w-2xl mx-auto border border-gray-100">
              <div className="text-6xl mb-6">😴</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-4">
                No Pending Requests
              </h3>
              <p className="text-gray-600 mb-8 text-lg leading-relaxed">
                You don't have any mentees requesting to match with you at the moment. 
                Keep your profile updated to attract more potential mentees!
              </p>
              <Link
                href="/profile"
                className="inline-flex items-center px-8 py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold rounded-xl hover:from-blue-700 hover:to-purple-700 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-1"
              >
                <span className="mr-2">✨</span>
                Update Profile
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-8">
            {requests.map((request) => (
              <div key={request.requestId} className="bg-white shadow-xl rounded-2xl p-8 border border-gray-100 hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1">
                <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-6">
                  <div className="flex-1">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
                      <div className="flex items-center space-x-3 mb-2 sm:mb-0">
                        <div className="bg-gradient-to-r from-blue-500 to-purple-500 text-white p-2 rounded-full">
                          🎓
                        </div>
                        <h3 className="text-xl font-bold text-gray-900">
                          New Match Request
                        </h3>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">
                          📅 {formatDate(request.createdAt)}
                        </span>
                      </div>
                    </div>

                    <div className="grid md:grid-cols-2 gap-6 mb-8">
                      <div className="bg-gradient-to-br from-blue-50 to-indigo-50 p-6 rounded-xl border border-blue-100">
                        <h4 className="text-lg font-bold text-blue-900 mb-3 flex items-center">
                          <span className="mr-2">🔬</span>
                          Academic Field
                        </h4>
                        <p className="text-blue-800 font-medium text-lg">
                          {request.academicField}
                        </p>
                      </div>

                      <div className="bg-gradient-to-br from-purple-50 to-pink-50 p-6 rounded-xl border border-purple-100">
                        <h4 className="text-lg font-bold text-purple-900 mb-3 flex items-center">
                          <span className="mr-2">🎯</span>
                          Mentorship Goals
                        </h4>
                        <p className="text-purple-800 leading-relaxed">
                          {request.mentorshipGoals}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex flex-col sm:flex-row lg:flex-col gap-3 lg:min-w-[200px]">
                    <button
                      onClick={() => respondToRequest(request.requestId, true)}
                      disabled={responding === request.requestId || !request.requestId}
                      className="flex-1 lg:flex-none bg-gradient-to-r from-green-500 to-emerald-600 text-white py-4 px-6 rounded-xl font-semibold hover:from-green-600 hover:to-emerald-700 focus:outline-none focus:ring-4 focus:ring-green-200 disabled:opacity-50 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5"
                    >
                      {responding === request.requestId ? (
                        <>
                          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2 inline-block"></div>
                          Accepting...
                        </>
                      ) : (
                        <>
                          <span className="mr-2">✅</span>
                          Accept Request
                        </>
                      )}
                    </button>
                      
                    <button
                      onClick={() => respondToRequest(request.requestId, false)}
                      disabled={responding === request.requestId || !request.requestId}
                      className="flex-1 lg:flex-none bg-gradient-to-r from-red-500 to-pink-600 text-white py-4 px-6 rounded-xl font-semibold hover:from-red-600 hover:to-pink-700 focus:outline-none focus:ring-4 focus:ring-red-200 disabled:opacity-50 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5"
                    >
                      {responding === request.requestId ? (
                        <>
                          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2 inline-block"></div>
                          Rejecting...
                        </>
                      ) : (
                        <>
                          <span className="mr-2">❌</span>
                          Decline Request
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Call to Action */}
        <div className="mt-16 text-center">
          <div className="bg-white rounded-2xl shadow-xl p-8 max-w-2xl mx-auto border border-gray-100">
            <h3 className="text-2xl font-bold text-gray-900 mb-4">Ready to Connect?</h3>
            <p className="text-gray-600 mb-6">Check out your active conversations and continue building meaningful mentorship relationships.</p>
            <Link
              href="/chat"
              className="inline-flex items-center px-8 py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold rounded-xl hover:from-blue-700 hover:to-purple-700 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-1"
            >
              <span className="mr-2">💬</span>
              View My Chats
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
