import { NextRequest, NextResponse } from 'next/server'

export async function GET(
  request: NextRequest,
  { params }: { params: { matchId: string; userId: string } }
) {
  try {
    const { matchId, userId } = params
    
    // Forward the request to the Spring Boot backend
    const response = await fetch(`https://mentoring-backend-production.up.railway.app/api/chat/match/${matchId}/access/${userId}`, {
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
        { error: data || 'Failed to check chat access' },
        { status: response.status }
      )
    }
  } catch (error) {
    console.error('Chat access check error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}