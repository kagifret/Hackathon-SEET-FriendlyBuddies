package com.mentoringplatform.service;

import com.mentoringplatform.model.*;
import com.mentoringplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private ProgramSettingsRepository programSettingsRepository;
    
    @Autowired
    private MatchRequestRepository matchRequestRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MenteeProfileRepository menteeProfileRepository;
    
    @Autowired
    private MentorProfileRepository mentorProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProgramSettings getCurrentSettings() {
        ProgramSettings settings = programSettingsRepository.findFirstByOrderByCreatedAtDesc();
        if (settings == null) {
            // Return default settings
            return new ProgramSettings(10, 20, 5, false);
        }
        return settings;
    }

    public ProgramSettings updateSettings(ProgramSettings newSettings) {
        return programSettingsRepository.save(newSettings);
    }

    public List<Map<String, Object>> getAllMatchRequestsWithScoring() {
        List<MatchRequest> matchRequests = matchRequestRepository.findAll();
        return matchRequests.stream().map(this::calculateFinalScore).toList();
    }

    public Map<String, Object> calculateFinalScore(MatchRequest matchRequest) {
        Map<String, Object> result = new HashMap<>();
        
        // Base match request info
        result.put("id", matchRequest.getId());
        result.put("menteeId", matchRequest.getMentee().getId());
        result.put("menteeName", matchRequest.getMentee().getFirstName() + " " + matchRequest.getMentee().getLastName());
        result.put("mentorId", matchRequest.getMentor().getId());
        result.put("mentorName", matchRequest.getMentor().getFirstName() + " " + matchRequest.getMentor().getLastName());
        result.put("status", matchRequest.getStatus());
        result.put("createdAt", matchRequest.getCreatedAt());
        
        // Get scores directly from database (no recalculation)
        // 70% - Algorithmic Score
        double algorithmicScore = matchRequest.getInitialMatchScore() != null ? matchRequest.getInitialMatchScore() : 0.0;
        
        // 15% - Chat Engagement Score (from database, updated when messages are sent)
        double chatScore = matchRequest.getChatScore() != null ? matchRequest.getChatScore() : 0.0;
        
        // 15% - User Feedback Score (from database, updated when feedback is submitted)
        double feedbackScore = matchRequest.getFeedbackScore() != null ? matchRequest.getFeedbackScore() : 0.0;
        
        // Final score (from database, calculated when both users submit feedback)
        double finalScore = matchRequest.getFinalScore() != null ? matchRequest.getFinalScore() : 
                           (algorithmicScore * 0.7) + (chatScore * 0.15) + (feedbackScore * 0.15);
        
        result.put("algorithmicScore", algorithmicScore);
        result.put("chatEngagementScore", chatScore);
        result.put("userFeedbackScore", feedbackScore);
        result.put("finalScore", finalScore);
        result.put("recommendation", finalScore >= 80 ? "APPROVE" : finalScore >= 60 ? "REVIEW" : "REJECT");
        
        // Add feedback decisions
        result.put("menteeRating", matchRequest.getMenteeRating());
        result.put("mentorRating", matchRequest.getMentorRating());
        result.put("menteeFinalDecision", matchRequest.getMenteeFinalDecision());
        result.put("mentorFinalDecision", matchRequest.getMentorFinalDecision());
        
        return result;
    }

    public MatchRequest finalApproveMatch(Long matchRequestId) {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
            .orElseThrow(() -> new RuntimeException("Match request not found"));
        
        matchRequest.setStatus(MatchRequest.MatchStatus.ADMIN_APPROVED);
        matchRequest.setAdminApproved(true);
        return matchRequestRepository.save(matchRequest);
    }

    public MatchRequest finalRejectMatch(Long matchRequestId) {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId)
            .orElseThrow(() -> new RuntimeException("Match request not found"));
        
        matchRequest.setStatus(MatchRequest.MatchStatus.ADMIN_REJECTED);
        matchRequest.setAdminApproved(false);
        return matchRequestRepository.save(matchRequest);
    }

    public void initializeTestData() {
        // Clear existing data
        chatMessageRepository.deleteAll();
        matchRequestRepository.deleteAll();
        menteeProfileRepository.deleteAll();
        mentorProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user
        User admin = new User(
            "admin@example.com",
            passwordEncoder.encode("admin123"),
            "Admin",
            "User",
            User.UserRole.ADMIN
        );
        admin = userRepository.save(admin);

        // Create mentees with complete profiles
        User mentee1 = new User(
            "alice@example.com",
            passwordEncoder.encode("password123"),
            "Alice",
            "Johnson",
            User.UserRole.MENTEE,
            LocalDate.of(1998, 4, 12),
            User.Gender.FEMALE,
            User.Gender.ANY,
            "Zurich",
            "[{\"language\":\"English\",\"proficiency\":\"NATIVE\"},{\"language\":\"German\",\"proficiency\":\"B2\"}]",
            "Computer Science",
            "BSc Computer Science",
            "High School Diploma in STEM"
        );
        mentee1 = userRepository.save(mentee1);

        User mentee2 = new User(
            "bob@example.com",
            passwordEncoder.encode("password123"),
            "Bob",
            "Smith",
            User.UserRole.MENTEE,
            LocalDate.of(1997, 9, 8),
            User.Gender.MALE,
            User.Gender.ANY,
            "Basel",
            "[{\"language\":\"English\",\"proficiency\":\"NATIVE\"},{\"language\":\"French\",\"proficiency\":\"C1\"},{\"language\":\"German\",\"proficiency\":\"B1\"}]",
            "Business Administration",
            "MBA Business Analytics",
            "BSc Business Administration"
        );
        mentee2 = userRepository.save(mentee2);

        // Create mentors with complete profiles
        User mentor1 = new User(
            "carol@example.com",
            passwordEncoder.encode("password123"),
            "Carol",
            "Davis",
            User.UserRole.MENTOR,
            LocalDate.of(1988, 11, 23),
            User.Gender.FEMALE,
            null,
            "Zurich",
            "[{\"language\":\"English\",\"proficiency\":\"NATIVE\"},{\"language\":\"German\",\"proficiency\":\"C1\"},{\"language\":\"Spanish\",\"proficiency\":\"B2\"}]",
            "Computer Science",
            "MSc Software Engineering",
            "BSc Computer Science with Honors"
        );
        mentor1 = userRepository.save(mentor1);

        User mentor2 = new User(
            "david@example.com",
            passwordEncoder.encode("password123"),
            "David",
            "Wilson",
            User.UserRole.MENTOR,
            LocalDate.of(1985, 2, 16),
            User.Gender.MALE,
            null,
            "Basel",
            "[{\"language\":\"English\",\"proficiency\":\"NATIVE\"},{\"language\":\"French\",\"proficiency\":\"C2\"},{\"language\":\"German\",\"proficiency\":\"C1\"}]",
            "Business Administration",
            "MBA + Engineering Background",
            "MSc Industrial Engineering"
        );
        mentor2 = userRepository.save(mentor2);

        // ========== NEW PROFILES (10 more: 5 mentees + 5 mentors) ==========
        
        // Mentee 3: Sophie - Data Science student in Zurich
        User mentee3 = new User(
            "sophie@example.com",
            passwordEncoder.encode("password123"),
            "Sophie",
            "Mueller",
            User.UserRole.MENTEE,
            LocalDate.of(1999, 3, 15),
            User.Gender.FEMALE,
            User.Gender.ANY,
            "Zurich",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C1\"}]",
            "Data Science",
            "MSc Data Science and Machine Learning",
            "BSc Mathematics"
        );
        mentee3 = userRepository.save(mentee3);

        // Mentee 4: Thomas - Software Engineering student in Basel
        User mentee4 = new User(
            "thomas@example.com",
            passwordEncoder.encode("password123"),
            "Thomas",
            "Weber",
            User.UserRole.MENTEE,
            LocalDate.of(2000, 7, 22),
            User.Gender.MALE,
            User.Gender.ANY,
            "Basel",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"French\",\"proficiency\":\"B2\"}]",
            "Software Engineering",
            "BSc Computer Science",
            "Technical Apprenticeship in IT"
        );
        mentee4 = userRepository.save(mentee4);

        // Mentee 5: Elena - Business Administration student in Geneva
        User mentee5 = new User(
            "elena@example.com",
            passwordEncoder.encode("password123"),
            "Elena",
            "Rossi",
            User.UserRole.MENTEE,
            LocalDate.of(1998, 11, 8),
            User.Gender.FEMALE,
            User.Gender.FEMALE,
            "Geneva",
            "[{\"language\":\"French\",\"proficiency\":\"NATIVE\"},{\"language\":\"Italian\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C1\"}]",
            "Business Administration",
            "MSc International Business",
            "BSc Economics"
        );
        mentee5 = userRepository.save(mentee5);

        // Mentee 6: Lukas - Psychology student in Bern
        User mentee6 = new User(
            "lukas@example.com",
            passwordEncoder.encode("password123"),
            "Lukas",
            "Schmidt",
            User.UserRole.MENTEE,
            LocalDate.of(2001, 5, 30),
            User.Gender.MALE,
            User.Gender.MALE,
            "Bern",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"B2\"}]",
            "Psychology",
            "BSc Psychology",
            "Swiss Matura (High School Diploma)"
        );
        mentee6 = userRepository.save(mentee6);

        // Mentee 7: Anna - Biomedical Sciences student in Lausanne
        User mentee7 = new User(
            "anna@example.com",
            passwordEncoder.encode("password123"),
            "Anna",
            "Dubois",
            User.UserRole.MENTEE,
            LocalDate.of(1999, 9, 12),
            User.Gender.FEMALE,
            User.Gender.ANY,
            "Lausanne",
            "[{\"language\":\"French\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"German\",\"proficiency\":\"B1\"}]",
            "Biomedical Sciences",
            "MSc Biomedical Engineering",
            "BSc Biology"
        );
        mentee7 = userRepository.save(mentee7);

        // Mentor 3: Dr. Maria - Data Science expert in Zurich
        User mentor3 = new User(
            "maria@example.com",
            passwordEncoder.encode("password123"),
            "Maria",
            "Schneider",
            User.UserRole.MENTOR,
            LocalDate.of(1988, 4, 20),
            User.Gender.FEMALE,
            null,
            "Zurich",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"French\",\"proficiency\":\"B2\"}]",
            "Data Science",
            "PhD Computer Science (Machine Learning)",
            "MSc Statistics"
        );
        mentor3 = userRepository.save(mentor3);

        // Mentor 4: Michael - Senior Software Architect in Basel
        User mentor4 = new User(
            "michael@example.com",
            passwordEncoder.encode("password123"),
            "Michael",
            "Fischer",
            User.UserRole.MENTOR,
            LocalDate.of(1985, 8, 16),
            User.Gender.MALE,
            null,
            "Basel",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"Spanish\",\"proficiency\":\"B1\"}]",
            "Software Engineering",
            "MSc Software Engineering",
            "BSc Computer Science"
        );
        mentor4 = userRepository.save(mentor4);

        // Mentor 5: Isabelle - Business Consultant in Geneva
        User mentor5 = new User(
            "isabelle@example.com",
            passwordEncoder.encode("password123"),
            "Isabelle",
            "Martin",
            User.UserRole.MENTOR,
            LocalDate.of(1983, 12, 5),
            User.Gender.FEMALE,
            null,
            "Geneva",
            "[{\"language\":\"French\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"German\",\"proficiency\":\"C1\"}]",
            "Business Administration",
            "MBA International Management",
            "MSc Finance"
        );
        mentor5 = userRepository.save(mentor5);

        // Mentor 6: Daniel - Clinical Psychologist in Bern
        User mentor6 = new User(
            "daniel@example.com",
            passwordEncoder.encode("password123"),
            "Daniel",
            "Meier",
            User.UserRole.MENTOR,
            LocalDate.of(1980, 6, 28),
            User.Gender.MALE,
            null,
            "Bern",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C1\"},{\"language\":\"French\",\"proficiency\":\"B2\"}]",
            "Psychology",
            "PhD Clinical Psychology",
            "MSc Psychology"
        );
        mentor6 = userRepository.save(mentor6);

        // Mentor 7: Dr. Claire - Biomedical Researcher in Lausanne
        User mentor7 = new User(
            "claire@example.com",
            passwordEncoder.encode("password123"),
            "Claire",
            "Lefort",
            User.UserRole.MENTOR,
            LocalDate.of(1986, 2, 14),
            User.Gender.FEMALE,
            null,
            "Lausanne",
            "[{\"language\":\"French\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"Italian\",\"proficiency\":\"B2\"}]",
            "Biomedical Sciences",
            "PhD Biomedical Engineering",
            "MSc Biotechnology"
        );
        mentor7 = userRepository.save(mentor7);

        // ========== ADDITIONAL MENTORS (5 more) ==========
        
        // Mentor 8: Stefan - Data Science expert in Zurich
        User mentor8 = new User(
            "stefan@example.com",
            passwordEncoder.encode("password123"),
            "Stefan",
            "Huber",
            User.UserRole.MENTOR,
            LocalDate.of(1987, 10, 3),
            User.Gender.MALE,
            null,
            "Zurich",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"}]",
            "Data Science",
            "PhD Statistics and Data Mining",
            "MSc Applied Mathematics"
        );
        mentor8 = userRepository.save(mentor8);

        // Mentor 9: Laura - Software Engineering mentor in Bern
        User mentor9 = new User(
            "laura@example.com",
            passwordEncoder.encode("password123"),
            "Laura",
            "Keller",
            User.UserRole.MENTOR,
            LocalDate.of(1984, 6, 19),
            User.Gender.FEMALE,
            null,
            "Bern",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C1\"},{\"language\":\"French\",\"proficiency\":\"C1\"}]",
            "Software Engineering",
            "MSc Computer Science",
            "BSc Information Technology"
        );
        mentor9 = userRepository.save(mentor9);

        // Mentor 10: Pierre - Business mentor in Lausanne
        User mentor10 = new User(
            "pierre@example.com",
            passwordEncoder.encode("password123"),
            "Pierre",
            "Blanc",
            User.UserRole.MENTOR,
            LocalDate.of(1982, 3, 27),
            User.Gender.MALE,
            null,
            "Lausanne",
            "[{\"language\":\"French\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"German\",\"proficiency\":\"B2\"}]",
            "Business Administration",
            "MBA Executive Leadership",
            "MSc Economics"
        );
        mentor10 = userRepository.save(mentor10);

        // Mentor 11: Nina - Psychology expert in Geneva
        User mentor11 = new User(
            "nina@example.com",
            passwordEncoder.encode("password123"),
            "Nina",
            "Zimmermann",
            User.UserRole.MENTOR,
            LocalDate.of(1981, 9, 11),
            User.Gender.FEMALE,
            null,
            "Geneva",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"French\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C1\"}]",
            "Psychology",
            "PhD Organizational Psychology",
            "MSc Clinical Psychology"
        );
        mentor11 = userRepository.save(mentor11);

        // Mentor 12: Andreas - Biomedical Sciences mentor in Basel
        User mentor12 = new User(
            "andreas@example.com",
            passwordEncoder.encode("password123"),
            "Andreas",
            "Graf",
            User.UserRole.MENTOR,
            LocalDate.of(1979, 12, 8),
            User.Gender.MALE,
            null,
            "Basel",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"French\",\"proficiency\":\"B1\"}]",
            "Biomedical Sciences",
            "PhD Molecular Biology",
            "MSc Biochemistry"
        );
        mentor12 = userRepository.save(mentor12);

        // ========== ADDITIONAL DIVERSE USERS (Engineering, Medicine, Arts, etc.) ==========
        
        // Mentee 8: Oliver - Mechanical Engineering student in Zurich
        User mentee8 = new User(
            "oliver@example.com",
            passwordEncoder.encode("password123"),
            "Oliver",
            "Koch",
            User.UserRole.MENTEE,
            LocalDate.of(2000, 1, 14),
            User.Gender.MALE,
            User.Gender.ANY,
            "Zurich",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"B2\"},{\"language\":\"Italian\",\"proficiency\":\"A2\"}]",
            "Engineering",
            "BSc Mechanical Engineering",
            "Technical Apprenticeship in Manufacturing"
        );
        mentee8 = userRepository.save(mentee8);

        // Mentee 9: Camille - Medicine student in Geneva  
        User mentee9 = new User(
            "camille@example.com",
            passwordEncoder.encode("password123"),
            "Camille",
            "Moreau",
            User.UserRole.MENTEE,
            LocalDate.of(1999, 6, 3),
            User.Gender.FEMALE,
            User.Gender.FEMALE,
            "Geneva",
            "[{\"language\":\"French\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C1\"},{\"language\":\"Spanish\",\"proficiency\":\"B1\"}]",
            "Medicine",
            "Medical School Year 4",
            "BSc Biomedical Sciences"
        );
        mentee9 = userRepository.save(mentee9);

        // Mentee 10: Marco - Finance student in Basel
        User mentee10 = new User(
            "marco@example.com",
            passwordEncoder.encode("password123"),
            "Marco",
            "Bianchi",
            User.UserRole.MENTEE,
            LocalDate.of(1998, 10, 25),
            User.Gender.MALE,
            User.Gender.ANY,
            "Basel",
            "[{\"language\":\"Italian\",\"proficiency\":\"NATIVE\"},{\"language\":\"German\",\"proficiency\":\"C1\"},{\"language\":\"English\",\"proficiency\":\"C2\"}]",
            "Finance",
            "MSc Quantitative Finance",
            "BSc Economics"
        );
        mentee10 = userRepository.save(mentee10);

        // Mentee 11: Lina - Environmental Science student in Bern
        User mentee11 = new User(
            "lina@example.com",
            passwordEncoder.encode("password123"),
            "Lina",
            "Andersson",
            User.UserRole.MENTEE,
            LocalDate.of(2000, 4, 18),
            User.Gender.FEMALE,
            User.Gender.ANY,
            "Bern",
            "[{\"language\":\"Swedish\",\"proficiency\":\"NATIVE\"},{\"language\":\"German\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C1\"}]",
            "Environmental Science",
            "MSc Environmental Engineering",
            "BSc Environmental Sciences"
        );
        mentee11 = userRepository.save(mentee11);

        // Mentee 12: Hassan - Cybersecurity student in Lausanne
        User mentee12 = new User(
            "hassan@example.com",
            passwordEncoder.encode("password123"),
            "Hassan",
            "Al-Rahman",
            User.UserRole.MENTEE,
            LocalDate.of(1999, 8, 7),
            User.Gender.MALE,
            User.Gender.MALE,
            "Lausanne",
            "[{\"language\":\"Arabic\",\"proficiency\":\"NATIVE\"},{\"language\":\"French\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C1\"}]",
            "Cybersecurity",
            "MSc Information Security",
            "BSc Computer Science"
        );
        mentee12 = userRepository.save(mentee12);

        // Mentor 13: Dr. Robert - Mechanical Engineering expert in Zurich
        User mentor13 = new User(
            "robert@example.com",
            passwordEncoder.encode("password123"),
            "Robert",
            "Lehmann",
            User.UserRole.MENTOR,
            LocalDate.of(1978, 11, 2),
            User.Gender.MALE,
            null,
            "Zurich",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"French\",\"proficiency\":\"B1\"}]",
            "Engineering",
            "PhD Mechanical Engineering",
            "MSc Aerospace Engineering"
        );
        mentor13 = userRepository.save(mentor13);

        // Mentor 14: Dr. Sophia - Medical Doctor in Geneva
        User mentor14 = new User(
            "sophia@example.com",
            passwordEncoder.encode("password123"),
            "Sophia",
            "Bergmann",
            User.UserRole.MENTOR,
            LocalDate.of(1984, 3, 19),
            User.Gender.FEMALE,
            null,
            "Geneva",
            "[{\"language\":\"German\",\"proficiency\":\"NATIVE\"},{\"language\":\"French\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C2\"}]",
            "Medicine",
            "MD Specialized in Cardiology",
            "Medical Residency in Internal Medicine"
        );
        mentor14 = userRepository.save(mentor14);

        // Mentor 15: Alessandro - Finance expert in Basel
        User mentor15 = new User(
            "alessandro@example.com",
            passwordEncoder.encode("password123"),
            "Alessandro",
            "Romano",
            User.UserRole.MENTOR,
            LocalDate.of(1981, 7, 12),
            User.Gender.MALE,
            null,
            "Basel",
            "[{\"language\":\"Italian\",\"proficiency\":\"NATIVE\"},{\"language\":\"German\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"French\",\"proficiency\":\"B2\"}]",
            "Finance",
            "MBA Finance & Investment Banking",
            "MSc Quantitative Finance"
        );
        mentor15 = userRepository.save(mentor15);

        // Mentor 16: Dr. Astrid - Environmental Scientist in Bern
        User mentor16 = new User(
            "astrid@example.com",
            passwordEncoder.encode("password123"),
            "Astrid",
            "Larsson",
            User.UserRole.MENTOR,
            LocalDate.of(1980, 9, 24),
            User.Gender.FEMALE,
            null,
            "Bern",
            "[{\"language\":\"Swedish\",\"proficiency\":\"NATIVE\"},{\"language\":\"German\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"French\",\"proficiency\":\"B1\"}]",
            "Environmental Science",
            "PhD Environmental Chemistry",
            "MSc Climate Science"
        );
        mentor16 = userRepository.save(mentor16);

        // Mentor 17: Omar - Cybersecurity expert in Lausanne
        User mentor17 = new User(
            "omar@example.com",
            passwordEncoder.encode("password123"),
            "Omar",
            "Mansouri",
            User.UserRole.MENTOR,
            LocalDate.of(1983, 12, 15),
            User.Gender.MALE,
            null,
            "Lausanne",
            "[{\"language\":\"Arabic\",\"proficiency\":\"NATIVE\"},{\"language\":\"French\",\"proficiency\":\"C2\"},{\"language\":\"English\",\"proficiency\":\"C2\"},{\"language\":\"German\",\"proficiency\":\"B1\"}]",
            "Cybersecurity",
            "MSc Information Security & Cryptography",
            "BSc Computer Engineering"
        );
        mentor17 = userRepository.save(mentor17);

        // Create mentee profiles
        MenteeProfile menteeProfile1 = new MenteeProfile();
        menteeProfile1.setUser(mentee1);
        menteeProfile1.setAcademicField("Computer Science");
        menteeProfile1.setMentorshipGoals("Career advancement and technical skills in software engineering");
        menteeProfile1.setCommunicationStyle("Direct and frequent");
        menteeProfile1.setCity("Zurich");
        menteeProfile1.setLanguage("English, German");
        menteeProfile1.setAge(27);
        menteeProfile1.setAdditionalInfo("Looking for guidance in system design and leadership, strong programming background");
        menteeProfile1 = menteeProfileRepository.save(menteeProfile1);

        MenteeProfile menteeProfile2 = new MenteeProfile();
        menteeProfile2.setUser(mentee2);
        menteeProfile2.setAcademicField("Business Administration");
        menteeProfile2.setMentorshipGoals("Transition to Product Management role and develop strategic thinking");
        menteeProfile2.setCommunicationStyle("Collaborative and structured");
        menteeProfile2.setCity("Basel");
        menteeProfile2.setLanguage("English, French, German");
        menteeProfile2.setAge(28);
        menteeProfile2.setAdditionalInfo("Background in business analysis, seeking PM skills and leadership development");
        menteeProfile2 = menteeProfileRepository.save(menteeProfile2);

        // Create mentor profiles
        MentorProfile mentorProfile1 = new MentorProfile();
        mentorProfile1.setUser(mentor1);
        mentorProfile1.setAcademicField("Computer Science");
        mentorProfile1.setAreasOfExpertise("Software Engineering, System Design, Leadership, Full-Stack Development");
        mentorProfile1.setMentoringPhilosophy("Hands-on learning with practical projects and real-world experience");
        mentorProfile1.setCommunicationStyle("Patient and encouraging");
        mentorProfile1.setCity("Zurich");
        mentorProfile1.setLanguage("English, German, Spanish");
        mentorProfile1.setAge(37);
        mentorProfile1.setAdditionalInfo("Senior Software Architect with 12+ years experience, specializing in scalable systems");
        mentorProfile1 = mentorProfileRepository.save(mentorProfile1);

        MentorProfile mentorProfile2 = new MentorProfile();
        mentorProfile2.setUser(mentor2);
        mentorProfile2.setAcademicField("Business Administration");
        mentorProfile2.setAreasOfExpertise("Product Management, Strategy, User Research, Digital Transformation");
        mentorProfile2.setMentoringPhilosophy("Strategic thinking combined with data-driven decisions and agile execution");
        mentorProfile2.setCommunicationStyle("Analytical and supportive");
        mentorProfile2.setCity("Basel");
        mentorProfile2.setLanguage("English, French, German");
        mentorProfile2.setAge(40);
        mentorProfile2.setAdditionalInfo("Senior Product Director with 15+ years in tech, former startup founder");
        mentorProfile2 = mentorProfileRepository.save(mentorProfile2);

        // ========== NEW MENTEE PROFILES ==========
        
        // Mentee Profile 3: Sophie
        MenteeProfile menteeProfile3 = new MenteeProfile();
        menteeProfile3.setUser(mentee3);
        menteeProfile3.setAcademicField("Data Science");
        menteeProfile3.setMentorshipGoals("Learn advanced machine learning techniques and career guidance in AI research");
        menteeProfile3.setCommunicationStyle("Structured and analytical");
        menteeProfile3.setCity("Zurich");
        menteeProfile3.setLanguage("German, English");
        menteeProfile3.setAge(26);
        menteeProfile3.setAdditionalInfo("Strong mathematical background, interested in deep learning and NLP");
        menteeProfile3 = menteeProfileRepository.save(menteeProfile3);

        // Mentee Profile 4: Thomas
        MenteeProfile menteeProfile4 = new MenteeProfile();
        menteeProfile4.setUser(mentee4);
        menteeProfile4.setAcademicField("Software Engineering");
        menteeProfile4.setMentorshipGoals("Master software architecture patterns and transition to senior developer role");
        menteeProfile4.setCommunicationStyle("Practical and hands-on");
        menteeProfile4.setCity("Basel");
        menteeProfile4.setLanguage("German, English, French");
        menteeProfile4.setAge(25);
        menteeProfile4.setAdditionalInfo("Experience with web development, seeking guidance in system design");
        menteeProfile4 = menteeProfileRepository.save(menteeProfile4);

        // Mentee Profile 5: Elena
        MenteeProfile menteeProfile5 = new MenteeProfile();
        menteeProfile5.setUser(mentee5);
        menteeProfile5.setAcademicField("Business Administration");
        menteeProfile5.setMentorshipGoals("Develop strategic consulting skills and understand Swiss business culture");
        menteeProfile5.setCommunicationStyle("Collaborative and open-minded");
        menteeProfile5.setCity("Geneva");
        menteeProfile5.setLanguage("French, Italian, English");
        menteeProfile5.setAge(27);
        menteeProfile5.setAdditionalInfo("International background, focus on sustainable business practices");
        menteeProfile5 = menteeProfileRepository.save(menteeProfile5);

        // Mentee Profile 6: Lukas
        MenteeProfile menteeProfile6 = new MenteeProfile();
        menteeProfile6.setUser(mentee6);
        menteeProfile6.setAcademicField("Psychology");
        menteeProfile6.setMentorshipGoals("Gain clinical experience and understand therapeutic approaches");
        menteeProfile6.setCommunicationStyle("Empathetic and reflective");
        menteeProfile6.setCity("Bern");
        menteeProfile6.setLanguage("German, English");
        menteeProfile6.setAge(24);
        menteeProfile6.setAdditionalInfo("Interested in cognitive behavioral therapy and mental health research");
        menteeProfile6 = menteeProfileRepository.save(menteeProfile6);

        // Mentee Profile 7: Anna
        MenteeProfile menteeProfile7 = new MenteeProfile();
        menteeProfile7.setUser(mentee7);
        menteeProfile7.setAcademicField("Biomedical Sciences");
        menteeProfile7.setMentorshipGoals("Research guidance in medical devices and career path in biotech industry");
        menteeProfile7.setCommunicationStyle("Detail-oriented and curious");
        menteeProfile7.setCity("Lausanne");
        menteeProfile7.setLanguage("French, English, German");
        menteeProfile7.setAge(26);
        menteeProfile7.setAdditionalInfo("Research focus on prosthetics and neural interfaces");
        menteeProfile7 = menteeProfileRepository.save(menteeProfile7);

        // ========== NEW MENTOR PROFILES ==========
        
        // Mentor Profile 3: Dr. Maria
        MentorProfile mentorProfile3 = new MentorProfile();
        mentorProfile3.setUser(mentor3);
        mentorProfile3.setAcademicField("Data Science");
        mentorProfile3.setAreasOfExpertise("Machine Learning, AI Research, Statistical Modeling, Python/R");
        mentorProfile3.setMentoringPhilosophy("Theory meets practice - understanding fundamentals while building real projects");
        mentorProfile3.setCommunicationStyle("Patient and encouraging");
        mentorProfile3.setCity("Zurich");
        mentorProfile3.setLanguage("German, English, French");
        mentorProfile3.setAge(37);
        mentorProfile3.setAdditionalInfo("Lead Data Scientist at major tech company, published researcher");
        mentorProfile3 = mentorProfileRepository.save(mentorProfile3);

        // Mentor Profile 4: Michael
        MentorProfile mentorProfile4 = new MentorProfile();
        mentorProfile4.setUser(mentor4);
        mentorProfile4.setAcademicField("Software Engineering");
        mentorProfile4.setAreasOfExpertise("Software Architecture, Microservices, Cloud Computing, Team Leadership");
        mentorProfile4.setMentoringPhilosophy("Learn through building - focus on clean code and scalable systems");
        mentorProfile4.setCommunicationStyle("Direct and constructive");
        mentorProfile4.setCity("Basel");
        mentorProfile4.setLanguage("German, English, Spanish");
        mentorProfile4.setAge(40);
        mentorProfile4.setAdditionalInfo("Chief Architect with 15 years experience, former startup CTO");
        mentorProfile4 = mentorProfileRepository.save(mentorProfile4);

        // Mentor Profile 5: Isabelle
        MentorProfile mentorProfile5 = new MentorProfile();
        mentorProfile5.setUser(mentor5);
        mentorProfile5.setAcademicField("Business Administration");
        mentorProfile5.setAreasOfExpertise("Strategic Consulting, International Business, Change Management, Leadership");
        mentorProfile5.setMentoringPhilosophy("Strategic thinking combined with practical execution");
        mentorProfile5.setCommunicationStyle("Supportive and strategic");
        mentorProfile5.setCity("Geneva");
        mentorProfile5.setLanguage("French, English, German");
        mentorProfile5.setAge(42);
        mentorProfile5.setAdditionalInfo("Senior Partner at consulting firm, specializing in Swiss market");
        mentorProfile5 = mentorProfileRepository.save(mentorProfile5);

        // Mentor Profile 6: Daniel
        MentorProfile mentorProfile6 = new MentorProfile();
        mentorProfile6.setUser(mentor6);
        mentorProfile6.setAcademicField("Psychology");
        mentorProfile6.setAreasOfExpertise("Clinical Psychology, CBT, Trauma Therapy, Research Methods");
        mentorProfile6.setMentoringPhilosophy("Evidence-based practice with compassionate care");
        mentorProfile6.setCommunicationStyle("Empathetic and professional");
        mentorProfile6.setCity("Bern");
        mentorProfile6.setLanguage("German, English, French");
        mentorProfile6.setAge(45);
        mentorProfile6.setAdditionalInfo("Licensed psychotherapist with private practice and university teaching");
        mentorProfile6 = mentorProfileRepository.save(mentorProfile6);

        // Mentor Profile 7: Dr. Claire
        MentorProfile mentorProfile7 = new MentorProfile();
        mentorProfile7.setUser(mentor7);
        mentorProfile7.setAcademicField("Biomedical Sciences");
        mentorProfile7.setAreasOfExpertise("Biomedical Engineering, Medical Devices, Research & Development, Grant Writing");
        mentorProfile7.setMentoringPhilosophy("Innovation through interdisciplinary collaboration");
        mentorProfile7.setCommunicationStyle("Collaborative and innovative");
        mentorProfile7.setCity("Lausanne");
        mentorProfile7.setLanguage("French, English, Italian");
        mentorProfile7.setAge(39);
        mentorProfile7.setAdditionalInfo("Principal Investigator at EPFL, multiple patents in medical technology");
        mentorProfile7 = mentorProfileRepository.save(mentorProfile7);

        // ========== ADDITIONAL MENTOR PROFILES (5 more) ==========
        
        // Mentor Profile 8: Stefan
        MentorProfile mentorProfile8 = new MentorProfile();
        mentorProfile8.setUser(mentor8);
        mentorProfile8.setAcademicField("Data Science");
        mentorProfile8.setAreasOfExpertise("Statistical Analysis, Predictive Modeling, Big Data, R and Python");
        mentorProfile8.setMentoringPhilosophy("Data driven decisions through rigorous statistical methods");
        mentorProfile8.setCommunicationStyle("Analytical and thorough");
        mentorProfile8.setCity("Zurich");
        mentorProfile8.setLanguage("German, English");
        mentorProfile8.setAge(38);
        mentorProfile8.setAdditionalInfo("Senior Data Analyst at financial services company with focus on predictive analytics");
        mentorProfile8 = mentorProfileRepository.save(mentorProfile8);

        // Mentor Profile 9: Laura
        MentorProfile mentorProfile9 = new MentorProfile();
        mentorProfile9.setUser(mentor9);
        mentorProfile9.setAcademicField("Software Engineering");
        mentorProfile9.setAreasOfExpertise("Full Stack Development, Agile Methods, DevOps, Code Quality");
        mentorProfile9.setMentoringPhilosophy("Build quality software through best practices and continuous learning");
        mentorProfile9.setCommunicationStyle("Supportive and practical");
        mentorProfile9.setCity("Bern");
        mentorProfile9.setLanguage("German, English, French");
        mentorProfile9.setAge(41);
        mentorProfile9.setAdditionalInfo("Lead Developer with 12 years experience in web and mobile applications");
        mentorProfile9 = mentorProfileRepository.save(mentorProfile9);

        // Mentor Profile 10: Pierre
        MentorProfile mentorProfile10 = new MentorProfile();
        mentorProfile10.setUser(mentor10);
        mentorProfile10.setAcademicField("Business Administration");
        mentorProfile10.setAreasOfExpertise("Entrepreneurship, Financial Planning, Market Analysis, Business Development");
        mentorProfile10.setMentoringPhilosophy("Empower entrepreneurs through practical business knowledge and networking");
        mentorProfile10.setCommunicationStyle("Engaging and motivational");
        mentorProfile10.setCity("Lausanne");
        mentorProfile10.setLanguage("French, English, German");
        mentorProfile10.setAge(43);
        mentorProfile10.setAdditionalInfo("Serial entrepreneur and startup advisor with three successful exits");
        mentorProfile10 = mentorProfileRepository.save(mentorProfile10);

        // Mentor Profile 11: Nina
        MentorProfile mentorProfile11 = new MentorProfile();
        mentorProfile11.setUser(mentor11);
        mentorProfile11.setAcademicField("Psychology");
        mentorProfile11.setAreasOfExpertise("Workplace Psychology, Career Counseling, Stress Management, Team Dynamics");
        mentorProfile11.setMentoringPhilosophy("Support personal growth through self awareness and evidence based techniques");
        mentorProfile11.setCommunicationStyle("Warm and understanding");
        mentorProfile11.setCity("Geneva");
        mentorProfile11.setLanguage("German, French, English");
        mentorProfile11.setAge(44);
        mentorProfile11.setAdditionalInfo("HR Psychologist with corporate and private practice experience");
        mentorProfile11 = mentorProfileRepository.save(mentorProfile11);

        // Mentor Profile 12: Andreas
        MentorProfile mentorProfile12 = new MentorProfile();
        mentorProfile12.setUser(mentor12);
        mentorProfile12.setAcademicField("Biomedical Sciences");
        mentorProfile12.setAreasOfExpertise("Molecular Research, Laboratory Management, Scientific Writing, Drug Development");
        mentorProfile12.setMentoringPhilosophy("Excellence in research through methodical approach and critical thinking");
        mentorProfile12.setCommunicationStyle("Precise and detail oriented");
        mentorProfile12.setCity("Basel");
        mentorProfile12.setLanguage("German, English, French");
        mentorProfile12.setAge(46);
        mentorProfile12.setAdditionalInfo("Research Director at pharmaceutical company with 20 years industry experience");
        mentorProfile12 = mentorProfileRepository.save(mentorProfile12);

        // ========== ADDITIONAL MENTEE PROFILES ==========
        
        // Mentee Profile 8: Oliver - Mechanical Engineering
        MenteeProfile menteeProfile8 = new MenteeProfile();
        menteeProfile8.setUser(mentee8);
        menteeProfile8.setAcademicField("Engineering");
        menteeProfile8.setMentorshipGoals("Learn advanced manufacturing techniques and transition to engineering management");
        menteeProfile8.setCommunicationStyle("Hands-on and practical");
        menteeProfile8.setCity("Zurich");
        menteeProfile8.setLanguage("German, English, Italian");
        menteeProfile8.setAge(25);
        menteeProfile8.setAdditionalInfo("Passionate about sustainable manufacturing and automation");
        menteeProfile8 = menteeProfileRepository.save(menteeProfile8);

        // Mentee Profile 9: Camille - Medicine
        MenteeProfile menteeProfile9 = new MenteeProfile();
        menteeProfile9.setUser(mentee9);
        menteeProfile9.setAcademicField("Medicine");
        menteeProfile9.setMentorshipGoals("Specialize in pediatric cardiology and research medical innovation");
        menteeProfile9.setCommunicationStyle("Compassionate and detail oriented");
        menteeProfile9.setCity("Geneva");
        menteeProfile9.setLanguage("French, English, Spanish");
        menteeProfile9.setAge(26);
        menteeProfile9.setAdditionalInfo("Volunteering in medical missions, interested in global health");
        menteeProfile9 = menteeProfileRepository.save(menteeProfile9);

        // Mentee Profile 10: Marco - Finance
        MenteeProfile menteeProfile10 = new MenteeProfile();
        menteeProfile10.setUser(mentee10); 
        menteeProfile10.setAcademicField("Finance");
        menteeProfile10.setMentorshipGoals("Master algorithmic trading and become a quantitative analyst");
        menteeProfile10.setCommunicationStyle("Analytical and precise");
        menteeProfile10.setCity("Basel");
        menteeProfile10.setLanguage("Italian, German, English");
        menteeProfile10.setAge(27);
        menteeProfile10.setAdditionalInfo("Strong mathematical background, interested in fintech");
        menteeProfile10 = menteeProfileRepository.save(menteeProfile10);

        // Mentee Profile 11: Lina - Environmental Science
        MenteeProfile menteeProfile11 = new MenteeProfile();
        menteeProfile11.setUser(mentee11);
        menteeProfile11.setAcademicField("Environmental Science");
        menteeProfile11.setMentorshipGoals("Research climate change solutions and sustainable technology development");
        menteeProfile11.setCommunicationStyle("Collaborative and research focused");
        menteeProfile11.setCity("Bern");
        menteeProfile11.setLanguage("Swedish, German, English");
        menteeProfile11.setAge(25);
        menteeProfile11.setAdditionalInfo("Active in environmental advocacy, seeking research opportunities");
        menteeProfile11 = menteeProfileRepository.save(menteeProfile11);

        // Mentee Profile 12: Hassan - Cybersecurity
        MenteeProfile menteeProfile12 = new MenteeProfile();
        menteeProfile12.setUser(mentee12);
        menteeProfile12.setAcademicField("Cybersecurity");
        menteeProfile12.setMentorshipGoals("Develop expertise in ethical hacking and security architecture");
        menteeProfile12.setCommunicationStyle("Technical and security focused");
        menteeProfile12.setCity("Lausanne");
        menteeProfile12.setLanguage("Arabic, French, English");
        menteeProfile12.setAge(26);
        menteeProfile12.setAdditionalInfo("Certified in multiple security frameworks, interested in threat intelligence");
        menteeProfile12 = menteeProfileRepository.save(menteeProfile12);

        // ========== ADDITIONAL MENTOR PROFILES ==========
        
        // Mentor Profile 13: Dr. Robert - Mechanical Engineering
        MentorProfile mentorProfile13 = new MentorProfile();
        mentorProfile13.setUser(mentor13);
        mentorProfile13.setAcademicField("Engineering");
        mentorProfile13.setAreasOfExpertise("Manufacturing Engineering, Automation, Project Management, Quality Systems");
        mentorProfile13.setMentoringPhilosophy("Learn by doing - practical engineering solutions for real problems");
        mentorProfile13.setCommunicationStyle("Systematic and practical");
        mentorProfile13.setCity("Zurich");
        mentorProfile13.setLanguage("German, English, French");
        mentorProfile13.setAge(47);
        mentorProfile13.setAdditionalInfo("Engineering Director at leading manufacturing company, 20+ years experience");
        mentorProfile13 = mentorProfileRepository.save(mentorProfile13);

        // Mentor Profile 14: Dr. Sophia - Medicine
        MentorProfile mentorProfile14 = new MentorProfile();
        mentorProfile14.setUser(mentor14);
        mentorProfile14.setAcademicField("Medicine");
        mentorProfile14.setAreasOfExpertise("Cardiology, Medical Research, Clinical Trials, Medical Device Innovation");
        mentorProfile14.setMentoringPhilosophy("Evidence-based medicine with patient-centered care");
        mentorProfile14.setCommunicationStyle("Empathetic and scientific");
        mentorProfile14.setCity("Geneva");
        mentorProfile14.setLanguage("German, French, English");
        mentorProfile14.setAge(41);
        mentorProfile14.setAdditionalInfo("Senior Cardiologist at University Hospital, published researcher");
        mentorProfile14 = mentorProfileRepository.save(mentorProfile14);

        // Mentor Profile 15: Alessandro - Finance
        MentorProfile mentorProfile15 = new MentorProfile();
        mentorProfile15.setUser(mentor15);
        mentorProfile15.setAcademicField("Finance");
        mentorProfile15.setAreasOfExpertise("Investment Banking, Quantitative Analysis, Risk Management, Financial Technology");
        mentorProfile15.setMentoringPhilosophy("Combine analytical rigor with market intuition");
        mentorProfile15.setCommunicationStyle("Analytical and strategic");
        mentorProfile15.setCity("Basel");
        mentorProfile15.setLanguage("Italian, German, English, French");
        mentorProfile15.setAge(44);
        mentorProfile15.setAdditionalInfo("VP at international bank, specializing in structured products");
        mentorProfile15 = mentorProfileRepository.save(mentorProfile15);

        // Mentor Profile 16: Dr. Astrid - Environmental Science
        MentorProfile mentorProfile16 = new MentorProfile();
        mentorProfile16.setUser(mentor16);
        mentorProfile16.setAcademicField("Environmental Science");
        mentorProfile16.setAreasOfExpertise("Climate Research, Environmental Policy, Sustainable Technology, Grant Writing");
        mentorProfile16.setMentoringPhilosophy("Science-driven solutions for environmental challenges");
        mentorProfile16.setCommunicationStyle("Research focused and collaborative");
        mentorProfile16.setCity("Bern");
        mentorProfile16.setLanguage("Swedish, German, English, French");
        mentorProfile16.setAge(45);
        mentorProfile16.setAdditionalInfo("Research Director at environmental institute, IPCC contributor");
        mentorProfile16 = mentorProfileRepository.save(mentorProfile16);

        // Mentor Profile 17: Omar - Cybersecurity
        MentorProfile mentorProfile17 = new MentorProfile();
        mentorProfile17.setUser(mentor17);
        mentorProfile17.setAcademicField("Cybersecurity");
        mentorProfile17.setAreasOfExpertise("Penetration Testing, Security Architecture, Threat Intelligence, Compliance");
        mentorProfile17.setMentoringPhilosophy("Hands-on security learning through real-world scenarios");
        mentorProfile17.setCommunicationStyle("Technical and detail oriented");
        mentorProfile17.setCity("Lausanne");
        mentorProfile17.setLanguage("Arabic, French, English, German");
        mentorProfile17.setAge(42);
        mentorProfile17.setAdditionalInfo("Chief Security Officer with extensive penetration testing background");
        mentorProfile17 = mentorProfileRepository.save(mentorProfile17);
    }
    public List<User> getAllMentees() {
        return userRepository.findByRole(User.UserRole.MENTEE);
    }

    public List<User> getAllMentors() {
        return userRepository.findByRole(User.UserRole.MENTOR);
    }
}