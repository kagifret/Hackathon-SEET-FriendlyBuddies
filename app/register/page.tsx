'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '../hooks/useAuth'
import { API_BASE_URL } from '../config/api'

// Swiss cities for dropdown
const SWISS_CITIES = [
  'Zurich', 'Geneva', 'Basel', 'Bern', 'Lausanne', 'Winterthur', 'Lucerne', 'St. Gallen',
  'Lugano', 'Biel/Bienne', 'Thun', 'Köniz', 'La Chaux-de-Fonds', 'Fribourg', 'Schaffhausen',
  'Chur', 'Vernier', 'Neuchâtel', 'Uster', 'Sion', 'Lancy', 'Yverdon-les-Bains', 'Emmen',
  'Zug', 'Kriens', 'Rapperswil-Jona', 'Dübendorf', 'Dietikon', 'Montreux', 'Frauenfeld'
].sort()

// Available languages
const AVAILABLE_LANGUAGES = [
  'German', 'English', 'French', 'Italian', 'Spanish', 'Portuguese', 'Russian', 'Arabic',
  'Chinese', 'Japanese', 'Korean', 'Hindi', 'Dutch', 'Swedish', 'Norwegian', 'Danish'
].sort()

// Language proficiency levels
const PROFICIENCY_LEVELS = [
  { value: 'B1', label: 'B1 - Intermediate' },
  { value: 'B2', label: 'B2 - Upper Intermediate' },
  { value: 'C1', label: 'C1 - Advanced' },
  { value: 'C2', label: 'C2 - Proficient' },
  { value: 'NATIVE', label: 'Native Speaker' }
] as const

// Academic fields
const ACADEMIC_FIELDS = [
  'Computer Science', 'Information Technology', 'Software Engineering', 'Data Science',
  'Artificial Intelligence', 'Cybersecurity', 'Biology', 'Biomedical Sciences', 'Medicine',
  'Biotechnology', 'Chemistry', 'Physics', 'Mathematics', 'Statistics', 'Engineering',
  'Mechanical Engineering', 'Electrical Engineering', 'Civil Engineering', 'Chemical Engineering',
  'Business Administration', 'Economics', 'Finance', 'Marketing', 'Management', 'Law',
  'International Relations', 'Political Science', 'Psychology', 'Sociology', 'Philosophy',
  'Literature', 'Languages', 'History', 'Art', 'Design', 'Architecture', 'Education',
  'Environmental Science', 'Geology', 'Geography', 'Anthropology', 'Archaeology'
].sort()

interface User {
  firstName: string
  lastName: string
  email: string
  password: string
  role: 'MENTEE' | 'MENTOR'
  dateOfBirth: string
  gender: 'MALE' | 'FEMALE' | ''
  mentorGenderPreference?: 'ANY' | 'SAME'
  city: string
  languages: Array<{
    language: string
    proficiency: 'B1' | 'B2' | 'C1' | 'C2' | 'NATIVE'
  }>
  academicField: string
  currentCourse: string
  previousBackground: string
}

