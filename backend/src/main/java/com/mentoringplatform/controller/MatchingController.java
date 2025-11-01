package com.mentoringplatform.controller;

import com.mentoringplatform.model.MatchRequest;
import com.mentoringplatform.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://matching-tan.vercel.app"})
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    @GetMapping("/candidates/{menteeId}")
    public ResponseEntity<?> getPotentialMatches(@PathVariable Long menteeId) {
        try {
            List<MatchingService.MatchCandidate> candidates = matchingService.findPotentialMatches(menteeId);
            return ResponseEntity.ok(candidates);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all-mentors/{menteeId}")
    public ResponseEntity<?> getAllMentors(@PathVariable Long menteeId) {
        try {
            List<MatchingService.MatchCandidate> candidates = matchingService.findAllMentors(menteeId);
            return ResponseEntity.ok(candidates);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NEW: Pre-computed matching endpoints
    @GetMapping("/pre-computed/{menteeId}")
    public ResponseEntity<?> getPreComputedMatches(@PathVariable Long menteeId) {
        try {
            List<MatchingService.MatchCandidate> candidates = matchingService.getPreComputedMatches(menteeId);
            return ResponseEntity.ok(candidates);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/pre-computed-all/{menteeId}")
    public ResponseEntity<?> getAllPreComputedMatches(@PathVariable Long menteeId) {
        try {
            List<MatchingService.MatchCandidate> candidates = matchingService.getAllPreComputedMatches(menteeId);
            return ResponseEntity.ok(candidates);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get detailed match information for a specific mentor (shows identifying information)
    @GetMapping("/mentors")
    public ResponseEntity<?> getAllMentorsSimple() {
        try {
            List<MatchingService.MatchCandidate> mentors = matchingService.getAllMentorsSimple();
            return ResponseEntity.ok(mentors);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/express-interest")
    public ResponseEntity<?> expressInterest(@RequestBody Map<String, Long> request) {
        try {
            Long menteeId = request.get("menteeId");
            Long mentorId = request.get("mentorId");
            MatchRequest matchRequest = matchingService.expressInterest(menteeId, mentorId);
            return ResponseEntity.ok(matchRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}