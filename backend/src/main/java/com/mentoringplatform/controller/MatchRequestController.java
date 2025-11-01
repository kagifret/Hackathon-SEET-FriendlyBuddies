package com.mentoringplatform.controller;

import com.mentoringplatform.model.MatchRequest;
import com.mentoringplatform.service.MatchRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://matching-tan.vercel.app"})
public class MatchRequestController {

    @Autowired
    private MatchRequestService matchRequestService;

    @GetMapping("/incoming/{mentorId}")
    public ResponseEntity<?> getIncomingRequests(@PathVariable Long mentorId) {
        try {
            List<MatchRequestService.MatchRequestWithMenteeInfo> requests = matchRequestService.getIncomingRequests(mentorId);
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{requestId}/respond")
    public ResponseEntity<?> respondToRequest(@PathVariable Long requestId, @RequestBody Map<String, Boolean> request) {
        try {
            boolean accept = request.get("accept");
            MatchRequest updatedRequest = matchRequestService.respondToRequest(requestId, accept);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMatches(@PathVariable Long userId) {
        try {
            List<MatchRequest> matches = matchRequestService.getUserMatches(userId);
            return ResponseEntity.ok(matches);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getMatchRequest(@PathVariable Long requestId) {
        try {
            MatchRequest matchRequest = matchRequestService.getMatchRequest(requestId);
            return ResponseEntity.ok(matchRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{requestId}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Long requestId, @RequestBody Map<String, Object> feedback) {
        try {
            Long userId = Long.valueOf(feedback.get("userId").toString());
            Integer rating = Integer.valueOf(feedback.get("rating").toString());
            Boolean continueDecision = Boolean.valueOf(feedback.get("continueDecision").toString());
            
            MatchRequest updatedRequest = matchRequestService.submitFeedback(requestId, userId, rating, continueDecision);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}