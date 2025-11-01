package com.mentoringplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "manual_constraints")
public class ManualConstraint {
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
    private ConstraintType type;

    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public ManualConstraint() {}

    public ManualConstraint(User mentee, User mentor, ConstraintType type, String reason, User createdBy) {
        this.mentee = mentee;
        this.mentor = mentor;
        this.type = type;
        this.reason = reason;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMentee() { return mentee; }
    public void setMentee(User mentee) { this.mentee = mentee; }

    public User getMentor() { return mentor; }
    public void setMentor(User mentor) { this.mentor = mentor; }

    public ConstraintType getType() { return type; }
    public void setType(ConstraintType type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public enum ConstraintType {
        MUST_NOT_MATCH,
        MUST_MATCH
    }
}