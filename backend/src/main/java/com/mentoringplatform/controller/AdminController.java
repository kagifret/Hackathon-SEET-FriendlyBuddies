package com.mentoringplatform.controller;

import com.mentoringplatform.model.MatchRequest;
import com.mentoringplatform.model.ProgramSettings;
import com.mentoringplatform.service.AdminService;
import com.mentoringplatform.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://matching-tan.vercel.app"})
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MatchingService matchingService;

    @GetMapping("/settings")
    public ResponseEntity<ProgramSettings> getCurrentSettings() {
        ProgramSettings settings = adminService.getCurrentSettings();
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/settings")
    public ResponseEntity<ProgramSettings> updateSettings(@RequestBody ProgramSettings settings) {
        ProgramSettings updatedSettings = adminService.updateSettings(settings);
        return ResponseEntity.ok(updatedSettings);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<Map<String, Object>>> getDashboardData() {
        List<Map<String, Object>> matchRequestsWithScoring = adminService.getAllMatchRequestsWithScoring();
        return ResponseEntity.ok(matchRequestsWithScoring);
    }

    @PostMapping("/matches/{matchRequestId}/approve")
    public ResponseEntity<MatchRequest> approveMatch(@PathVariable Long matchRequestId) {
        try {
            MatchRequest approvedMatch = adminService.finalApproveMatch(matchRequestId);
            return ResponseEntity.ok(approvedMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/matches/{matchRequestId}/reject")
    public ResponseEntity<MatchRequest> rejectMatch(@PathVariable Long matchRequestId) {
        try {
            MatchRequest rejectedMatch = adminService.finalRejectMatch(matchRequestId);
            return ResponseEntity.ok(rejectedMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/init-test-data")
    public ResponseEntity<String> initTestData() {
        try {
            adminService.initializeTestData();
            return ResponseEntity.ok("Test data initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing test data: " + e.getMessage());
        }
    }

    @PostMapping("/precompute-matches")
    public ResponseEntity<String> preComputeMatches() {
        try {
            matchingService.preComputeAllMatches();
            return ResponseEntity.ok("All mentor-mentee matches pre-computed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error pre-computing matches: " + e.getMessage());
        }
    }
}