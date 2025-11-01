import { NextRequest, NextResponse } from 'next/server'

export async function GET(
  request: NextRequest,
  { params }: { params: { userId: string } }
) {
  try {
    const { userId } = params

    const response = await fetch(`https://mentoring-backend-production.up.railway.app/api/users/${userId}/simple-profile`, {
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
        { error: data || 'Failed to get user profile' },
        { status: response.status }
      )
    }
  } catch (error) {
    console.error('Error getting user profile:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}