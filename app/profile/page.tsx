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

declare global {
  interface Window {
    SpeechRecognition: new () => SpeechRecognition
    webkitSpeechRecognition: new () => SpeechRecognition
  }
}

export default function ProfilePage() {
  const [voiceAnswer, setVoiceAnswer] = useState<string>('')
  const [isRecording, setIsRecording] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [isEditingAnswer, setIsEditingAnswer] = useState(false)
  const [tempAnswer, setTempAnswer] = useState('')

  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const audioChunksRef = useRef<Blob[]>([])
  const router = useRouter()

  // Daily question - you can make this dynamic based on date
  const todaysQuestion = "What specific skills or knowledge areas would you like to develop through mentoring?"

  useEffect(() => {
    loadVoiceAnswer()
  }, [])

  const loadVoiceAnswer = async () => {
    try {
      // Check both possible localStorage keys for backward compatibility
      let userDataStr = localStorage.getItem('userData') || localStorage.getItem('user')
      if (!userDataStr) {
        router.push('/login')
        return
      }

      const userData = JSON.parse(userDataStr)
      const userId = userData.id

      if (!userId) {
        setError('User ID not found')
        return
      }

      const response = await fetch(`/api/voice-answers/${userId}`)
      if (response.ok) {
        const data = await response.json()
        if (data.answer) {
          setVoiceAnswer(data.answer)
        }
      }
    } catch (err) {
      console.error('Error loading voice answer:', err)
    }
  }

  const saveVoiceAnswer = async (answer: string) => {
    try {
      let userDataStr = localStorage.getItem('userData') || localStorage.getItem('user')
      if (!userDataStr) return

      const userData = JSON.parse(userDataStr)
      const userId = userData.id

      const response = await fetch(`/api/voice-answers/${userId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ answer }),
      })

      if (response.ok) {
        setVoiceAnswer(answer)
        setSuccess('Answer saved successfully!')
        setTimeout(() => setSuccess(''), 3000)
      }
    } catch (err) {
      console.error('Error saving voice answer:', err)
      setError('Failed to save answer')
    }
  }

  const startRecording = async () => {
    try {
      setError('')
      setIsRecording(true)
      audioChunksRef.current = []

      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      mediaRecorderRef.current = new MediaRecorder(stream)

      mediaRecorderRef.current.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data)
      }

      mediaRecorderRef.current.onstop = async () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' })
        await processAudioWithSpeechRecognition()
        
        stream.getTracks().forEach(track => track.stop())
      }

      mediaRecorderRef.current.start()
    } catch (err) {
      console.error('Error starting recording:', err)
      setError('Failed to start recording. Please check your microphone permissions.')
      setIsRecording(false)
    }
  }

  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop()
      setIsRecording(false)
    }
  }

  const processAudioWithSpeechRecognition = () => {
    return new Promise<void>((resolve, reject) => {
      setIsProcessing(true)

      const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
      if (!SpeechRecognition) {
        setError('Speech recognition not supported in this browser.')
        setIsProcessing(false)
        reject(new Error('Speech recognition not supported'))
        return
      }

      const recognition = new SpeechRecognition()
      recognition.continuous = false
      recognition.interimResults = false
      recognition.lang = 'en-US'

      recognition.onresult = (event: SpeechRecognitionEvent) => {
        const transcript = event.results[0][0].transcript
        saveVoiceAnswer(transcript)
        setIsProcessing(false)
        resolve()
      }

      recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
        console.error('Speech recognition error:', event.error)
        setError(`Speech recognition error: ${event.error}`)
        setIsProcessing(false)
        reject(new Error(event.error))
      }

      recognition.onend = () => {
        setIsProcessing(false)
        resolve()
      }

      // Start a new recording for speech recognition
      navigator.mediaDevices.getUserMedia({ audio: true })
        .then(stream => {
          recognition.start()
          setTimeout(() => {
            recognition.stop()
            stream.getTracks().forEach(track => track.stop())
          }, 5000) // Stop after 5 seconds
        })
        .catch(err => {
          console.error('Error accessing microphone:', err)
          setError('Failed to access microphone')
          setIsProcessing(false)
          reject(err)
        })
    })
  }

  const editAnswer = () => {
    setIsEditingAnswer(true)
    setTempAnswer(voiceAnswer)
  }

  const saveEditedAnswer = () => {
    saveVoiceAnswer(tempAnswer)
    setIsEditingAnswer(false)
    setTempAnswer('')
  }

  const cancelEdit = () => {
    setIsEditingAnswer(false)
    setTempAnswer('')
  }

  const submitProfile = async () => {
    if (!voiceAnswer.trim()) {
      setError('Please provide an answer to today\'s question')
      return
    }

    setLoading(true)
    setError('')

    try {
      let userDataStr = localStorage.getItem('userData') || localStorage.getItem('user')
      if (!userDataStr) {
        router.push('/login')
        return
      }

      const userData = JSON.parse(userDataStr)
      const userId = userData.id
      const userRole = userData.role

      // Determine the endpoint based on role - use simplified endpoints
      const endpoint = userRole?.toLowerCase() === 'mentor' 
        ? `/api/users/${userId}/simple-mentor-profile`
        : `/api/users/${userId}/simple-mentee-profile`

      const profileData = {
        answer: voiceAnswer
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(profileData),
      })

      if (response.ok) {
        setSuccess('Profile updated successfully!')
        setTimeout(() => {
          router.push('/dashboard')
        }, 2000)
      } else {
        const errorData = await response.text()
        setError(`Failed to update profile: ${errorData}`)
      }
    } catch (err) {
      console.error('Error submitting profile:', err)
      setError('Failed to update profile. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthWrapper>
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        
        <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Daily Question */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="mb-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Today's Question</h2>
              <p className="text-gray-700 text-lg">{todaysQuestion}</p>
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
            {voiceAnswer && (
              <div className="mb-6">
                <h4 className="font-medium mb-2">Your Answer:</h4>
                <div className="bg-gray-50 p-4 rounded-lg">
                  {isEditingAnswer ? (
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
                      <p className="text-gray-800 mb-2">{voiceAnswer}</p>
                      <button
                        onClick={editAnswer}
                        className="text-blue-600 hover:text-blue-800 text-sm"
                      >
                        Edit Answer
                      </button>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Submit Button */}
            <div className="flex justify-center">
              <button
                onClick={submitProfile}
                disabled={loading || !voiceAnswer.trim()}
                className={`px-8 py-3 rounded-md font-medium ${
                  loading || !voiceAnswer.trim()
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'bg-green-600 text-white hover:bg-green-700'
                }`}
              >
                {loading ? 'Updating Profile...' : 'Save Answer'}
              </button>
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