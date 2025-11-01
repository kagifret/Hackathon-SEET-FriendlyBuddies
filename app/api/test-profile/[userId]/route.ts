import { NextRequest, NextResponse } from 'next/server'

export async function GET(
  request: NextRequest,
  { params }: { params: { userId: string } }
) {
  try {
    const { userId } = params

    return NextResponse.json({
      userId,
      message: 'Test profile endpoint',
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    console.error('Error in test profile endpoint:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}