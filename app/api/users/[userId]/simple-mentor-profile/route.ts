import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://mentoring-backend-production.up.railway.app'
  : 'http://localhost:8080'

export async function POST(
  request: NextRequest,
  { params }: { params: { userId: string } }
) {
  try {
    const body = await request.json()
    const { userId } = params

    const response = await fetch(`${API_BASE_URL}/api/users/${userId}/simple-mentor-profile`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    if (!response.ok) {
      const errorText = await response.text()
      return NextResponse.json({ error: errorText }, { status: response.status })
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error creating simple mentor profile:', error)
    return NextResponse.json({ error: 'Failed to create mentor profile' }, { status: 500 })
  }
}