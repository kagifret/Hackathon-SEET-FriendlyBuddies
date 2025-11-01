package com.mentoringplatform.service;

import com.mentoringplatform.model.*;
import com.mentoringplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenteeProfileRepository menteeProfileRepository;

    @Autowired
    private MentorProfileRepository mentorProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public MenteeProfile createMenteeProfile(Long userId, MenteeProfile profile) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        profile.setUser(user);
        return menteeProfileRepository.save(profile);
    }

    public MentorProfile createMentorProfile(Long userId, MentorProfile profile) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        profile.setUser(user);
        return mentorProfileRepository.save(profile);
    }

    public MenteeProfile createSimpleMenteeProfile(Long userId, String answer) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        MenteeProfile profile = new MenteeProfile();
        profile.setUser(user);
        // Set required fields with default values and user's answer
        profile.setAcademicField(user.getAcademicField() != null ? user.getAcademicField() : "General");
        profile.setMentorshipGoals(answer);
        profile.setCommunicationStyle("Open to various styles");
        profile.setCity(user.getCity());
        profile.setLanguage("English");
        profile.setAdditionalInfo("Profile created via voice answer");
        
        return menteeProfileRepository.save(profile);
    }

    public MentorProfile createSimpleMentorProfile(Long userId, String answer) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        MentorProfile profile = new MentorProfile();
        profile.setUser(user);
        // Set required fields with default values and user's answer
        profile.setAcademicField(user.getAcademicField() != null ? user.getAcademicField() : "General");
        profile.setAreasOfExpertise(answer);
        profile.setMentoringPhilosophy("Dedicated to helping others grow and learn");
        profile.setCommunicationStyle("Open to various styles");
        profile.setCity(user.getCity());
        profile.setLanguage("English");
        profile.setAdditionalInfo("Profile created via voice answer");
        
        return mentorProfileRepository.save(profile);
    }

    public MenteeProfile getMenteeProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return menteeProfileRepository.findByUser(user)
            .orElse(null);
    }

    public MentorProfile getMentorProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mentorProfileRepository.findByUser(user)
            .orElse(null);
    }

    public Map<String, Object> loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("message", "Login successful");
        return response;
    }

    public MenteeProfile updateMenteeProfile(Long userId, MenteeProfile updatedProfile) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        MenteeProfile existingProfile = menteeProfileRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        // Update the fields
        existingProfile.setAcademicField(updatedProfile.getAcademicField());
        existingProfile.setMentorshipGoals(updatedProfile.getMentorshipGoals());
        existingProfile.setCommunicationStyle(updatedProfile.getCommunicationStyle());
        existingProfile.setCity(updatedProfile.getCity());
        existingProfile.setLanguage(updatedProfile.getLanguage());
        existingProfile.setAge(updatedProfile.getAge());
        existingProfile.setAdditionalInfo(updatedProfile.getAdditionalInfo());
        
        return menteeProfileRepository.save(existingProfile);
    }

    public MentorProfile updateMentorProfile(Long userId, MentorProfile updatedProfile) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        MentorProfile existingProfile = mentorProfileRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        // Update the fields
        existingProfile.setAcademicField(updatedProfile.getAcademicField());
        existingProfile.setAreasOfExpertise(updatedProfile.getAreasOfExpertise());
        existingProfile.setMentoringPhilosophy(updatedProfile.getMentoringPhilosophy());
        existingProfile.setCommunicationStyle(updatedProfile.getCommunicationStyle());
        existingProfile.setCity(updatedProfile.getCity());
        existingProfile.setLanguage(updatedProfile.getLanguage());
        existingProfile.setAge(updatedProfile.getAge());
        existingProfile.setAdditionalInfo(updatedProfile.getAdditionalInfo());
        
        return mentorProfileRepository.save(existingProfile);
    }
}