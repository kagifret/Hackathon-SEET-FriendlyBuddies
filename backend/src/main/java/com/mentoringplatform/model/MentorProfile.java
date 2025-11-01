package com.mentoringplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_profiles")
public class MentorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @NotNull
    private User user;

    @NotNull
    private String academicField;

    @Column(length = 1000)
    @NotNull
    private String areasOfExpertise;

    @Column(length = 1000)
    @NotNull
    private String mentoringPhilosophy;

    @NotNull
    private String communicationStyle;

    private String city;
    private String language;
    private Integer age;

    @Column(length = 500)
    private String additionalInfo;

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
    public MentorProfile() {}

    public MentorProfile(User user, String academicField, String areasOfExpertise, String mentoringPhilosophy, String communicationStyle) {
        this.user = user;
        this.academicField = academicField;
        this.areasOfExpertise = areasOfExpertise;
        this.mentoringPhilosophy = mentoringPhilosophy;
        this.communicationStyle = communicationStyle;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAcademicField() { return academicField; }
    public void setAcademicField(String academicField) { this.academicField = academicField; }

    public String getAreasOfExpertise() { return areasOfExpertise; }
    public void setAreasOfExpertise(String areasOfExpertise) { this.areasOfExpertise = areasOfExpertise; }

    public String getMentoringPhilosophy() { return mentoringPhilosophy; }
    public void setMentoringPhilosophy(String mentoringPhilosophy) { this.mentoringPhilosophy = mentoringPhilosophy; }

    public String getCommunicationStyle() { return communicationStyle; }
    public void setCommunicationStyle(String communicationStyle) { this.communicationStyle = communicationStyle; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}