export default function Register() {
  const [user, setUser] = useState<User>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'MENTEE',
    dateOfBirth: '',
    gender: '',
    mentorGenderPreference: 'ANY',
    city: '',
    languages: [],
    academicField: '',
    currentCourse: '',
    previousBackground: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const router = useRouter()
  const { login } = useAuth()

  // Helper functions for language management
  const addLanguage = (language: string) => {
    if (!user.languages.find(l => l.language === language)) {
      setUser(prev => ({
        ...prev,
        languages: [...prev.languages, { language, proficiency: 'B2' }]
      }))
    }
  }

  const removeLanguage = (language: string) => {
    setUser(prev => ({
      ...prev,
      languages: prev.languages.filter(l => l.language !== language)
    }))
  }

  const updateLanguageProficiency = (language: string, proficiency: 'B1' | 'B2' | 'C1' | 'C2' | 'NATIVE') => {
    setUser(prev => ({
      ...prev,
      languages: prev.languages.map(l => 
        l.language === language ? { ...l, proficiency } : l
      )
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    // Validation
    if (!user.city) {
      setError('Please select your city of residence.')
      setLoading(false)
      return
    }

    if (user.languages.length === 0) {
      setError('Please add at least one language.')
      setLoading(false)
      return
    }

    if (!user.academicField) {
      setError('Please select your academic field.')
      setLoading(false)
      return
    }

    if (!user.currentCourse.trim()) {
      setError('Please enter your current/recent course of study.')
      setLoading(false)
      return
    }

    try {
      // Convert "SAME" gender preference to actual gender for backend
      let genderPreference: string | undefined = user.mentorGenderPreference
      if (genderPreference === 'SAME') {
        genderPreference = user.gender
      }
      
      // Convert languages array to JSON string for backend compatibility
      const userPayload = {
        ...user,
        mentorGenderPreference: genderPreference,
        languages: JSON.stringify(user.languages)
      }
      
      const response = await fetch(`${API_BASE_URL}/api/users/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userPayload),
      })

      if (response.ok) {
        const userData = await response.json()
        
        // Set auth data for both authentication systems
        localStorage.setItem('userId', userData.id.toString())
        localStorage.setItem('userRole', userData.role)
        
        // Use auth hook for AuthWrapper compatibility
        login({
          id: userData.id,
          email: userData.email,
          firstName: userData.firstName,
          lastName: userData.lastName,
          role: userData.role
        })
        
        router.push('/profile')
      } else {
        const errorMsg = await response.text()
        setError(errorMsg)
      }
    } catch (error) {
      setError('Registration failed. Please try again.')
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (field: keyof User, value: any) => {
    setUser(prev => ({
      ...prev,
      [field]: value
    }))
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900">
            Step 3: Academics & Profession
          </h1>
        
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Role Selection - Prominent Buttons */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                You are a:
              </label>
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => handleInputChange('role', 'MENTEE')}
                  className={`px-4 py-3 rounded-lg border-2 text-sm font-medium transition-colors ${
                    user.role === 'MENTEE'
                      ? 'border-blue-500 bg-blue-50 text-blue-700'
                      : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  Mentee
                  <div className="text-xs text-gray-500 mt-1">Seeking guidance</div>
                </button>
                <button
                  type="button"
                  onClick={() => handleInputChange('role', 'MENTOR')}
                  className={`px-4 py-3 rounded-lg border-2 text-sm font-medium transition-colors ${
                    user.role === 'MENTOR'
                      ? 'border-green-500 bg-green-50 text-green-700'
                      : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  Mentor
                  <div className="text-xs text-gray-500 mt-1">Providing guidance</div>
                </button>
              </div>
            </div>

            {/* Name Fields */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
                  First Name
                </label>
                <input
                  type="text"
                  id="firstName"
                  value={user.firstName}
                  onChange={(e) => handleInputChange('firstName', e.target.value)}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                  required
                />
              </div>

              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
                  Last Name
                </label>
                <input
                  type="text"
                  id="lastName"
                  value={user.lastName}
                  onChange={(e) => handleInputChange('lastName', e.target.value)}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                  required
                />
              </div>
            </div>

            {/* Date of Birth */}
            <div>
              <label htmlFor="dateOfBirth" className="block text-sm font-medium text-gray-700">
                Date of Birth
              </label>
              <input
                type="date"
                id="dateOfBirth"
                value={user.dateOfBirth}
                onChange={(e) => handleInputChange('dateOfBirth', e.target.value)}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
                max={new Date(new Date().setFullYear(new Date().getFullYear() - 16)).toISOString().split('T')[0]}
              />
              <p className="text-xs text-gray-500 mt-1">Used for age-based matching constraints</p>
            </div>

            {/* Gender */}
            <div>
              <label htmlFor="gender" className="block text-sm font-medium text-gray-700">
                Gender
              </label>
              <select
                id="gender"
                value={user.gender}
                onChange={(e) => handleInputChange('gender', e.target.value)}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
              >
                <option value="">Select gender</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
              </select>
            </div>

            {/* Mentor Gender Preference - Only for Mentees */}
            {user.role === 'MENTEE' && (
              <div className="bg-blue-50 p-4 rounded-lg">
                <label htmlFor="mentorGenderPreference" className="block text-sm font-medium text-blue-900">
                  Mentor Gender Preference
                </label>
                <select
                  id="mentorGenderPreference"
                  value={user.mentorGenderPreference || 'ANY'}
                  onChange={(e) => handleInputChange('mentorGenderPreference', e.target.value)}
                  className="mt-1 block w-full border-blue-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="ANY">Doesn't matter</option>
                  <option value="SAME">Same gender only</option>
                </select>
                <p className="text-xs text-blue-700 mt-1">
                  <strong>Mandatory Criterion 1:</strong> This preference will be strictly enforced in matching
                </p>
              </div>
            )}

            {/* City of Residence */}
            <div className="bg-orange-50 p-4 rounded-lg">
              <label htmlFor="city" className="block text-sm font-medium text-orange-900">
                City of Residence
              </label>
              <select
                id="city"
                value={user.city}
                onChange={(e) => handleInputChange('city', e.target.value)}
                className="mt-1 block w-full border-orange-300 rounded-md shadow-sm focus:ring-orange-500 focus:border-orange-500"
                required
              >
                <option value="">Select your city</option>
                {SWISS_CITIES.map(city => (
                  <option key={city} value={city}>{city}</option>
                ))}
              </select>
              <p className="text-xs text-orange-700 mt-1">
                <strong>Mandatory Criterion 4:</strong> Location-based matching for convenient meetings
              </p>
            </div>

            {/* Languages */}
            <div className="bg-green-50 p-4 rounded-lg">
              <label className="block text-sm font-medium text-green-900 mb-3">
                Languages & Proficiency
              </label>
              
              {/* Add Language Dropdown */}
              <div className="mb-4">
                <select
                  onChange={(e) => {
                    if (e.target.value) {
                      addLanguage(e.target.value)
                      e.target.value = ''
                    }
                  }}
                  className="block w-full border-green-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500"
                >
                  <option value="">Add a language...</option>
                  {AVAILABLE_LANGUAGES
                    .filter(lang => !user.languages.find(l => l.language === lang))
                    .map(language => (
                      <option key={language} value={language}>{language}</option>
                    ))
                  }
                </select>
              </div>

              {/* Selected Languages with Proficiency */}
              {user.languages.length > 0 && (
                <div className="space-y-3">
                  {user.languages.map(({ language, proficiency }) => (
                    <div key={language} className="flex items-center space-x-3 bg-white p-3 rounded border border-green-200">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="font-medium text-gray-900">{language}</span>
                          <select
                            value={proficiency}
                            onChange={(e) => updateLanguageProficiency(language, e.target.value as any)}
                            className="border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500 text-sm"
                          >
                            {PROFICIENCY_LEVELS.map(level => (
                              <option key={level.value} value={level.value}>
                                {level.label}
                              </option>
                            ))}
                          </select>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={() => removeLanguage(language)}
                        className="text-red-500 hover:text-red-700 text-sm font-medium"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}

              {user.languages.length === 0 && (
                <p className="text-sm text-green-700 bg-green-100 p-2 rounded">
                  Please add at least one language to continue
                </p>
              )}

              <p className="text-xs text-green-700 mt-3">
                <strong>Mandatory Criterion 3:</strong> Language compatibility ensures effective communication
              </p>
            </div>

            {/* Academic Fields */}
            <div className="bg-purple-50 p-4 rounded-lg">
              <label className="block text-sm font-medium text-purple-900 mb-3">
                Academic & Professional Background
              </label>
              
              {/* Academic Field */}
              <div className="mb-4">
                <label htmlFor="academicField" className="block text-sm font-medium text-purple-800 mb-1">
                  Your Academic Field
                </label>
                <select
                  id="academicField"
                  value={user.academicField}
                  onChange={(e) => handleInputChange('academicField', e.target.value)}
                  className="block w-full border-purple-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                  required
                >
                  <option value="">Select your academic field...</option>
                  {ACADEMIC_FIELDS.map(field => (
                    <option key={field} value={field}>{field}</option>
                  ))}
                </select>
              </div>

              {/* Current Course */}
              <div className="mb-4">
                <label htmlFor="currentCourse" className="block text-sm font-medium text-purple-800 mb-1">
                  Your Current/Recent Course of Study
                </label>
                <input
                  type="text"
                  id="currentCourse"
                  value={user.currentCourse}
                  onChange={(e) => handleInputChange('currentCourse', e.target.value)}
                  placeholder="e.g., MSc in Artificial Intelligence"
                  className="block w-full border-purple-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                  required
                />
              </div>

              {/* Previous Background */}
              <div className="mb-4">
                <label htmlFor="previousBackground" className="block text-sm font-medium text-purple-800 mb-1">
                  Previous Academic Background
                </label>
                <input
                  type="text"
                  id="previousBackground"
                  value={user.previousBackground}
                  onChange={(e) => handleInputChange('previousBackground', e.target.value)}
                  placeholder="e.g., BSc in Computer Science"
                  className="block w-full border-purple-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                />
                <p className="text-xs text-purple-600 mt-1">Optional - helps with more precise matching</p>
              </div>

              <p className="text-xs text-purple-700">
                <strong>Mandatory Criterion 2:</strong> Academic field compatibility ensures relevant mentoring
              </p>
            </div>

            {/* Email */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                type="email"
                id="email"
                value={user.email}
                onChange={(e) => handleInputChange('email', e.target.value)}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
              />
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                type="password"
                id="password"
                value={user.password}
                onChange={(e) => handleInputChange('password', e.target.value)}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
                minLength={6}
              />
            </div>

            {error && (
              <div className="p-3 rounded bg-red-100 text-red-700">
                {error}
              </div>
            )}

            <div className="flex space-x-4">
              <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
              >
                {loading ? 'Creating Account...' : 'Create Account'}
              </button>
              
              <Link
                href="/"
                className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md text-center hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
              >
                Back
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}