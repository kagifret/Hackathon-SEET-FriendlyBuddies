'use client'
import Link from 'next/link'
import Image from 'next/image'
import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'

export default function Navigation() {
  const { user, logout } = useAuth()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  if (!user) {
    return null
  }

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'ADMIN': return 'bg-gradient-to-r from-purple-500 to-indigo-600 text-white'
      case 'MENTOR': return 'bg-gradient-to-r from-emerald-500 to-teal-600 text-white'
      case 'MENTEE': return 'bg-gradient-to-r from-blue-500 to-cyan-600 text-white'
      default: return 'bg-gradient-to-r from-gray-400 to-gray-600 text-white'
    }
  }

  const navigationItems = [
    { href: '/dashboard', label: '🏠 Dashboard', roles: ['ADMIN', 'MENTOR', 'MENTEE'] },
    { href: '/profile', label: '👤 Profile', roles: ['MENTOR', 'MENTEE'] },
    { href: '/browse-matches', label: '🔍 Browse Matches', roles: ['MENTEE'] },
    { href: '/match-requests', label: '📋 Match Requests', roles: ['MENTOR'] },
    { href: '/chat', label: '💬 Chat', roles: ['MENTOR', 'MENTEE'] },
    { href: '/admin', label: '⚙️ Admin', roles: ['ADMIN'] },
  ]

  const visibleItems = navigationItems.filter(item => item.roles.includes(user.role))

  return (
    <nav className="bg-white shadow-lg border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-20">
          <div className="flex items-center space-x-4">
            <Link href="/dashboard" className="flex items-center space-x-3 hover:opacity-80 transition-opacity">
              <div className="relative h-12 w-16">
                <Image
                  src="/SEET_Logo_1_gross.jpg"
                  alt="SEET Logo"
                  fill
                  className="object-contain"
                  priority
                />
              </div>
              <div className="hidden sm:block">
                <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                  SEET Mentoring
                </h1>
                <p className="text-sm text-gray-500">Connecting Minds, Building Futures</p>
              </div>
            </Link>
          </div>
          
          <div className="flex items-center space-x-2 lg:space-x-6">
            {/* Role Badge */}
            <div className={`px-4 py-2 rounded-full text-sm font-semibold shadow-sm ${getRoleColor(user.role)}`}>
              {user.role}
            </div>
            
            {/* Desktop Navigation */}
            <div className="hidden lg:flex items-center space-x-1">
              {visibleItems.map((item) => (
                <Link 
                  key={item.href}
                  href={item.href}
                  className="text-gray-700 hover:text-blue-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-blue-50"
                >
                  {item.label}
                </Link>
              ))}
            </div>

            {/* User Info & Actions */}
            <div className="hidden md:flex items-center space-x-3 pl-4 border-l border-gray-200">
              <div className="text-right">
                <p className="text-sm font-medium text-gray-900">{user.firstName} {user.lastName}</p>
                <p className="text-xs text-gray-500">Welcome back!</p>
              </div>
              
              <button
                onClick={logout}
                className="bg-gradient-to-r from-red-500 to-pink-600 text-white px-6 py-2 rounded-lg text-sm font-semibold hover:from-red-600 hover:to-pink-700 transition-all duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5"
              >
                Logout
              </button>
            </div>

            {/* Mobile menu button */}
            <div className="lg:hidden">
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="inline-flex items-center justify-center p-2 rounded-md text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-colors duration-200"
              >
                <span className="sr-only">Open main menu</span>
                {mobileMenuOpen ? (
                  <svg className="block h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                ) : (
                  <svg className="block h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile menu */}
        {mobileMenuOpen && (
          <div className="lg:hidden border-t border-gray-200 bg-white">
            <div className="px-2 pt-2 pb-3 space-y-1">
              {visibleItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-colors duration-200"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  {item.label}
                </Link>
              ))}
              
              {/* Mobile user info */}
              <div className="border-t border-gray-200 pt-4 mt-4">
                <div className="px-3 py-2">
                  <p className="text-base font-medium text-gray-900">{user.firstName} {user.lastName}</p>
                  <p className="text-sm text-gray-500">Welcome back!</p>
                </div>
                <button
                  onClick={() => {
                    logout()
                    setMobileMenuOpen(false)
                  }}
                  className="w-full text-left px-3 py-2 rounded-md text-base font-medium text-red-600 hover:text-red-700 hover:bg-red-50 transition-colors duration-200"
                >
                  Logout
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  )
}