# SEET Friendly Buddies - Mentoring & Matching Platform
A hackathon project for the "Hackathon for Crisis Apps for GaaP": a mentoring / matching platform that helps match mentors and mentees, supports profiles, chat and admin tools.

# Team
- Daria Kazmina
  - Role: Interviewer; responsible for full-stack development, acting as the main interviewer with partners, preparing the presentation, holding the pitch, providing the initial open-source code, generating the foundational, detailed prompt, and fixing the deployed version for the final video.
- Ilya Kruchenetskiy
  - Role: Time-keeper & Archivist; responsible for full-stack development, Q&A during interviews – mainly focusing on the coding part, testing the initial open-source code, working on the prototype deployment, handling the final code submission, and archiving technical materials.
- Tatevik Petrosyan
  - Role: Incident recorder and Interviewer; handled ad hoc tasks, Q&A during user interviews, preparing the presentation, captured incidents in a structured log, main interviewer during the "Peer-Interview" phase post-hackathon, and primarily responsible for writing the report.

## Challenege
SEET currently relies on a manual process to match refugees (mentees) with student mentors. This process is time consuming and difficult to scale, particularly during crises when there is a sudden spike in participants. The challenge was to build a reliable, accessible platform to connect mentors and mentees while providing admin tools to oversee matches.

## Solution
We developed a hybrid decision support platform that balances automation with human oversight. The core concept is a "Smart Tinder" for mentorship that allows users to express preferences while maintaining anonymity until a match is confirmed.

## Key features
- The platform relies on anonymized profiles where users cannot upload photos initially and sensitive data is hidden to avoid bias. 
- It uses smart filtering using hard filters such as gender, academic field, language, and city, alongside soft filters for hobbies and interests. 
- An AI monitored chat ensures anonymity by censoring personal contact details like phone numbers or social handles before a match is finalized. 
- Compatibility is calculated using a formula consisting of 70% Hard/Soft Filters, 15% AI Chat Evaluation, and 15% User Feedback. 
- An Admin Dashboard provides a control panel for SEET administrators to approve, reject, or force matches based on the system's draft recommendations.

## Demo video
Visit the following link to view the demo video: https://drive.google.com/file/d/12aGAV2PQ9qHIt7j-J76POCmy7NmY_E1o/view?usp=sharing


## AI usage
We utilized generative AI (Gemini 2.5 Pro, GitHub Copilot, ChatGPT) throughout the project. The role of AI shifted from a "teammate" during the ideation phase to a specific "tool" for coding and debugging during execution.

How AI helped us:
- Design thinking: Generated a "Mom Test" interview guide to uncover hidden stakeholder requirements
- Vibe coding: We used prompts to generate the full-stack architecture (Frontend, Backend, Database) from scratch after abandoning an open source codebase
- Feature development: Successfully implemented complex logic for the 70/15/15 scoring system and the admin dashboard visualization

How AI hindered us:
- Context overload: Free models frequently lost context or hallucinated directory structures, requiring constant restarts
- Logic errors: The AI aggressively overcensored names in the chat function even when instructed to reveal them for confirmed matches, requiring manual fixes across DTOs and React components
- Self-fixing loops: The coding agent often entered loops of fixing errors that created new bugs, wasting development time

  
## Tech stack
- Frontend: Next.js (React), Node 18+
- Backend: Spring Boot (Java 17+)
- Database: PostgreSQL
- Deployment: Vercel

## Repository layout
- frontend/         — Next.js React app
- backend/          — Spring Boot service (API, auth, DB)
- node-services/    — optional Node microservices
- pitch/            — final presentation for the hackathon
- DEPLOYMENT.md     — deployment guide
- DATABASE_SETUP.md — DB setup guide

## Local setup (Windows PowerShell)
Prerequisites: Node.js, npm/yarn, Java 17, Maven (or use ./mvnw), PostgreSQL
1. Clone repo
   - git clone <this url>
2. Frontend
   - cd frontend
   - npm install
   - npm run dev
3. Backend
   - cd backend
   - ./mvnw spring-boot:run (or mvn spring-boot:run)
4. Environment variables (examples)
   - NEXT_PUBLIC_API_URL=http://localhost:8080
   - DATABASE_URL=jdbc:postgresql://localhost:5432/seet


See DEPLOYMENT.md for recommended hosting (Vercel + Railway, Render, Netlify).

## License
This project is licensed under the MIT License. see LICENSE file