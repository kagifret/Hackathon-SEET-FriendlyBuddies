package com.mentoringplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "program_settings")
public class ProgramSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer maxLikesPerMentee;

    @NotNull
    private Integer maxAgeDifference;

    @NotNull
    private Integer minAgeDifference;

    @NotNull
    private Boolean matchingSeasonActive;

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
    public ProgramSettings() {}

    public ProgramSettings(Integer maxLikesPerMentee, Integer maxAgeDifference, Integer minAgeDifference, Boolean matchingSeasonActive) {
        this.maxLikesPerMentee = maxLikesPerMentee;
        this.maxAgeDifference = maxAgeDifference;
        this.minAgeDifference = minAgeDifference;
        this.matchingSeasonActive = matchingSeasonActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getMaxLikesPerMentee() { return maxLikesPerMentee; }
    public void setMaxLikesPerMentee(Integer maxLikesPerMentee) { this.maxLikesPerMentee = maxLikesPerMentee; }

    public Integer getMaxAgeDifference() { return maxAgeDifference; }
    public void setMaxAgeDifference(Integer maxAgeDifference) { this.maxAgeDifference = maxAgeDifference; }

    public Integer getMinAgeDifference() { return minAgeDifference; }
    public void setMinAgeDifference(Integer minAgeDifference) { this.minAgeDifference = minAgeDifference; }

    public Boolean getMatchingSeasonActive() { return matchingSeasonActive; }
    public void setMatchingSeasonActive(Boolean matchingSeasonActive) { this.matchingSeasonActive = matchingSeasonActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}