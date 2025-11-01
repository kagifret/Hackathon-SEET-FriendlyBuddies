package com.mentoringplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserRole role;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_gender_preference")
    private Gender mentorGenderPreference;

    private String city;

    @Column(name = "languages", columnDefinition = "TEXT")
    private String languages; // JSON string for language-proficiency pairs

    @Column(name = "academic_field")
    private String academicField;

    @Column(name = "current_course")
    private String currentCourse;

    @Column(name = "previous_background")
    private String previousBackground;

    private String profilePictureUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public User() {}

    public User(String email, String password, String firstName, String lastName, UserRole role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public User(String email, String password, String firstName, String lastName, UserRole role, 
                LocalDate dateOfBirth, Gender gender, Gender mentorGenderPreference, String city, String languages,
                String academicField, String currentCourse, String previousBackground) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.mentorGenderPreference = mentorGenderPreference;
        this.city = city;
        this.languages = languages;
        this.academicField = academicField;
        this.currentCourse = currentCourse;
        this.previousBackground = previousBackground;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Gender getMentorGenderPreference() { return mentorGenderPreference; }
    public void setMentorGenderPreference(Gender mentorGenderPreference) { this.mentorGenderPreference = mentorGenderPreference; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getAcademicField() { return academicField; }
    public void setAcademicField(String academicField) { this.academicField = academicField; }

    public String getCurrentCourse() { return currentCourse; }
    public void setCurrentCourse(String currentCourse) { this.currentCourse = currentCourse; }

    public String getPreviousBackground() { return previousBackground; }
    public void setPreviousBackground(String previousBackground) { this.previousBackground = previousBackground; }

    public enum UserRole {
        MENTEE, MENTOR, ADMIN
    }

    public enum Gender {
        MALE, FEMALE, ANY
    }
}