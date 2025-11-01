'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import Navigation from '../components/Navigation'
import { API_BASE_URL } from '../config/api'

interface MatchCandidate {
  mentorId: number
  academicField: string
  areasOfExpertise: string
  mentoringPhilosophy: string
  mentorName?: string
  city?: string
  languages?: string
  age?: number
}

export default function BrowseMatches() {
  const [candidates, setCandidates] = useState<MatchCandidate[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [userId, setUserId] = useState<string>('')
  const [userName, setUserName] = useState<string>('')
  const [expressing, setExpressing] = useState<number | null>(null)
  const router = useRouter()

  useEffect(() => {
    const userData = localStorage.getItem('user')
    if (!userData) {
      router.push('/login')
      return
    }
    
    try {
      const user = JSON.parse(userData)
      if (user.role !== 'MENTEE') {
        router.push('/dashboard')
        return
      }
      setUserId(user.id.toString())
      setUserName(user.firstName + ' ' + user.lastName)
      fetchCandidates(user.id.toString())
    } catch (error) {
      console.error('Error parsing user data:', error)
      router.push('/login')
      return
    }
  }, [router])

  const logout = () => {
    localStorage.removeItem('user')
    router.push('/')
  }

  const fetchCandidates = async (menteeId: string) => {
    try {
      // For new mentees, try the all-mentors endpoint first
      let endpoint = `${API_BASE_URL}/api/matching/all-mentors/${menteeId}`
      
      let response = await fetch(endpoint)
      if (response.ok) {
        const allMentors = await response.json()
        setCandidates(allMentors)
      } else {
        // If the mentee profile doesn't exist yet, fall back to simple mentors endpoint
        console.log('Mentee profile not found, using simple mentors endpoint')
        endpoint = `${API_BASE_URL}/api/matching/mentors`
        response = await fetch(endpoint)
        if (response.ok) {
          const simpleMentors = await response.json()
          setCandidates(simpleMentors)
        } else {
          const errorMsg = await response.text()
          setError(errorMsg)
        }
      }
    } catch (error) {
      setError('Failed to load potential matches')
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  const expressInterest = async (mentorId: number) => {
    setExpressing(mentorId)

    try {
      const response = await fetch(`${API_BASE_URL}/api/matching/express-interest`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          menteeId: parseInt(userId),
          mentorId: mentorId
        }),
      })

      if (response.ok) {
        // Remove the candidate from the list
        setCandidates(prev => prev.filter(c => c.mentorId !== mentorId))
      } else {
        const errorMsg = await response.text()
        setError(errorMsg)
      }
    } catch (error) {
      setError('Failed to express interest')
      console.error('Error:', error)
    } finally {
      setExpressing(null)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">Loading potential matches...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <div className="py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-extrabold text-gray-900">
              Browse Available Mentors
            </h1>
            <p className="text-lg text-gray-600 mt-2">
              Discover available mentors and find the perfect match for your learning journey!
            </p>
          </div>

        {error && (
          <div className="mb-6 p-4 rounded bg-red-100 text-red-700">
            {error}
          </div>
        )}

        {candidates.length === 0 ? (
          <div className="text-center">
            <div className="bg-white shadow rounded-lg p-8">
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No matches available
              </h3>
              <p className="text-gray-600 mb-4">
                There are currently no mentors that match your criteria, or you may have reached your maximum number of likes.
              </p>
              <Link
                href="/profile"
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
              >
                Update Profile
              </Link>
            </div>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {candidates.map((candidate) => (
              <div key={candidate.mentorId} className="bg-white shadow rounded-lg p-6">
                <div className="mb-4">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="text-lg font-semibold text-gray-900">
                      Academic Field
                    </h3>
                  </div>
                  <p className="text-gray-700 font-medium">
                    {candidate.academicField}
                  </p>
                </div>

                <div className="mb-4">
                  <h4 className="text-sm font-semibold text-gray-900 mb-2">
                    Areas of Expertise
                  </h4>
                  <p className="text-gray-600 text-sm">
                    {candidate.areasOfExpertise}
                  </p>
                </div>

                <div className="mb-6">
                  <h4 className="text-sm font-semibold text-gray-900 mb-2">
                    Mentoring Philosophy
                  </h4>
                  <p className="text-gray-600 text-sm">
                    {candidate.mentoringPhilosophy}
                  </p>
                </div>

                <button
                  onClick={() => expressInterest(candidate.mentorId)}
                  disabled={expressing === candidate.mentorId}
                  className="w-full bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                >
                  {expressing === candidate.mentorId ? 'Expressing Interest...' : 'Express Interest'}
                </button>
              </div>
            ))}
          </div>
        )}

        <div className="mt-8 text-center">
          <Link
            href="/match-requests"
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-primary-700 bg-primary-100 hover:bg-primary-200"
          >
            View My Requests
          </Link>
        </div>
        </div>
      </div>
    </div>
  )
}