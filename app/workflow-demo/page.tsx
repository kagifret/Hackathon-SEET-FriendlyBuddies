'use client';

import { useState } from 'react';
import Link from 'next/link';

export default function WorkflowDemo() {
  const [currentStep, setCurrentStep] = useState(1);

  const steps = [
    {
      id: 1,
      title: "Login as Mentee (Alice)",
      description: "Experience the mentee perspective",
      credentials: "alice@example.com / password123",
      actions: [
        "Login with Alice's credentials",
        "Go to Dashboard → Complete Profile",
        "Go to Dashboard → Browse Matches",
        "Send a match request to a mentor"
      ]
    },
    {
      id: 2,
      title: "Login as Mentor (Carol)",
      description: "Experience the mentor perspective",
      credentials: "carol@example.com / password123",
      actions: [
        "Logout and login as Carol",
        "Go to Dashboard → Review Requests",
        "Accept Alice's match request",
        "This moves the match to chat phase"
      ]
    },
    {
      id: 3,
      title: "Chat Between Alice & Carol",
      description: "Test real-time messaging",
      credentials: "Switch between both accounts",
      actions: [
        "Login as Alice → Dashboard → Chat",
        "Send messages to Carol",
        "Login as Carol → Dashboard → Chat",
        "Reply to Alice's messages",
        "Chat engagement affects the 15% scoring"
      ]
    },
    {
      id: 4,
      title: "Provide Feedback",
      description: "Rate the mentoring experience",
      credentials: "Both Alice and Carol",
      actions: [
        "In the chat, click 'Provide Feedback'",
        "Rate with stars (1-5)",
        "Decide if you want to continue",
        "Feedback affects the 15% scoring"
      ]
    },
    {
      id: 5,
      title: "Admin Final Review",
      description: "70/15/15 scoring system in action",
      credentials: "admin@example.com / admin123",
      actions: [
        "Login as Admin",
        "Go to Admin Dashboard",
        "See the complete scoring breakdown:",
        "• 70% Algorithmic matching",
        "• 15% Chat engagement", 
        "• 15% User feedback",
        "Make final approve/reject decision"
      ]
    }
  ];

  const testUsers = [
    { role: "Admin", email: "admin@example.com", password: "admin123", name: "Admin User" },
    { role: "Mentee", email: "alice@example.com", password: "password123", name: "Alice Johnson" },
    { role: "Mentee", email: "bob@example.com", password: "password123", name: "Bob Smith" },
    { role: "Mentor", email: "carol@example.com", password: "password123", name: "Carol Davis" },
    { role: "Mentor", email: "david@example.com", password: "password123", name: "David Wilson" }
  ];

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            🚀 Complete Workflow Demo
          </h1>
          <p className="text-lg text-gray-600">
            Follow this guide to test the entire mentoring platform from all perspectives
          </p>
          <Link 
            href="/"
            className="inline-block mt-4 text-indigo-600 hover:text-indigo-500"
          >
            ← Back to Home
          </Link>
          <span className="mx-2">|</span>
          <Link 
            href="/api-test"
            className="inline-block mt-4 text-green-600 hover:text-green-500"
          >
            🧪 API Testing →
          </Link>
        </div>

        {/* Test Users Reference */}
        <div className="bg-white rounded-lg shadow mb-8 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">📋 Test User Accounts</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {testUsers.map((user, index) => (
              <div key={index} className="border rounded-lg p-3">
                <div className="font-medium text-gray-900">{user.name}</div>
                <div className="text-sm text-gray-500">{user.role}</div>
                <div className="text-xs text-gray-600 mt-1">
                  {user.email}
                </div>
                <div className="text-xs text-gray-600">
                  {user.password}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Step Navigation */}
        <div className="flex justify-center mb-8">
          <div className="flex space-x-2">
            {steps.map((step) => (
              <button
                key={step.id}
                onClick={() => setCurrentStep(step.id)}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  currentStep === step.id
                    ? 'bg-indigo-600 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
              >
                Step {step.id}
              </button>
            ))}
          </div>
        </div>

        {/* Current Step */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center mb-4">
            <div className="flex-shrink-0 w-8 h-8 bg-indigo-600 text-white rounded-full flex items-center justify-center font-bold">
              {currentStep}
            </div>
            <div className="ml-4">
              <h3 className="text-xl font-semibold text-gray-900">
                {steps[currentStep - 1].title}
              </h3>
              <p className="text-gray-600">
                {steps[currentStep - 1].description}
              </p>
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <h4 className="font-medium text-blue-900 mb-2">🔑 Credentials:</h4>
            <p className="text-blue-800 font-mono text-sm">
              {steps[currentStep - 1].credentials}
            </p>
          </div>

          <div className="space-y-3">
            <h4 className="font-medium text-gray-900">📝 Actions to perform:</h4>
            <ol className="list-decimal list-inside space-y-2 text-gray-700">
              {steps[currentStep - 1].actions.map((action, index) => (
                <li key={index} className="pl-2">{action}</li>
              ))}
            </ol>
          </div>

          {/* Navigation */}
          <div className="flex justify-between mt-8">
            <button
              onClick={() => setCurrentStep(Math.max(1, currentStep - 1))}
              disabled={currentStep === 1}
              className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-400"
            >
              Previous
            </button>
            <Link
              href="/login"
              className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 font-medium"
            >
              Go to Login →
            </Link>
            <button
              onClick={() => setCurrentStep(Math.min(steps.length, currentStep + 1))}
              disabled={currentStep === steps.length}
              className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-400"
            >
              Next
            </button>
          </div>
        </div>

        {/* System Architecture Info */}
        <div className="mt-8 bg-gray-800 text-white rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-4">🏗️ System Architecture</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
            <div>
              <h4 className="font-medium text-gray-200 mb-2">7-Phase Process:</h4>
              <ol className="list-decimal list-inside space-y-1 text-gray-300">
                <li>Admin Settings ✅</li>
                <li>User Registration ✅</li>
                <li>Blind Matching ✅</li>
                <li>Mentor Review ✅</li>
                <li>Chat System ✅</li>
                <li>Feedback System ✅</li>
                <li>Admin Dashboard ✅</li>
              </ol>
            </div>
            <div>
              <h4 className="font-medium text-gray-200 mb-2">Matching Process:</h4>
              <ul className="space-y-1 text-gray-300">
                <li>• Algorithmic compatibility matching</li>
                <li>• Chat engagement tracking</li>
                <li>• User feedback and decisions</li>
                <li>• Final admin approval</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}