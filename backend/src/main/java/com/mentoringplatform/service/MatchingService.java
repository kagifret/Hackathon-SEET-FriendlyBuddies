package com.mentoringplatform.service;

import com.mentoringplatform.model.*;
import com.mentoringplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MentorProfileRepository mentorProfileRepository;

    @Autowired
    private MenteeProfileRepository menteeProfileRepository;

    @Autowired
    private ProgramSettingsRepository programSettingsRepository;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private ManualConstraintRepository manualConstraintRepository;

    @Autowired
    private PotentialMatchRepository potentialMatchRepository;

    /**
     * Find potential matches using FULL algorithmic filtering
     * This method applies all hard filters including academic field and location compatibility
     * Provides the most curated and compatible matches
     */
    public List<MatchCandidate> findPotentialMatches(Long menteeId) {
        User mentee = userRepository.findById(menteeId)
            .orElseThrow(() -> new RuntimeException("Mentee not found"));

        MenteeProfile menteeProfile = menteeProfileRepository.findByUser(mentee)
            .orElseThrow(() -> new RuntimeException("Mentee profile not found"));

        ProgramSettings settings = programSettingsRepository.findFirstByOrderByCreatedAtDesc();
        if (settings == null || !settings.getMatchingSeasonActive()) {
            throw new RuntimeException("Matching season is not active");
        }

        // Check how many matches the mentee has already made
        long existingMatches = matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.MENTEE_INTERESTED).size();
        if (existingMatches >= settings.getMaxLikesPerMentee()) {
            throw new RuntimeException("Maximum likes reached");
        }

        // Get all mentor profiles
        List<MentorProfile> allMentors = mentorProfileRepository.findAll();

        List<MatchCandidate> candidates = new ArrayList<>();


        for (MentorProfile mentorProfile : allMentors) {
            User mentor = mentorProfile.getUser();

            // Skip if already matched or requested
            if (matchRequestRepository.existsByMenteeAndMentor(mentee, mentor)) {
                continue;
            }

            // Check manual constraints
            List<ManualConstraint> constraints = manualConstraintRepository
                .findByMenteeAndMentorAndType(mentee, mentor, ManualConstraint.ConstraintType.MUST_NOT_MATCH);
            if (!constraints.isEmpty()) {
                continue;
            }

            // Apply hard filters
            if (!passesHardFilters(menteeProfile, mentorProfile, settings)) {
                continue;
            }

            // Calculate initial match score (70% of final score)
            double score = calculateInitialMatchScore(menteeProfile, mentorProfile);

            candidates.add(new MatchCandidate(
                mentor.getId(),
                mentorProfile.getAcademicField(),
                mentorProfile.getAreasOfExpertise(),
                mentorProfile.getMentoringPhilosophy(),
                score
            ));
        }

        // Sort by score (highest first)
        return candidates.stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    /**
     * New method: Show ALL compatible mentors to mentees with essential hard filtering
     * This gives mentees visibility of all mentors who meet fundamental compatibility requirements
     * but without advanced algorithmic filtering
     */
    public List<MatchCandidate> findAllMentors(Long menteeId) {
        User mentee = userRepository.findById(menteeId)
            .orElseThrow(() -> new RuntimeException("Mentee not found"));

        // Try to get mentee profile, but handle case where it doesn't exist (new users)
        MenteeProfile menteeProfile = menteeProfileRepository.findByUser(mentee).orElse(null);
        
        // If no mentee profile exists, fall back to simple mentor list
        if (menteeProfile == null) {
            return getAllMentorsSimple();
        }

        // Get program settings for hard filter parameters
        ProgramSettings settings = programSettingsRepository.findFirstByOrderByCreatedAtDesc();
        if (settings == null) {
            settings = new ProgramSettings(10, 20, 5, true); // Default settings
        }

        // Get all mentor profiles
        List<MentorProfile> allMentors = mentorProfileRepository.findAll();

        List<MatchCandidate> candidates = new ArrayList<>();

        for (MentorProfile mentorProfile : allMentors) {
            User mentor = mentorProfile.getUser();

            // Skip if already matched or requested
            if (matchRequestRepository.existsByMenteeAndMentor(mentee, mentor)) {
                continue;
            }

            // Check manual constraints
            List<ManualConstraint> constraints = manualConstraintRepository
                .findByMenteeAndMentorAndType(mentee, mentor, ManualConstraint.ConstraintType.MUST_NOT_MATCH);
            if (!constraints.isEmpty()) {
                continue;
            }

            // Apply ESSENTIAL hard filters only (safety & basic compatibility)
            if (!passesEssentialHardFilters(menteeProfile, mentorProfile, settings)) {
                continue;
            }

            // Calculate match score for ranking purposes
            double score = calculateInitialMatchScore(menteeProfile, mentorProfile);

            candidates.add(new MatchCandidate(
                mentor.getId(),
                mentorProfile.getAcademicField(),
                mentorProfile.getAreasOfExpertise(),
                mentorProfile.getMentoringPhilosophy(),
                score,
                mentor.getFirstName() + " " + mentor.getLastName(),
                mentor.getCity(),
                mentorProfile.getLanguage(),
                mentorProfile.getAge()
            ));
        }

        // Sort by score (highest first) for better user experience
        return candidates.stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    /**
     * Simple method to get all mentors without requiring a mentee profile
     * Used for new users who haven't completed their profile yet
     */
    public List<MatchCandidate> getAllMentorsSimple() {
        // Get all mentor profiles
        List<MentorProfile> allMentors = mentorProfileRepository.findAll();
        List<MatchCandidate> candidates = new ArrayList<>();

        for (MentorProfile mentorProfile : allMentors) {
            User mentor = mentorProfile.getUser();

            candidates.add(new MatchCandidate(
                mentor.getId(),
                mentorProfile.getAcademicField(),
                mentorProfile.getAreasOfExpertise(),
                mentorProfile.getMentoringPhilosophy(),
                0.0, // No score calculation since we don't have mentee profile
                mentor.getFirstName() + " " + mentor.getLastName(),
                mentor.getCity(),
                mentorProfile.getLanguage(),
                mentorProfile.getAge()
            ));
        }

        return candidates;
    }

    private boolean passesHardFilters(MenteeProfile menteeProfile, MentorProfile mentorProfile, ProgramSettings settings) {
        // MANDATORY CRITERION 1: Gender Preference Filter (Highest Priority)
        User mentee = menteeProfile.getUser();
        User mentor = mentorProfile.getUser();
        
        if (mentee.getMentorGenderPreference() != null && mentee.getMentorGenderPreference() != User.Gender.ANY) {
            // Check if mentor's gender matches the mentee's preference
            // Note: In the frontend, "SAME" is sent as the mentee's own gender
            if (mentor.getGender() != null && !mentor.getGender().equals(mentee.getMentorGenderPreference())) {
                return false; // Hard rejection - gender preference not met
            }
        }

        // MANDATORY CRITERION 2: Academic Field Compatibility Filter
        if (!hasAcademicFieldCompatibility(mentee, mentor)) {
            return false; // Hard rejection - incompatible academic fields
        }

        // MANDATORY CRITERION 3: Language Compatibility Filter
        if (!hasLanguageCompatibility(mentee, mentor)) {
            return false; // Hard rejection - no shared languages
        }

        // MANDATORY CRITERION 4: City/Location Filter
        if (!hasCityCompatibility(mentee, mentor)) {
            return false; // Hard rejection - different cities
        }

        // Language filter (legacy - keeping for backward compatibility)
        if (menteeProfile.getLanguage() != null && mentorProfile.getLanguage() != null) {
            if (!menteeProfile.getLanguage().equalsIgnoreCase(mentorProfile.getLanguage())) {
                return false;
            }
        }

        // Age difference filter
        if (menteeProfile.getAge() != null && mentorProfile.getAge() != null) {
            int ageDiff = Math.abs(menteeProfile.getAge() - mentorProfile.getAge());
            if (ageDiff < settings.getMinAgeDifference() || ageDiff > settings.getMaxAgeDifference()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Essential hard filters that ensure basic compatibility and safety
     * These are applied even in "show all mentors" mode
     * INTENTIONALLY VERY LENIENT to allow maximum mentor visibility
     */
    private boolean passesEssentialHardFilters(MenteeProfile menteeProfile, MentorProfile mentorProfile, ProgramSettings settings) {
        User mentee = menteeProfile.getUser();
        User mentor = mentorProfile.getUser();

        System.out.println("🔍 Checking essential filters for " + mentee.getFirstName() + " → " + mentor.getFirstName());

        // ESSENTIAL FILTER 1: Gender Preference (Only if strictly specified)
        if (mentee.getMentorGenderPreference() != null && mentee.getMentorGenderPreference() != User.Gender.ANY) {
            if (mentor.getGender() != null && !mentor.getGender().equals(mentee.getMentorGenderPreference())) {
                System.out.println("❌ Gender filter failed: " + mentee.getFirstName() + " wants " + mentee.getMentorGenderPreference() + " but " + mentor.getFirstName() + " is " + mentor.getGender());
                return false; // Hard rejection - gender preference not met
            }
        }

        // SKIPPING AGE FILTER - Let mentees decide for themselves
        // SKIPPING LANGUAGE FILTER - Let mentees see all mentors
        // SKIPPING ACADEMIC FIELD FILTER - Allow cross-field mentoring
        // SKIPPING CITY FILTER - Allow remote mentoring

        System.out.println("✅ All essential filters passed for " + mentee.getFirstName() + " → " + mentor.getFirstName());
        return true;
    }

    private double calculateInitialMatchScore(MenteeProfile menteeProfile, MentorProfile mentorProfile) {
        double score = 0.0;

        // Academic field match (30% weight)
        if (menteeProfile.getAcademicField().equalsIgnoreCase(mentorProfile.getAcademicField())) {
            score += 30.0;
        }

        // Goals vs Expertise overlap (50% weight) - simplified text matching
        String goals = menteeProfile.getMentorshipGoals().toLowerCase();
        String expertise = mentorProfile.getAreasOfExpertise().toLowerCase();
        
        // Simple keyword matching - in production, use more sophisticated NLP
        String[] goalWords = goals.split("\\s+");
        String[] expertiseWords = expertise.split("\\s+");
        
        int matches = 0;
        for (String goalWord : goalWords) {
            if (goalWord.length() > 3) { // ignore short words
                for (String expertiseWord : expertiseWords) {
                    if (goalWord.equals(expertiseWord)) {
                        matches++;
                    }
                }
            }
        }
        
        double overlapScore = Math.min(50.0, (matches * 10.0));
        score += overlapScore;

        // Communication style match (20% weight)
        if (menteeProfile.getCommunicationStyle().equals(mentorProfile.getCommunicationStyle())) {
            score += 20.0;
        }

        return score;
    }

    public MatchRequest expressInterest(Long menteeId, Long mentorId) {
        User mentee = userRepository.findById(menteeId)
            .orElseThrow(() -> new RuntimeException("Mentee not found"));
        User mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> new RuntimeException("Mentor not found"));

        // Only check for active (non-rejected) match requests
        List<MatchRequest.MatchStatus> activeStatuses = List.of(
            MatchRequest.MatchStatus.MENTEE_INTERESTED, 
            MatchRequest.MatchStatus.MENTOR_ACCEPTED, 
            MatchRequest.MatchStatus.ADMIN_APPROVED
        );
        
        if (matchRequestRepository.existsByMenteeAndMentorAndStatusIn(mentee, mentor, activeStatuses)) {
            throw new RuntimeException("Interest already expressed");
        }

        // Try to get mentee and mentor profiles, but allow for missing mentee profile (new users)
        MenteeProfile menteeProfile = menteeProfileRepository.findByUser(mentee).orElse(null);
        MentorProfile mentorProfile = mentorProfileRepository.findByUser(mentor)
            .orElseThrow(() -> new RuntimeException("Mentor profile not found"));

        // Calculate initial score if mentee profile exists, otherwise use 0.0
        double initialScore = 0.0;
        if (menteeProfile != null) {
            initialScore = calculateInitialMatchScore(menteeProfile, mentorProfile);
        }

        MatchRequest matchRequest = new MatchRequest(mentee, mentor, MatchRequest.MatchStatus.MENTEE_INTERESTED, initialScore);
        return matchRequestRepository.save(matchRequest);
    }

    /**
     * MANDATORY CRITERION 2: Academic Field Compatibility
     * Checks if mentee and mentor have compatible academic fields
     */
    private boolean hasAcademicFieldCompatibility(User mentee, User mentor) {
        if (mentee.getAcademicField() == null || mentor.getAcademicField() == null) {
            return false; // Both must have academic fields specified
        }
        
        String menteeField = mentee.getAcademicField().toLowerCase().trim();
        String mentorField = mentor.getAcademicField().toLowerCase().trim();
        
        // Exact match
        if (menteeField.equals(mentorField)) {
            return true;
        }
        
        // Check for related fields - broader categories
        return areRelatedAcademicFields(menteeField, mentorField);
    }

    /**
     * Helper method to determine if two academic fields are related
     */
    private boolean areRelatedAcademicFields(String field1, String field2) {
        // Define related field groups for more flexible matching
        String[][] relatedGroups = {
            {"computer science", "information technology", "software engineering", "data science", "artificial intelligence", "cybersecurity"},
            {"biology", "biomedical sciences", "medicine", "biotechnology"},
            {"chemistry", "chemical engineering"},
            {"physics", "mathematics", "statistics"},
            {"mechanical engineering", "electrical engineering", "civil engineering", "engineering"},
            {"business administration", "economics", "finance", "marketing", "management"},
            {"psychology", "sociology"},
            {"literature", "languages", "philosophy"},
            {"environmental science", "geology", "geography"}
        };
        
        for (String[] group : relatedGroups) {
            boolean field1InGroup = false;
            boolean field2InGroup = false;
            
            for (String field : group) {
                if (field1.contains(field) || field.contains(field1)) {
                    field1InGroup = true;
                }
                if (field2.contains(field) || field.contains(field2)) {
                    field2InGroup = true;
                }
            }
            
            if (field1InGroup && field2InGroup) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * MANDATORY CRITERION 3: Language Compatibility
     * Checks if mentee and mentor share at least one common language
     */
    private boolean hasLanguageCompatibility(User mentee, User mentor) {
        if (mentee.getLanguages() == null || mentor.getLanguages() == null) {
            return false; // Both must have languages specified
        }
        
        try {
            Set<String> menteeLanguages = parseLanguagesFromJson(mentee.getLanguages());
            Set<String> mentorLanguages = parseLanguagesFromJson(mentor.getLanguages());
            
            // Check for at least one common language
            for (String language : menteeLanguages) {
                if (mentorLanguages.contains(language)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // If JSON parsing fails, fall back to no compatibility
            return false;
        }
    }

    /**
     * MANDATORY CRITERION 4: City/Location Compatibility
     * Checks if mentee and mentor are in the same city
     */
    private boolean hasCityCompatibility(User mentee, User mentor) {
        if (mentee.getCity() == null || mentor.getCity() == null) {
            return false; // Both must have cities specified
        }
        
        return mentee.getCity().equalsIgnoreCase(mentor.getCity());
    }

    /**
     * Helper method to parse language names from JSON format
     * Expected format: [{"language":"German","proficiency":"C1"},{"language":"English","proficiency":"B2"}]
     */
    private Set<String> parseLanguagesFromJson(String languagesJson) {
        Set<String> languages = new HashSet<>();
        if (languagesJson == null || languagesJson.trim().isEmpty()) {
            return languages;
        }
        
        try {
            // Simple JSON parsing for language names
            // This is a basic implementation - in production, use a proper JSON library
            languagesJson = languagesJson.trim();
            if (languagesJson.startsWith("[") && languagesJson.endsWith("]")) {
                languagesJson = languagesJson.substring(1, languagesJson.length() - 1);
                String[] entries = languagesJson.split("\\},\\{");
                
                for (String entry : entries) {
                    entry = entry.replace("{", "").replace("}", "");
                    String[] pairs = entry.split(",");
                    
                    for (String pair : pairs) {
                        if (pair.contains("\"language\"")) {
                            String[] keyValue = pair.split(":");
                            if (keyValue.length == 2) {
                                String language = keyValue[1].replace("\"", "").trim();
                                languages.add(language);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, return empty set
        }
        
        return languages;
    }

    public static class MatchCandidate {
        private Long mentorId;
        private String academicField;
        private String areasOfExpertise;
        private String mentoringPhilosophy;
        private double score;
        private String mentorName;
        private String city;
        private String languages;
        private int age;

        public MatchCandidate(Long mentorId, String academicField, String areasOfExpertise, String mentoringPhilosophy, double score) {
            this.mentorId = mentorId;
            this.academicField = academicField;
            this.areasOfExpertise = areasOfExpertise;
            this.mentoringPhilosophy = mentoringPhilosophy;
            this.score = score;
        }

        public MatchCandidate(Long mentorId, String academicField, String areasOfExpertise, String mentoringPhilosophy, double score, String mentorName, String city, String languages, int age) {
            this.mentorId = mentorId;
            this.academicField = academicField;
            this.areasOfExpertise = areasOfExpertise;
            this.mentoringPhilosophy = mentoringPhilosophy;
            this.score = score;
            this.mentorName = mentorName;
            this.city = city;
            this.languages = languages;
            this.age = age;
        }

        // Getters and setters
        public Long getMentorId() { return mentorId; }
        public void setMentorId(Long mentorId) { this.mentorId = mentorId; }

        public String getAcademicField() { return academicField; }
        public void setAcademicField(String academicField) { this.academicField = academicField; }

        public String getAreasOfExpertise() { return areasOfExpertise; }
        public void setAreasOfExpertise(String areasOfExpertise) { this.areasOfExpertise = areasOfExpertise; }

        public String getMentoringPhilosophy() { return mentoringPhilosophy; }
        public void setMentoringPhilosophy(String mentoringPhilosophy) { this.mentoringPhilosophy = mentoringPhilosophy; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getMentorName() { return mentorName; }
        public void setMentorName(String mentorName) { this.mentorName = mentorName; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getLanguages() { return languages; }
        public void setLanguages(String languages) { this.languages = languages; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    // ===============================
    // PRE-COMPUTED MATCHING METHODS
    // ===============================

    /**
     * Pre-compute all possible mentor-mentee combinations and store them in the database
     * This method generates all combinations and applies hard filters once
     */
    public void preComputeAllMatches() {
        System.out.println("🔄 Starting pre-computation of all mentor-mentee matches...");
        
        // Clear existing pre-computed matches
        potentialMatchRepository.deleteAll();
        
        // Get all mentees and mentors
        List<MenteeProfile> allMentees = menteeProfileRepository.findAll();
        List<MentorProfile> allMentors = mentorProfileRepository.findAll();
        
        ProgramSettings settings = programSettingsRepository.findFirstByOrderByCreatedAtDesc();
        if (settings == null) {
            System.out.println("❌ No program settings found, cannot pre-compute matches");
            return;
        }
        
        int totalCombinations = allMentees.size() * allMentors.size();
        int compatibleMatches = 0;
        int processedCombinations = 0;
        
        System.out.println("📊 Processing " + totalCombinations + " mentor-mentee combinations...");
        
        for (MenteeProfile mentee : allMentees) {
            for (MentorProfile mentor : allMentors) {
                processedCombinations++;
                
                // Check if this combination passes essential hard filters
                boolean isCompatible = passesEssentialHardFilters(mentee, mentor, settings);
                
                // Calculate compatibility score
                double score = 0.0;
                if (isCompatible) {
                    score = calculateInitialMatchScore(mentee, mentor);
                    compatibleMatches++;
                }
                
                // Store the pre-computed match
                PotentialMatch potentialMatch = new PotentialMatch(
                    mentee, 
                    mentor, 
                    isCompatible, 
                    score
                );
                
                potentialMatchRepository.save(potentialMatch);
                
                // Progress logging every 10% 
                if (processedCombinations % (totalCombinations / 10 + 1) == 0) {
                    int progress = (processedCombinations * 100) / totalCombinations;
                    System.out.println("⏳ Progress: " + progress + "% (" + processedCombinations + "/" + totalCombinations + ")");
                }
            }
        }
        
        System.out.println("✅ Pre-computation completed!");
        System.out.println("📈 Total combinations: " + totalCombinations);
        System.out.println("✔️ Compatible matches: " + compatibleMatches);
        System.out.println("❌ Incompatible matches: " + (totalCombinations - compatibleMatches));
    }

    /**
     * Get pre-computed compatible matches for a mentee
     */
    public List<MatchCandidate> getPreComputedMatches(Long menteeId) {
        List<PotentialMatch> potentialMatches = potentialMatchRepository.findCompatibleMatchesByMenteeId(menteeId);
        
        // Get mentee user to check for existing match requests
        User mentee = userRepository.findById(menteeId)
            .orElseThrow(() -> new RuntimeException("Mentee not found"));
        
        // Get list of mentor IDs that mentee has already expressed interest in (active requests only)
        List<MatchRequest> existingRequests = matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.MENTEE_INTERESTED);
        existingRequests.addAll(matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.MENTOR_ACCEPTED));
        existingRequests.addAll(matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.ADMIN_APPROVED));
        
        Set<Long> excludedMentorIds = existingRequests.stream()
            .map(req -> req.getMentor().getId())
            .collect(Collectors.toSet());
        
        return potentialMatches.stream()
            .filter(match -> !excludedMentorIds.contains(match.getMentor().getUser().getId()))
            .map(this::convertToMatchCandidate)
            .collect(Collectors.toList());
    }

    /**
     * Get ALL pre-computed matches for a mentee (both compatible and incompatible)
     * This is useful for debugging and showing "all mentors" view
     */
    public List<MatchCandidate> getAllPreComputedMatches(Long menteeId) {
        List<PotentialMatch> potentialMatches = potentialMatchRepository.findAllMatchesByMenteeId(menteeId);
        
        // Get mentee user to check for existing match requests
        User mentee = userRepository.findById(menteeId)
            .orElseThrow(() -> new RuntimeException("Mentee not found"));
        
        // Get list of mentor IDs that mentee has already expressed interest in (active requests only)
        List<MatchRequest> existingRequests = matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.MENTEE_INTERESTED);
        existingRequests.addAll(matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.MENTOR_ACCEPTED));
        existingRequests.addAll(matchRequestRepository.findByMenteeAndStatus(mentee, MatchRequest.MatchStatus.ADMIN_APPROVED));
        
        Set<Long> excludedMentorIds = existingRequests.stream()
            .map(req -> req.getMentor().getId())
            .collect(Collectors.toSet());
        
        return potentialMatches.stream()
            .filter(match -> !excludedMentorIds.contains(match.getMentor().getUser().getId()))
            .map(this::convertToMatchCandidate)
            .collect(Collectors.toList());
    }

    /**
     * Get detailed match information for a specific mentor
     * Used when showing full profile details (e.g., in match requests)
     */
    public MatchCandidate getDetailedMatchInfo(Long menteeId, Long mentorId) {
        PotentialMatch potentialMatch = potentialMatchRepository.findByMenteeIdAndMentorId(menteeId, mentorId)
            .orElseThrow(() -> new RuntimeException("No potential match found between mentee " + menteeId + " and mentor " + mentorId));
        
        return convertToMatchCandidateDetailed(potentialMatch);
    }

    /**
     * Refresh matches for a specific mentee (when their profile is updated)
     */
    public void refreshMatchesForMentee(Long menteeId) {
        MenteeProfile mentee = menteeProfileRepository.findById(menteeId)
            .orElseThrow(() -> new RuntimeException("Mentee not found"));
            
        // Delete existing matches for this mentee
        potentialMatchRepository.deleteByMenteeId(menteeId);
        
        // Recompute matches for this mentee
        List<MentorProfile> allMentors = mentorProfileRepository.findAll();
        ProgramSettings settings = programSettingsRepository.findFirstByOrderByCreatedAtDesc();
        
        if (settings == null) {
            return;
        }
        
        for (MentorProfile mentor : allMentors) {
            boolean isCompatible = passesEssentialHardFilters(mentee, mentor, settings);
            double score = isCompatible ? calculateInitialMatchScore(mentee, mentor) : 0.0;
            
            PotentialMatch potentialMatch = new PotentialMatch(mentee, mentor, isCompatible, score);
            potentialMatchRepository.save(potentialMatch);
        }
        
        System.out.println("♻️ Refreshed matches for mentee: " + mentee.getUser().getEmail());
    }

    /**
     * Refresh matches for a specific mentor (when their profile is updated)
     */
    public void refreshMatchesForMentor(Long mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
            .orElseThrow(() -> new RuntimeException("Mentor not found"));
            
        // Delete existing matches for this mentor
        potentialMatchRepository.deleteByMentorId(mentorId);
        
        // Recompute matches for this mentor
        List<MenteeProfile> allMentees = menteeProfileRepository.findAll();
        ProgramSettings settings = programSettingsRepository.findFirstByOrderByCreatedAtDesc();
        
        if (settings == null) {
            return;
        }
        
        for (MenteeProfile mentee : allMentees) {
            boolean isCompatible = passesEssentialHardFilters(mentee, mentor, settings);
            double score = isCompatible ? calculateInitialMatchScore(mentee, mentor) : 0.0;
            
            PotentialMatch potentialMatch = new PotentialMatch(mentee, mentor, isCompatible, score);
            potentialMatchRepository.save(potentialMatch);
        }
        
        System.out.println("♻️ Refreshed matches for mentor: " + mentor.getUser().getEmail());
    }

    /**
     * Convert PotentialMatch to MatchCandidate for bias-free browsing
     * Hides identifying information like name, age, and location
     */
    private MatchCandidate convertToMatchCandidateBiasFree(PotentialMatch potentialMatch) {
        MentorProfile mentor = potentialMatch.getMentor();
        User mentorUser = mentor.getUser();
        
        return new MatchCandidate(
            mentorUser.getId(),
            mentor.getAcademicField(),
            mentor.getAreasOfExpertise(),
            mentor.getMentoringPhilosophy(),
            potentialMatch.getCompatibilityScore()
        );
    }

    /**
     * Convert PotentialMatch to MatchCandidate with full details
     * Used for match requests where mentors need sufficient information to make decisions
     */
    private MatchCandidate convertToMatchCandidateDetailed(PotentialMatch potentialMatch) {
        MentorProfile mentor = potentialMatch.getMentor();
        User mentorUser = mentor.getUser();
        
        return new MatchCandidate(
            mentorUser.getId(),
            mentor.getAcademicField(),
            mentor.getAreasOfExpertise(),
            mentor.getMentoringPhilosophy(),
            potentialMatch.getCompatibilityScore(),
            mentorUser.getFirstName() + " " + mentorUser.getLastName(),
            mentor.getCity(),
            mentor.getLanguage(),
            mentor.getAge()
        );
    }

    /**
     * Convert PotentialMatch to MatchCandidate for API responses
     * Default method for backward compatibility - uses bias-free version
     */
    private MatchCandidate convertToMatchCandidate(PotentialMatch potentialMatch) {
        return convertToMatchCandidateBiasFree(potentialMatch);
    }
}