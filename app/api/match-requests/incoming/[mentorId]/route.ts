import { NextRequest, NextResponse } from 'next/server'

export async function GET(
  request: NextRequest,
  { params }: { params: { mentorId: string } }
) {
  try {
    const mentorId = params.mentorId
    
    if (!mentorId) {
      return NextResponse.json({ error: 'Mentor ID is required' }, { status: 400 })
    }
    
    // Forward the request to the Spring Boot backend
    const response = await fetch(`https://mentoring-backend-production.up.railway.app/api/match-requests/incoming/${mentorId}`, {
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
        { error: data || 'Failed to get incoming requests' },
        { status: response.status }
      )
    }
  } catch (error) {
    console.error('Error fetching incoming requests:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}