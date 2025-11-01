import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const response = await fetch('https://mentoring-backend-production.up.railway.app/api/admin/init-test-data', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    const data = await response.text();

    if (response.ok) {
      return NextResponse.json({ message: data });
    } else {
      return NextResponse.json(
        { message: 'Failed to initialize test data' },
        { status: response.status }
      );
    }
  } catch (error) {
    console.error('Test data initialization error:', error);
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    );
  }
}