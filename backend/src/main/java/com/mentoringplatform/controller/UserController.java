package com.mentoringplatform.controller;

import com.mentoringplatform.model.*;
import com.mentoringplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://matching-tan.vercel.app"})
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            Map<String, Object> response = userService.loginUser(email, password);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/simple-profile")
    public ResponseEntity<User> getSimpleProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/mentee-profile")
    public ResponseEntity<MenteeProfile> createMenteeProfile(@PathVariable Long userId, @RequestBody MenteeProfile profile) {
        MenteeProfile createdProfile = userService.createMenteeProfile(userId, profile);
        return ResponseEntity.ok(createdProfile);
    }

    @PostMapping("/{userId}/mentor-profile")
    public ResponseEntity<MentorProfile> createMentorProfile(@PathVariable Long userId, @RequestBody MentorProfile profile) {
        MentorProfile createdProfile = userService.createMentorProfile(userId, profile);
        return ResponseEntity.ok(createdProfile);
    }

    @GetMapping("/{userId}/mentee-profile")
    public ResponseEntity<MenteeProfile> getMenteeProfile(@PathVariable Long userId) {
        MenteeProfile profile = userService.getMenteeProfile(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}/mentor-profile")
    public ResponseEntity<MentorProfile> getMentorProfile(@PathVariable Long userId) {
        MentorProfile profile = userService.getMentorProfile(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{userId}/mentee-profile")
    public ResponseEntity<MenteeProfile> updateMenteeProfile(@PathVariable Long userId, @RequestBody MenteeProfile profile) {
        try {
            MenteeProfile updatedProfile = userService.updateMenteeProfile(userId, profile);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/simple-mentee-profile")
    public ResponseEntity<MenteeProfile> createSimpleMenteeProfile(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String answer = request.get("answer");
            MenteeProfile createdProfile = userService.createSimpleMenteeProfile(userId, answer);
            return ResponseEntity.ok(createdProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{userId}/simple-mentor-profile")
    public ResponseEntity<MentorProfile> createSimpleMentorProfile(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String answer = request.get("answer");
            MentorProfile createdProfile = userService.createSimpleMentorProfile(userId, answer);
            return ResponseEntity.ok(createdProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{userId}/mentor-profile")
    public ResponseEntity<MentorProfile> updateMentorProfile(@PathVariable Long userId, @RequestBody MentorProfile profile) {
        try {
            MentorProfile updatedProfile = userService.updateMentorProfile(userId, profile);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}