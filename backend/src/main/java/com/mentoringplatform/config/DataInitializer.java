package com.mentoringplatform.config;

import com.mentoringplatform.model.User;
import com.mentoringplatform.model.ProgramSettings;
import com.mentoringplatform.service.AdminService;
import com.mentoringplatform.service.MatchingService;
import com.mentoringplatform.service.MatchingService.MatchCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MatchingService matchingService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🚀 Initializing test data and starting matching process...");
        
        try {
            // Initialize test users and data
            adminService.initializeTestData();
            System.out.println("✅ Test data initialized successfully");
            
            // Activate matching season for automatic matching
            activateMatchingSeason();
            System.out.println("✅ Matching season activated");
            
            // Wait a moment for data to be fully persisted
            Thread.sleep(1000);
            
            // IMPORTANT: Use pre-computed matching system instead of old restrictive filtering
            triggerPreComputedMatching();
            System.out.println("✅ Pre-computed matching system activated successfully");
            
            System.out.println("🎉 Application ready with test data and matches!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void triggerPreComputedMatching() {
        try {
            System.out.println("🔄 Starting pre-computed matching system initialization...");
            
            // Step 1: Pre-compute all possible mentor-mentee combinations
            matchingService.preComputeAllMatches();
            System.out.println("✅ All mentor-mentee combinations pre-computed and stored in database");
            
            // Step 2: Get all mentees and show their available matches
            List<User> mentees = adminService.getAllMentees();
            
            for (User mentee : mentees) {
                try {
                    // Use the new pre-computed system to get matches
                    List<MatchCandidate> matches = matchingService.getAllPreComputedMatches(mentee.getId());
                    System.out.println("📋 Found " + matches.size() + " total pre-computed matches for " + mentee.getFirstName());
                    
                    // Get only compatible matches
                    List<MatchCandidate> compatibleMatches = matchingService.getPreComputedMatches(mentee.getId());
                    System.out.println("✅ Found " + compatibleMatches.size() + " compatible matches for " + mentee.getFirstName());
                    
                    // Auto-create match requests for top compatible matches (optional)
                    int autoMatchCount = Math.min(1, compatibleMatches.size()); // Create 1 auto-request if available
                    for (int i = 0; i < autoMatchCount; i++) {
                        MatchCandidate match = compatibleMatches.get(i);
                        try {
                            matchingService.expressInterest(mentee.getId(), match.getMentorId());
                            System.out.println("💕 Auto-created match request: " + mentee.getFirstName() + " → " + match.getMentorName());
                        } catch (Exception e) {
                            System.out.println("⚠️ Could not create match request: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Could not process pre-computed matching for mentee " + mentee.getFirstName() + ": " + e.getMessage());
                }
            }
            
            System.out.println("🎯 Pre-computed matching system fully operational!");
        } catch (Exception e) {
            System.err.println("❌ Error in pre-computed matching: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void activateMatchingSeason() {
        try {
            // Get current settings or create default ones
            ProgramSettings settings = adminService.getCurrentSettings();
            
            // Activate matching season
            settings.setMatchingSeasonActive(true);
            
            // Update the settings
            adminService.updateSettings(settings);
            
            System.out.println("🌟 Matching season activated successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error activating matching season: " + e.getMessage());
        }
    }
}