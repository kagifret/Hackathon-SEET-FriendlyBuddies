import { NextRequest, NextResponse } from 'next/server'

export async function POST(
  request: NextRequest,
  { params }: { params: { requestId: string } }
) {
  try {
    const { requestId } = params
    const body = await request.json()
    
    // Forward the request to the Spring Boot backend
    const response = await fetch(`https://mentoring-backend-production.up.railway.app/api/match-requests/${requestId}/respond`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    const data = await response.text()

    if (response.ok) {
      return NextResponse.json(JSON.parse(data))
    } else {
      return NextResponse.json(
        { error: data || 'Failed to respond to match request' },
        { status: response.status }
      )
    }
  } catch (error) {
    console.error('Match request respond error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}