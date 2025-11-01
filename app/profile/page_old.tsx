'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter } from 'next/navigation'
import AuthWrapper from '../components/AuthWrapper'
import Navigation from '../components/Navigation'

// Types for speech recognition
interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList
}

interface SpeechRecognitionErrorEvent extends Event {
  error: string
}

interface SpeechRecognition extends EventTarget {
  continuous: boolean
  interimResults: boolean
  lang: string
  start(): void
  stop(): void
  onresult: (event: SpeechRecognitionEvent) => void
  onerror: (event: SpeechRecognitionErrorEvent) => void
  onend: () => void
}

interface Window {
  SpeechRecognition: new () => SpeechRecognition
  webkitSpeechRecognition: new () => SpeechRecognition
}

interface User {
  id: string
  firstName: string
  lastName: string
  email: string
  role: string
  city: string
  academicField: string
  currentCourse: string
  dateOfBirth: string
  languages: string
}

interface VoiceAnswers {
  question1: string
  question2: string
}

const menteeQuestions = [
  {
    id: 1,
    question: "What specific goals do you hope to achieve through mentorship? Tell us about your aspirations and what you want to learn.",
    placeholder: "Example: I want to improve my programming skills and learn about career paths in software engineering..."
  },
  {
    id: 2,
    question: "How do you prefer to communicate and learn? Describe your ideal mentoring relationship and communication style.",
    placeholder: "Example: I prefer regular check-ins with structured feedback and enjoy collaborative problem-solving..."
  }
]

const mentorQuestions = [
  {
    id: 1,
    question: "What drives your passion for mentoring? Tell us about your mentoring philosophy and what you hope to give back.",
    placeholder: "Example: I believe in empowering others through knowledge sharing and creating opportunities for growth..."
  },
  {
    id: 2,
    question: "How do you approach mentoring relationships? Describe your communication style and mentoring methods.",
    placeholder: "Example: I prefer a collaborative approach with regular feedback and hands-on guidance..."
  }
]

