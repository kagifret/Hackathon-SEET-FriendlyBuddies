import { NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url)
    const userId = searchParams.get('userId')
    
    if (!userId) {
      return NextResponse.json({ error: 'User ID is required' }, { status: 400 })
    }
    
    // Forward the request to the Spring Boot backend
    const response = await fetch(`https://mentoring-backend-production.up.railway.app/api/match-requests/user/${userId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    const data = await response.text()

    if (response.ok) {
      return NextResponse.json(JSON.parse(data))
    } else {
      return NextResponse.json(
        { error: data || 'Failed to get match requests' },
        { status: response.status }
      )
    }
  } catch (error) {
    console.error('Match requests error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}