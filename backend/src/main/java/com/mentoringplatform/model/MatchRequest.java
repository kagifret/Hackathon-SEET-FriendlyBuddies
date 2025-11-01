package com.mentoringplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_requests")
public class MatchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mentee_id")
    @NotNull
    private User mentee;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    @NotNull
    private User mentor;

    @Enumerated(EnumType.STRING)
    @NotNull
    private MatchStatus status;

    // 70% of final score - algorithmic match
    private Double initialMatchScore;

    // 15% of final score - chat engagement
    private Double chatScore;

    // 15% of final score - user feedback
    private Double feedbackScore;

    // Final computed score (70/15/15)
    private Double finalScore;

    // User feedback
    private Integer menteeRating; // 1-5 stars
    private Integer mentorRating; // 1-5 stars
    private Boolean menteeFinalDecision; // wants to continue
    private Boolean mentorFinalDecision; // wants to continue

    // Admin decision
    private Boolean adminApproved;
    private String adminNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public MatchRequest() {}

    public MatchRequest(User mentee, User mentor, MatchStatus status, Double initialMatchScore) {
        this.mentee = mentee;
        this.mentor = mentor;
        this.status = status;
        this.initialMatchScore = initialMatchScore;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMentee() { return mentee; }
    public void setMentee(User mentee) { this.mentee = mentee; }

    public User getMentor() { return mentor; }
    public void setMentor(User mentor) { this.mentor = mentor; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public Double getInitialMatchScore() { return initialMatchScore; }
    public void setInitialMatchScore(Double initialMatchScore) { this.initialMatchScore = initialMatchScore; }

    public Double getChatScore() { return chatScore; }
    public void setChatScore(Double chatScore) { this.chatScore = chatScore; }

    public Double getFeedbackScore() { return feedbackScore; }
    public void setFeedbackScore(Double feedbackScore) { this.feedbackScore = feedbackScore; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public Integer getMenteeRating() { return menteeRating; }
    public void setMenteeRating(Integer menteeRating) { this.menteeRating = menteeRating; }

    public Integer getMentorRating() { return mentorRating; }
    public void setMentorRating(Integer mentorRating) { this.mentorRating = mentorRating; }

    public Boolean getMenteeFinalDecision() { return menteeFinalDecision; }
    public void setMenteeFinalDecision(Boolean menteeFinalDecision) { this.menteeFinalDecision = menteeFinalDecision; }

    public Boolean getMentorFinalDecision() { return mentorFinalDecision; }
    public void setMentorFinalDecision(Boolean mentorFinalDecision) { this.mentorFinalDecision = mentorFinalDecision; }

    public Boolean getAdminApproved() { return adminApproved; }
    public void setAdminApproved(Boolean adminApproved) { this.adminApproved = adminApproved; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum MatchStatus {
        MENTEE_INTERESTED,
        MENTOR_ACCEPTED,
        MENTOR_REJECTED,
        ADMIN_APPROVED,
        ADMIN_REJECTED
    }
}