export default function ProfilePage() {
  const router = useRouter()
  const [userId, setUserId] = useState<string>('')
  const [userRole, setUserRole] = useState<string>('')
  const [userName, setUserName] = useState<string>('')
  const [user, setUser] = useState<User | null>(null)
  const [loadingUser, setLoadingUser] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Voice input states
  const [voiceAnswers, setVoiceAnswers] = useState<VoiceAnswers>({
    question1: '',
    question2: ''
  })
  const [currentQuestion, setCurrentQuestion] = useState(1)
  const [isRecording, setIsRecording] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)
  const [isEditingAnswer, setIsEditingAnswer] = useState<number | null>(null)
  const [tempAnswer, setTempAnswer] = useState('')

  const recognitionRef = useRef<SpeechRecognition | null>(null)

  useEffect(() => {
    const storedUserId = localStorage.getItem('userId')
    const storedUserRole = localStorage.getItem('userRole')
    
    console.log('Profile page loaded, userId:', storedUserId, 'userRole:', storedUserRole)
    
    if (!storedUserId || !storedUserRole) {
      console.log('No authentication data found, redirecting to login')
      router.push('/login')
      return
    }
    
    setUserId(storedUserId)
    setUserRole(storedUserRole)
    
    // Load user data from backend
    loadUserData(storedUserId)
  }, [router])

  const loadUserData = async (userId: string) => {
    try {
      const response = await fetch(`/api/users/${userId}`, { method: 'GET' })
      
      if (response.ok) {
        const userData = await response.json()
        setUser(userData)
        setUserName(userData.firstName + ' ' + userData.lastName)
        
        // Load existing voice answers
        await loadVoiceAnswers(userId)
      } else {
        const errorText = await response.text()
        console.error('Error loading user data:', response.status, errorText)
        // Don't redirect to login immediately - show error message first
        setError(`Failed to load user data: ${response.status}`)
      }
    } catch (error) {
      console.error('Error loading user data:', error)
      setError(`Failed to load user data: ${error instanceof Error ? error.message : 'Unknown error'}`)
    } finally {
      setLoadingUser(false)
    }
  }

  const loadVoiceAnswers = async (userId: string) => {
    try {
      const response = await fetch(`http://localhost:8080/api/voice-answers/user/${userId}`)
      
      if (response.ok) {
        const answers = await response.json()
        const voiceAnswersMap: VoiceAnswers = { question1: '', question2: '' }
        
        answers.forEach((answer: any) => {
          if (answer.questionKey === 'question1') {
            voiceAnswersMap.question1 = answer.answerText
          } else if (answer.questionKey === 'question2') {
            voiceAnswersMap.question2 = answer.answerText
          }
        })
        
        setVoiceAnswers(voiceAnswersMap)
      }
    } catch (error) {
      console.error('Error loading voice answers:', error)
    }
  }

  // Initialize speech recognition
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
      if (SpeechRecognition) {
        const recognition = new SpeechRecognition()
        recognition.continuous = false
        recognition.interimResults = false
        recognition.lang = 'en-US'
        
        recognition.onresult = (event: SpeechRecognitionEvent) => {
          const transcript = event.results[0][0].transcript
          handleTranscriptionResult(transcript)
        }
        
        recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
          console.error('Speech recognition error:', event.error)
          setIsRecording(false)
          setIsProcessing(false)
          setError('Speech recognition failed. Please try again.')
        }
        
        recognition.onend = () => {
          setIsRecording(false)
          setIsProcessing(false)
        }
        
        recognitionRef.current = recognition
      }
    }
  }, [])

  const startRecording = () => {
    if (!recognitionRef.current) {
      setError('Speech recognition is not supported in your browser.')
      return
    }
    
    setError('')
    setIsRecording(true)
    setIsProcessing(true)
    recognitionRef.current.start()
  }

  const stopRecording = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop()
    }
    setIsRecording(false)
  }

  const saveVoiceAnswer = async (questionKey: string, answerText: string) => {
    if (!userId || !userRole) return
    
    try {
      const currentQuestions = userRole === 'MENTEE' ? menteeQuestions : mentorQuestions
      const questionNum = questionKey === 'question1' ? 1 : 2
      const questionData = currentQuestions.find(q => q.id === questionNum)
      
      const voiceAnswer = {
        userId: parseInt(userId),
        questionKey,
        questionText: questionData?.question || '',
        answerText,
        userRole
      }

      const response = await fetch('http://localhost:8080/api/voice-answers/save', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(voiceAnswer),
      })

      if (!response.ok) {
        console.error('Failed to save voice answer')
      }
    } catch (error) {
      console.error('Error saving voice answer:', error)
    }
  }

  const handleTranscriptionResult = (transcript: string) => {
    const questionKey = `question${currentQuestion}` as keyof VoiceAnswers
    setVoiceAnswers(prev => ({
      ...prev,
      [questionKey]: transcript
    }))
    setIsProcessing(false)
    
    // Save to database
    saveVoiceAnswer(questionKey, transcript)
  }

  const editAnswer = (questionNum: number) => {
    const questionKey = `question${questionNum}` as keyof VoiceAnswers
    setTempAnswer(voiceAnswers[questionKey])
    setIsEditingAnswer(questionNum)
  }

  const saveEditedAnswer = () => {
    if (isEditingAnswer) {
      const questionKey = `question${isEditingAnswer}` as keyof VoiceAnswers
      setVoiceAnswers(prev => ({
        ...prev,
        [questionKey]: tempAnswer
      }))
      
      // Save to database
      saveVoiceAnswer(questionKey, tempAnswer)
      
      setIsEditingAnswer(null)
      setTempAnswer('')
    }
  }

  const cancelEdit = () => {
    setIsEditingAnswer(null)
    setTempAnswer('')
  }

  const nextQuestion = () => {
    if (currentQuestion < 2) {
      setCurrentQuestion(currentQuestion + 1)
    }
  }

  const previousQuestion = () => {
    if (currentQuestion > 1) {
      setCurrentQuestion(currentQuestion - 1)
    }
  }

  const submitProfile = async () => {
    console.log('Starting profile submission...')
    console.log('Voice answers:', voiceAnswers)
    console.log('User:', user)
    console.log('UserId:', userId, 'UserRole:', userRole)
    
    if (!voiceAnswers.question1 || !voiceAnswers.question2) {
      setError('Please answer both questions before submitting.')
      return
    }

    setLoading(true)
    setError('')

    try {
      const profileData = userRole === 'MENTEE' ? {
        academicField: user?.academicField || '',
        mentorshipGoals: voiceAnswers.question1,
        communicationStyle: voiceAnswers.question2,
        city: user?.city || '',
        language: user?.languages ? JSON.parse(user.languages)[0]?.language || '' : '',
        age: user?.dateOfBirth ? new Date().getFullYear() - new Date(user.dateOfBirth).getFullYear() : 0,
        additionalInfo: ''
      } : {
        academicField: user?.academicField || '',
        areasOfExpertise: user?.currentCourse || '',
        mentoringPhilosophy: voiceAnswers.question1,
        communicationStyle: voiceAnswers.question2,
        city: user?.city || '',
        language: user?.languages ? JSON.parse(user.languages)[0]?.language || '' : '',
        age: user?.dateOfBirth ? new Date().getFullYear() - new Date(user.dateOfBirth).getFullYear() : 0,
        additionalInfo: ''
      }

      const endpoint = userRole === 'MENTEE' 
        ? `/api/users/${userId}/mentee-profile`
        : `/api/users/${userId}/mentor-profile`

      console.log('Submitting to endpoint:', endpoint)
      console.log('Profile data:', profileData)

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(profileData),
      })

      console.log('Response status:', response.status)

      if (response.ok) {
        console.log('Profile created successfully!')
        setSuccess('Profile created successfully!')
        setTimeout(() => {
          console.log('Redirecting to dashboard...')
          router.push('/dashboard')
        }, 2000)
      } else {
        const errorText = await response.text()
        console.error('Profile creation failed:', response.status, errorText)
        setError(`Failed to create profile: ${response.status} - ${errorText}`)
      }
    } catch (error) {
      console.error('Profile creation error:', error)
      setError(`Failed to create profile: ${error instanceof Error ? error.message : 'Unknown error'}`)
    } finally {
      setLoading(false)
    }
  }

  if (!userRole || loadingUser) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">Loading profile...</div>
      </div>
    )
  }

  const currentQuestions = userRole === 'MENTEE' ? menteeQuestions : mentorQuestions
  const currentQuestionData = currentQuestions[currentQuestion - 1]

  return (
    <AuthWrapper>
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        {/* Header */}
        <div className="bg-white shadow">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="py-4">
              <div>
                <p className="text-gray-600">Hello {userName}!</p>
              </div>
            </div>
          </div>
        </div>

        {/* User Registration Data Display */}
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {user && (
            <div className="bg-white rounded-lg shadow p-6 mb-8">
              <h2 className="text-lg font-semibold mb-4">Your Registration Information</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <span className="font-medium">Name:</span> {user.firstName} {user.lastName}
                </div>
                <div>
                  <span className="font-medium">Email:</span> {user.email}
                </div>
                <div>
                  <span className="font-medium">Role:</span> {user.role}
                </div>
                <div>
                  <span className="font-medium">City:</span> {user.city}
                </div>
                <div>
                  <span className="font-medium">Academic Field:</span> {user.academicField}
                </div>
                <div>
                  <span className="font-medium">Current Course:</span> {user.currentCourse}
                </div>
                <div>
                  <span className="font-medium">Date of Birth:</span> {user.dateOfBirth}
                </div>
                <div>
                  <span className="font-medium">Languages:</span> {
                    user.languages ? 
                      JSON.parse(user.languages).map((lang: any) => `${lang.language} (${lang.proficiency})`).join(', ') 
                      : 'None'
                  }
                </div>
              </div>
            </div>
          )}

          {/* Voice Input Interface */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="mb-6">
              <h3 className="text-lg font-semibold mb-2">
                Question {currentQuestion} of 2
              </h3>
              <p className="text-gray-700 mb-4">{currentQuestionData.question}</p>
            </div>

            {/* Voice Recording Interface */}
            <div className="mb-6">
              <div className="flex flex-col items-center space-y-4">
                <button
                  onClick={isRecording ? stopRecording : startRecording}
                  disabled={isProcessing}
                  className={`w-24 h-24 rounded-full flex items-center justify-center text-white font-bold text-lg ${
                    isRecording 
                      ? 'bg-red-500 hover:bg-red-600 animate-pulse' 
                      : 'bg-blue-500 hover:bg-blue-600'
                  } ${isProcessing ? 'opacity-50 cursor-not-allowed' : ''} transition-colors`}
                >
                  {isRecording ? '⏹️' : '🎤'}
                </button>
                
                <div className="text-center">
                  <p className="text-sm text-gray-600">
                    {isRecording ? 'Recording... Click to stop' : 'Click to start recording'}
                  </p>
                  {isProcessing && (
                    <p className="text-sm text-blue-600">Processing speech...</p>
                  )}
                </div>
              </div>
            </div>

            {/* Answer Display */}
            {voiceAnswers[`question${currentQuestion}` as keyof VoiceAnswers] && (
              <div className="mb-6">
                <h4 className="font-medium mb-2">Your Answer:</h4>
                <div className="bg-gray-50 p-4 rounded-lg">
                  {isEditingAnswer === currentQuestion ? (
                    <div className="space-y-3">
                      <textarea
                        value={tempAnswer}
                        onChange={(e) => setTempAnswer(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                        rows={4}
                      />
                      <div className="flex space-x-2">
                        <button
                          onClick={saveEditedAnswer}
                          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                        >
                          Save
                        </button>
                        <button
                          onClick={cancelEdit}
                          className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div>
                      <p className="text-gray-800 mb-2">
                        {voiceAnswers[`question${currentQuestion}` as keyof VoiceAnswers]}
                      </p>
                      <button
                        onClick={() => editAnswer(currentQuestion)}
                        className="text-blue-600 hover:text-blue-800 text-sm"
                      >
                        Edit Answer
                      </button>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Navigation */}
            <div className="flex justify-between items-center">
              <button
                onClick={previousQuestion}
                disabled={currentQuestion === 1}
                className={`px-4 py-2 rounded-md ${
                  currentQuestion === 1
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'bg-gray-600 text-white hover:bg-gray-700'
                }`}
              >
                Previous
              </button>

              <div className="flex space-x-2">
                <div className={`w-3 h-3 rounded-full ${currentQuestion === 1 ? 'bg-blue-500' : 'bg-gray-300'}`} />
                <div className={`w-3 h-3 rounded-full ${currentQuestion === 2 ? 'bg-blue-500' : 'bg-gray-300'}`} />
              </div>

              {currentQuestion < 2 ? (
                <button
                  onClick={nextQuestion}
                  disabled={!voiceAnswers[`question${currentQuestion}` as keyof VoiceAnswers]}
                  className={`px-4 py-2 rounded-md ${
                    !voiceAnswers[`question${currentQuestion}` as keyof VoiceAnswers]
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-blue-600 text-white hover:bg-blue-700'
                  }`}
                >
                  Next
                </button>
              ) : (
                <button
                  onClick={submitProfile}
                  disabled={loading || !voiceAnswers.question1 || !voiceAnswers.question2}
                  className={`px-6 py-2 rounded-md ${
                    loading || !voiceAnswers.question1 || !voiceAnswers.question2
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-green-600 text-white hover:bg-green-700'
                  }`}
                >
                  {loading ? 'Creating Profile...' : 'Complete Profile'}
                </button>
              )}
            </div>

            {/* Error and Success Messages */}
            {error && (
              <div className="mt-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
                {error}
              </div>
            )}

            {success && (
              <div className="mt-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded">
                {success}
              </div>
            )}
          </div>
        </div>
      </div>
    </AuthWrapper>
  )
}
