import { NextRequest, NextResponse } from 'next/server';

export async function POST(
  request: NextRequest,
  { params }: { params: { matchId: string } }
) {
  try {
    const matchId = params.matchId;
    
    const response = await fetch(`https://mentoring-backend-production.up.railway.app/api/admin/matches/${matchId}/reject`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data);
    } else {
      return NextResponse.json(
        { message: 'Failed to reject match' },
        { status: response.status }
      );
    }
  } catch (error) {
    console.error('Reject match error:', error);
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    );
  }
}