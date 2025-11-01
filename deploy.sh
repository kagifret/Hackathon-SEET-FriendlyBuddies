#!/bin/bash

echo "🚀 SEET Mentoring Platform - Quick Deploy Script"
echo "=================================================="

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: Run this script from the project root directory"
    exit 1
fi

echo "📦 Building frontend..."
npm install
npm run build

echo "🔨 Building backend..."
cd backend
mvn clean package -DskipTests
cd ..

echo "✅ Build complete!"
echo ""
echo "🌐 Next steps:"
echo "1. Push to GitHub: git add . && git commit -m 'Deploy' && git push"
echo "2. Choose a platform:"
echo "   • Vercel: https://vercel.com (easiest for frontend)"
echo "   • Railway: https://railway.app (great for full-stack)"
echo "   • Render: https://render.com (free tier available)"
echo "   • Netlify: https://netlify.com (simple deployment)"
echo ""
echo "🎯 Your app is ready to deploy!"