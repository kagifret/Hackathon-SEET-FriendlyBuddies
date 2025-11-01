package com.mentoringplatform.service;

import com.mentoringplatform.model.*;
import com.mentoringplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MatchRequestService {

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenteeProfileRepository menteeProfileRepository;

    public List<MatchRequestWithMenteeInfo> getIncomingRequests(Long mentorId) {
        User mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> new RuntimeException("Mentor not found"));

        List<MatchRequest> requests = matchRequestRepository.findByMentorAndStatus(mentor, MatchRequest.MatchStatus.MENTEE_INTERESTED);

        return requests.stream()
            .map(request -> {
                // Try to get mentee profile, but handle case where it doesn't exist (new users)
                MenteeProfile menteeProfile = menteeProfileRepository.findByUser(request.getMentee()).orElse(null);
                
                String academicField = "Not specified";
                String mentorshipGoals = "Profile not completed yet";
                
                if (menteeProfile != null) {
                    academicField = menteeProfile.getAcademicField();
                    mentorshipGoals = menteeProfile.getMentorshipGoals();
                }
                
                return new MatchRequestWithMenteeInfo(
                    request.getId(),
                    request.getMentee().getId(),
                    academicField,
                    mentorshipGoals,
                    request.getInitialMatchScore(),
                    request.getCreatedAt()
                );
            })
            .toList();
    }

    public MatchRequest respondToRequest(Long requestId, boolean accept) {
        MatchRequest request = matchRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Match request not found"));

        if (accept) {
            request.setStatus(MatchRequest.MatchStatus.MENTOR_ACCEPTED);
        } else {
            request.setStatus(MatchRequest.MatchStatus.MENTOR_REJECTED);
        }

        return matchRequestRepository.save(request);
    }

    public List<MatchRequest> getUserMatches(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Get matches where user is either mentee or mentor and status is MENTOR_ACCEPTED or ADMIN_APPROVED
        return matchRequestRepository.findByMenteeOrMentorAndStatusIn(
            user, user, 
            List.of(MatchRequest.MatchStatus.MENTOR_ACCEPTED, MatchRequest.MatchStatus.ADMIN_APPROVED)
        );
    }

    public MatchRequest getMatchRequest(Long requestId) {
        return matchRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Match request not found"));
    }

    public MatchRequest submitFeedback(Long requestId, Long userId, Integer rating, Boolean continueDecision) {
        MatchRequest matchRequest = matchRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Match request not found"));

        // Determine if user is mentee or mentor
        boolean isMentee = matchRequest.getMentee().getId().equals(userId);
        boolean isMentor = matchRequest.getMentor().getId().equals(userId);

        if (!isMentee && !isMentor) {
            throw new RuntimeException("User is not part of this match");
        }

        // Update feedback based on user role
        if (isMentee) {
            matchRequest.setMenteeRating(rating);
            matchRequest.setMenteeFinalDecision(continueDecision);
        } else {
            matchRequest.setMentorRating(rating);
            matchRequest.setMentorFinalDecision(continueDecision);
        }

        // Calculate feedback score (15% of final score)
        updateFeedbackScore(matchRequest);

        // Calculate final score if both users have provided feedback
        if (matchRequest.getMenteeRating() != null && matchRequest.getMentorRating() != null) {
            calculateFinalScore(matchRequest);
        }

        return matchRequestRepository.save(matchRequest);
    }

    private void updateFeedbackScore(MatchRequest matchRequest) {
        Integer menteeRating = matchRequest.getMenteeRating();
        Integer mentorRating = matchRequest.getMentorRating();
        Boolean menteeContinue = matchRequest.getMenteeFinalDecision();
        Boolean mentorContinue = matchRequest.getMentorFinalDecision();

        double feedbackScore = 0.0;
        int ratingsCount = 0;

        // Average the ratings (1-5 scale converted to 0-100)
        if (menteeRating != null) {
            feedbackScore += (menteeRating * 20.0); // Convert 1-5 to 20-100
            ratingsCount++;
        }
        if (mentorRating != null) {
            feedbackScore += (mentorRating * 20.0);
            ratingsCount++;
        }

        if (ratingsCount > 0) {
            feedbackScore = feedbackScore / ratingsCount;
        }

        // Apply continue/discontinue decision weights
        if (Boolean.FALSE.equals(menteeContinue)) {
            feedbackScore *= 0.5; // Reduce score if wanting to discontinue
        }
        if (Boolean.FALSE.equals(mentorContinue)) {
            feedbackScore *= 0.5;
        }

        // If both want to discontinue, score goes to 0
        if (Boolean.FALSE.equals(menteeContinue) && Boolean.FALSE.equals(mentorContinue)) {
            feedbackScore = 0.0;
        }

        matchRequest.setFeedbackScore(feedbackScore);
    }

    private void calculateFinalScore(MatchRequest matchRequest) {
        Double initialScore = matchRequest.getInitialMatchScore() != null ? matchRequest.getInitialMatchScore() : 0.0;
        Double chatScore = matchRequest.getChatScore() != null ? matchRequest.getChatScore() : 0.0;
        Double feedbackScore = matchRequest.getFeedbackScore() != null ? matchRequest.getFeedbackScore() : 0.0;

        // 70% algorithmic + 15% chat + 15% feedback
        double finalScore = (initialScore * 0.70) + (chatScore * 0.15) + (feedbackScore * 0.15);
        
        matchRequest.setFinalScore(finalScore);
    }

    public static class MatchRequestWithMenteeInfo {
        private Long requestId;
        private Long menteeId;
        private String academicField;
        private String mentorshipGoals;
        private Double matchScore;
        private java.time.LocalDateTime createdAt;

        public MatchRequestWithMenteeInfo(Long requestId, Long menteeId, String academicField, String mentorshipGoals, Double matchScore, java.time.LocalDateTime createdAt) {
            this.requestId = requestId;
            this.menteeId = menteeId;
            this.academicField = academicField;
            this.mentorshipGoals = mentorshipGoals;
            this.matchScore = matchScore;
            this.createdAt = createdAt;
        }

        // Getters and setters
        public Long getRequestId() { return requestId; }
        public void setRequestId(Long requestId) { this.requestId = requestId; }

        public Long getMenteeId() { return menteeId; }
        public void setMenteeId(Long menteeId) { this.menteeId = menteeId; }

        public String getAcademicField() { return academicField; }
        public void setAcademicField(String academicField) { this.academicField = academicField; }

        public String getMentorshipGoals() { return mentorshipGoals; }
        public void setMentorshipGoals(String mentorshipGoals) { this.mentorshipGoals = mentorshipGoals; }

        public Double getMatchScore() { return matchScore; }
        public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}