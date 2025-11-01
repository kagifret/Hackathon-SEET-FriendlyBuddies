# SEET Friendly Buddies - Mentoring & Matching Platform
A hackathon project for the "Hackathon for Crisis Apps for GaaP": a mentoring / matching platform that helps match mentors and mentees, supports profiles, chat and admin tools.

# Team
- Daria Kazmina
- Ilya Kruchenetskiy
- Tatevik Petrosyan

## Challenege
Build a reliable, accessible mentoring platform to connect mentors and mentees during crises and provide admin tools to manage matches and monitor usage

## Solution
- Fast web UI for users to sign up and discover mentors (Next.js / React)
- Robust backend APIs and business logic (Spring Boot)
- Optional Node microservices for realtime features, small utilities or integrations
- Database persistence (Postgres) and JWT-based auth
- Deployment-ready configs and docs in DEPLOYMENT.md and DATABASE_SETUP.md

## Key Features
- User registration / login
- Profile creation and matching filters
- Real-time or near-real-time chat
- Admin dashboard and health endpoints
- Environment-based configs for dev / prod
  
## Tech stack
- Frontend: Next.js (React), Node 18+
- Backend: Spring Boot (Java 17+), Maven or Gradle
- Database: PostgreSQL
- Optional: Node services / WebSocket / Redis

## Repository layout
- frontend/         — Next.js React app
- backend/          — Spring Boot service (API, auth, DB)
- node-services/    — optional Node microservices (if present)
- pitch/            — final presentation for the hackathon
- DEPLOYMENT.md     — deployment guide
- DATABASE_SETUP.md — DB setup guide

## Local setup (Windows PowerShell)
Prerequisites: Node.js, npm/yarn, Java 17, Maven (or use ./mvnw), PostgreSQL (or use Railway).
1. Clone repo
   - git clone <this url>
2. Frontend
   - cd frontend
   - npm install
   - npm run dev
3. Backend
   - cd backend
   - ./mvnw spring-boot:run   (or mvn spring-boot:run)
4. Environment variables (examples)
   - NEXT_PUBLIC_API_URL=http://localhost:8080
   - SPRING_PROFILES_ACTIVE=local
   - DATABASE_URL=jdbc:postgresql://localhost:5432/seet
   - DATABASE_USERNAME=youruser
   - DATABASE_PASSWORD=yourpass
   - JWT_SECRET=replace-with-secure-key

## Build for production
- Frontend: cd frontend && npm run build
- Backend: cd backend && ./mvnw -DskipTests clean package

See DEPLOYMENT.md for recommended hosting (Vercel + Railway, Render, Netlify).

## License
This project is licensed under the MIT License. see LICENSE file