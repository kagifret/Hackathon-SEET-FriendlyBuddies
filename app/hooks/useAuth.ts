'use client'
import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'

interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  role: 'ADMIN' | 'MENTOR' | 'MENTEE'
}

export function useAuth() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    const checkAuth = () => {
      try {
        const userData = localStorage.getItem('user')
        if (userData) {
          const parsedUser = JSON.parse(userData)
          setUser(parsedUser)
        } else {
          // Only redirect if we're not on public pages
          const publicPages = ['/login', '/register', '/', '/workflow-demo']
          if (!publicPages.includes(window.location.pathname)) {
            router.push('/login')
          }
        }
      } catch (error) {
        console.error('Error parsing user data:', error)
        localStorage.removeItem('user')
        router.push('/login')
      } finally {
        setLoading(false)
      }
    }

    checkAuth()
  }, [router])

  const login = (userData: User) => {
    localStorage.setItem('user', JSON.stringify(userData))
    setUser(userData)
  }

  const logout = () => {
    localStorage.removeItem('user')
    setUser(null)
    router.push('/login')
  }

  return { user, loading, login, logout }
}