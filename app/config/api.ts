// API Configuration for both development and production
const isDevelopment = process.env.NODE_ENV === 'development';

// Explicit production URL for Vercel deployment
const PRODUCTION_API_URL = 'https://mentoring-backend-production.up.railway.app';

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 
  (isDevelopment ? 'http://localhost:8080' : PRODUCTION_API_URL);

console.log('API_BASE_URL:', API_BASE_URL, 'isDevelopment:', isDevelopment, 'NODE_ENV:', process.env.NODE_ENV);