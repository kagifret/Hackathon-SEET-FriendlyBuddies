import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://mentoring-backend-production.up.railway.app'
  : 'http://localhost:8080'

export async function GET(
  request: NextRequest,
  { params }: { params: { requestId: string } }
) {
  try {
    const { requestId } = params
    
    // Forward the request to the Spring Boot backend
    const response = await fetch(`${API_BASE_URL}/api/match-requests/${requestId}`, {
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
        { error: data || 'Failed to get match request' },
        { status: response.status }
      )
    }
  } catch (error) {
    console.error('Match request error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}