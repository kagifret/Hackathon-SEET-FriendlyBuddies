'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'

interface ProgramSettings {
  id?: number
  maxLikesPerMentee: number
  maxAgeDifference: number
  minAgeDifference: number
  matchingSeasonActive: boolean
}

export default function AdminSettings() {
  const [settings, setSettings] = useState<ProgramSettings>({
    maxLikesPerMentee: 10,
    maxAgeDifference: 20,
    minAgeDifference: 5,
    matchingSeasonActive: false
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    fetchSettings()
  }, [])

  const fetchSettings = async () => {
    try {
      const response = await fetch('/api/admin/settings')
      if (response.ok) {
        const data = await response.json()
        setSettings(data)
      }
    } catch (error) {
      console.error('Error fetching settings:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setMessage('')

    try {
      const response = await fetch('/api/admin/settings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(settings),
      })

      if (response.ok) {
        setMessage('Settings updated successfully!')
      } else {
        setMessage('Error updating settings')
      }
    } catch (error) {
      setMessage('Error updating settings')
      console.error('Error:', error)
    } finally {
      setSaving(false)
    }
  }

  const handleInputChange = (field: keyof ProgramSettings, value: any) => {
    setSettings(prev => ({
      ...prev,
      [field]: value
    }))
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900">
            Admin Settings
          </h1>
          <p className="text-lg text-gray-600 mt-2">
            Configure matching rules and program settings
          </p>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="maxLikes" className="block text-sm font-medium text-gray-700">
                Max Likes per Mentee
              </label>
              <input
                type="number"
                id="maxLikes"
                min="1"
                max="50"
                value={settings.maxLikesPerMentee}
                onChange={(e) => handleInputChange('maxLikesPerMentee', parseInt(e.target.value))}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
              />
            </div>

            <div>
              <label htmlFor="maxAge" className="block text-sm font-medium text-gray-700">
                Max Age Difference (years)
              </label>
              <input
                type="number"
                id="maxAge"
                min="1"
                max="50"
                value={settings.maxAgeDifference}
                onChange={(e) => handleInputChange('maxAgeDifference', parseInt(e.target.value))}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
              />
            </div>

            <div>
              <label htmlFor="minAge" className="block text-sm font-medium text-gray-700">
                Min Age Difference (years)
              </label>
              <input
                type="number"
                id="minAge"
                min="0"
                max="20"
                value={settings.minAgeDifference}
                onChange={(e) => handleInputChange('minAgeDifference', parseInt(e.target.value))}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                required
              />
            </div>

            <div className="flex items-center">
              <input
                id="seasonActive"
                type="checkbox"
                checked={settings.matchingSeasonActive}
                onChange={(e) => handleInputChange('matchingSeasonActive', e.target.checked)}
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label htmlFor="seasonActive" className="ml-2 block text-sm text-gray-900">
                Matching Season Active
              </label>
            </div>

            {message && (
              <div className={`p-3 rounded ${message.includes('Error') ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                {message}
              </div>
            )}

            <div className="flex space-x-4">
              <button
                type="submit"
                disabled={saving}
                className="flex-1 bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save Settings'}
              </button>
              
              <Link
                href="/admin"
                className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md text-center hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
              >
                Back to Dashboard
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}