import { NextRequest, NextResponse } from 'next/server';

export async function GET() {
  try {
    // Forward the request to the backend
    const response = await fetch('https://mentoring-backend-production.up.railway.app/api/admin/dashboard', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    const data = await response.json();

    if (response.ok) {
      return NextResponse.json(data);
    } else {
      return NextResponse.json(
        { message: 'Failed to fetch dashboard data' },
        { status: response.status }
      );
    }
  } catch (error) {
    console.error('Dashboard API error:', error);
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    );
  }
}