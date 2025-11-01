package com.mentoringplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "potential_matches")
public class PotentialMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentee_id", nullable = false)
    private MenteeProfile mentee;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;
    
    @Column(name = "compatibility_score")
    private Double compatibilityScore;
    
    @Column(name = "is_compatible", nullable = false)
    private Boolean isCompatible;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PotentialMatch() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PotentialMatch(MenteeProfile mentee, MentorProfile mentor, Boolean isCompatible, Double compatibilityScore) {
        this();
        this.mentee = mentee;
        this.mentor = mentor;
        this.isCompatible = isCompatible;
        this.compatibilityScore = compatibilityScore;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public MenteeProfile getMentee() {
        return mentee;
    }
    
    public void setMentee(MenteeProfile mentee) {
        this.mentee = mentee;
    }
    
    public MentorProfile getMentor() {
        return mentor;
    }
    
    public void setMentor(MentorProfile mentor) {
        this.mentor = mentor;
    }
    
    public Double getCompatibilityScore() {
        return compatibilityScore;
    }
    
    public void setCompatibilityScore(Double compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }
    
    public Boolean getIsCompatible() {
        return isCompatible;
    }
    
    public void setIsCompatible(Boolean isCompatible) {
        this.isCompatible = isCompatible;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}