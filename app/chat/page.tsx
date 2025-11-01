'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import Navigation from '../components/Navigation'
import { API_BASE_URL } from '../config/api'

interface ChatMessage {
  id: number
  sender: {
    id: number
    firstName: string
    lastName: string
    role: string
  }
  content: string
  sentAt: string
}

interface MatchDetails {
  id: number
  mentee: {
    id: number
    firstName: string
    lastName: string
  }
  mentor: {
    id: number
    firstName: string
    lastName: string
  }
  status: string
  menteeFinalDecision?: boolean
  mentorFinalDecision?: boolean
}

export default function ChatPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [newMessage, setNewMessage] = useState('')
  const [matchDetails, setMatchDetails] = useState<MatchDetails | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [userId, setUserId] = useState<string>('')
  const [userName, setUserName] = useState<string>('')
  const [userRole, setUserRole] = useState<string>('')
  const [availableMatches, setAvailableMatches] = useState<any[]>([])
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const router = useRouter()
  const searchParams = useSearchParams()
  
  const matchId = searchParams.get('matchId')

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
      
      if (!matchId) {
        // Load user's matches for chat selection
        loadUserMatches(user.id.toString(), user.role)
        return
      }
      
      // Pass the user ID directly to loadChatData to avoid state timing issues
      loadChatData(user.id.toString())
    } catch (error) {
      console.error('Error parsing user data:', error)
      router.push('/login')
      return
    }
  }, [matchId, router])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const logout = () => {
    localStorage.removeItem('user')
    router.push('/')
  }

  const loadChatData = async (currentUserId?: string) => {
    try {
      // Use the passed userId or fall back to state
      const userIdToUse = currentUserId || userId
      
      if (!userIdToUse) {
        setError('User ID not available')
        return
      }

      console.log(`Loading chat data for matchId: ${matchId}, userId: ${userIdToUse}`)

      // Check if user has access to this chat
      const accessResponse = await fetch(`${API_BASE_URL}/api/chat/match/${matchId}/access/${userIdToUse}`)
      console.log(`Access check response status: ${accessResponse.status}`)
      
      if (!accessResponse.ok) {
        setError(`Failed to check chat access: ${accessResponse.status}`)
        return
      }
      
      const hasAccess = await accessResponse.json()
      console.log(`User has access to chat: ${hasAccess}`)
      
      if (!hasAccess) {
        setError('You do not have access to this chat. The match may not be accepted yet.')
        return
      }

      // Load chat history
      const messagesResponse = await fetch(`${API_BASE_URL}/api/chat/match/${matchId}/messages`)
      console.log(`Messages response status: ${messagesResponse.status}`)
      
      if (messagesResponse.ok) {
        const chatHistory = await messagesResponse.json()
        console.log(`Loaded ${chatHistory.length} messages`)
        setMessages(chatHistory)
      }

      // Load match details (for display purposes)
      const matchResponse = await fetch(`${API_BASE_URL}/api/match-requests/${matchId}`)
      console.log(`Match details response status: ${matchResponse.status}`)
      
      if (matchResponse.ok) {
        const match = await matchResponse.json()
        console.log(`Match details:`, match)
        setMatchDetails(match)
      } else {
        console.log('Failed to load match details, but chat access was granted')
      }

    } catch (error) {
      setError('Failed to load chat data')
      console.error('Error loading chat:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadUserMatches = async (userId: string, role: string) => {
    try {
      // Use the getUserMatches endpoint to get all active matches
      const response = await fetch(`${API_BASE_URL}/api/match-requests/user/${userId}`)
      
      if (response.ok) {
        const matches = await response.json()
        console.log('Loaded user matches:', matches)
        
        // Filter to only show accepted matches that can be chatted with
        const activeMatches = matches.filter((match: any) => 
          match.status === 'MENTOR_ACCEPTED' || match.status === 'ADMIN_APPROVED'
        )
        
        setAvailableMatches(activeMatches)
      } else {
        setError('Failed to load your matches')
      }
    } catch (error) {
      setError('Failed to load your matches')
      console.error('Error loading matches:', error)
    } finally {
      setLoading(false)
    }
  }

  const sendMessage = async () => {
    if (!newMessage.trim()) return

    try {
      const response = await fetch(`${API_BASE_URL}/api/chat/match/${matchId}/message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          senderId: parseInt(userId!),
          content: newMessage.trim()
        })
      })

      if (response.ok) {
        const sentMessage = await response.json()
        setMessages(prev => [...prev, sentMessage])
        setNewMessage('')
      } else {
        setError('Failed to send message')
      }
    } catch (error) {
      setError('Failed to send message')
      console.error('Error sending message:', error)
    }
  }

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl">Loading chat...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-xl text-red-600 mb-4">{error}</div>
          <button 
            onClick={() => router.push('/match-requests')}
            className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
          >
            Back to Matches
          </button>
        </div>
      </div>
    )
  }

  // Show available matches when no specific match is selected
  if (!matchId) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        <div className="py-12 px-4 sm:px-6 lg:px-8">
          <div className="max-w-4xl mx-auto">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-extrabold text-gray-900">
                Your Chats
              </h1>
              <p className="text-lg text-gray-600 mt-2">
                Click on a match to start chatting
              </p>
            </div>

            {availableMatches.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-600 mb-4">No matches available for chat yet.</p>
                <Link
                  href="/match-requests"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                >
                  View Match Requests
                </Link>
              </div>
            ) : (
              <div className="space-y-4">
                {availableMatches.map((match) => {
                  const matchId = match.id
                  const isUserMentor = match.mentor.id.toString() === userId
                  const otherPerson = isUserMentor 
                    ? { 
                        name: `${match.mentee.firstName} ${match.mentee.lastName}`, 
                        field: match.mentee.academicField,
                        role: 'Mentee'
                      }
                    : { 
                        name: `${match.mentor.firstName} ${match.mentor.lastName}`, 
                        field: match.mentor.academicField,
                        role: 'Mentor'
                      }
                  
                  return (
                    <div
                      key={matchId}
                      className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow cursor-pointer border border-gray-200"
                      onClick={() => router.push(`/chat?matchId=${matchId}`)}
                    >
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <div className="flex items-center space-x-3 mb-2">
                            <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                              <span className="text-primary-600 font-semibold">
                                {otherPerson.name.split(' ').map(n => n[0]).join('')}
                              </span>
                            </div>
                            <div>
                              <h3 className="text-lg font-medium text-gray-900">
                                {otherPerson.name}
                              </h3>
                              <p className="text-sm text-gray-600">
                                {otherPerson.role} • {otherPerson.field}
                              </p>
                            </div>
                          </div>
                          <div className="flex items-center space-x-4 text-sm text-gray-500">
                            <span className="flex items-center">
                              <span className={`inline-block w-2 h-2 rounded-full mr-2 ${
                                match.status === 'MENTOR_ACCEPTED' ? 'bg-green-400' : 
                                match.status === 'ADMIN_APPROVED' ? 'bg-blue-400' : 'bg-gray-400'
                              }`}></span>
                              {match.status.replace('_', ' ')}
                            </span>
                          </div>
                        </div>
                        <div className="text-right">
                          <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800 hover:bg-primary-200">
                            Open Chat →
                          </span>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      {/* Header */}
      <div className="bg-white shadow-sm border-b px-6 py-4">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div>
            <button 
              onClick={() => router.push('/match-requests')}
              className="text-gray-600 hover:text-gray-800 mb-2"
            >
              ← Back to Matches
            </button>
            {matchDetails && (
              <h1 className="text-2xl font-bold text-gray-900">
                Chat with {
                  matchDetails.mentee.id.toString() === userId 
                    ? `${matchDetails.mentor.firstName} ${matchDetails.mentor.lastName}`
                    : `${matchDetails.mentee.firstName} ${matchDetails.mentee.lastName}`
                }
              </h1>
            )}
          </div>
        </div>
      </div>

      {/* Chat Container */}
      <div className="max-w-4xl mx-auto px-6 py-6">
        <div className="bg-white rounded-lg shadow-sm h-96 flex flex-col">
          
          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 ? (
              <div className="text-center text-gray-500 py-8">
                <p>No messages yet. Start the conversation!</p>
              </div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${
                    message.sender.id.toString() === userId ? 'justify-end' : 'justify-start'
                  }`}
                >
                  <div
                    className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                      message.sender.id.toString() === userId
                        ? 'bg-primary-600 text-white'
                        : 'bg-gray-200 text-gray-900'
                    }`}
                  >
                    <div className="text-sm font-medium mb-1">
                      {message.sender.firstName} {message.sender.lastName}
                      <span className="text-xs ml-2 opacity-75">
                        ({message.sender.role})
                      </span>
                    </div>
                    <div>{message.content}</div>
                    <div className="text-xs mt-1 opacity-75">
                      {new Date(message.sentAt).toLocaleTimeString()}
                    </div>
                  </div>
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Message Input */}
          <div className="border-t p-4">
            <div className="flex space-x-4">
              <textarea
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Type your message..."
                className="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                rows={2}
              />
              <button
                onClick={sendMessage}
                disabled={!newMessage.trim()}
                className="px-6 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                Send
              </button>
            </div>
          </div>
        </div>
        
        {/* Feedback Section */}
        {matchDetails && (matchDetails.status === 'MENTOR_ACCEPTED' || matchDetails.status === 'ADMIN_APPROVED') && userRole !== 'admin' && (
          <div className="mt-6 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-6 border border-blue-200">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  📝 Feedback & Rating
                </h3>
                <div className="flex items-center space-x-4 text-sm text-gray-500">
                  <span className="flex items-center">
                    <span className="inline-block w-2 h-2 rounded-full bg-blue-400 mr-2"></span>
                    Rate your mentoring experience
                  </span>
                  <span className="flex items-center">
                    <span className="inline-block w-2 h-2 rounded-full bg-green-400 mr-2"></span>
                    Share your decision to continue
                  </span>
                </div>
              </div>
              <div className="flex flex-col space-y-2">
                <Link
                  href={`/feedback?matchId=${matchId}`}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 transition-colors"
                >
                  <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11.049 2.927c.3-.921 1.603-.921 1.903 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                  </svg>
                  Provide Feedback
                </Link>
                
              </div>
            </div>
          </div>
        )}
        </div>
      </div>
  )
}