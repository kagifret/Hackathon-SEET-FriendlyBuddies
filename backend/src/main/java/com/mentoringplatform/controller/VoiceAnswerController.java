package com.mentoringplatform.controller;

import com.mentoringplatform.model.VoiceAnswer;
import com.mentoringplatform.service.VoiceAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/voice-answers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://matching-tan.vercel.app"})
public class VoiceAnswerController {

    @Autowired
    private VoiceAnswerService voiceAnswerService;

    @PostMapping("/save")
    public ResponseEntity<VoiceAnswer> saveVoiceAnswer(@RequestBody VoiceAnswer voiceAnswer) {
        try {
            VoiceAnswer saved = voiceAnswerService.saveVoiceAnswer(voiceAnswer);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/save-batch")
    public ResponseEntity<Map<String, String>> saveVoiceAnswers(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String userRole = request.get("userRole").toString();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> answers = (Map<String, Object>) request.get("answers");
            
            @SuppressWarnings("unchecked")
            Map<String, String> questions = (Map<String, String>) request.get("questions");

            for (Map.Entry<String, Object> entry : answers.entrySet()) {
                String questionKey = entry.getKey();
                String answerText = entry.getValue().toString();
                String questionText = questions.get(questionKey);

                if (answerText != null && !answerText.trim().isEmpty()) {
                    VoiceAnswer voiceAnswer = new VoiceAnswer(userId, questionKey, questionText, answerText, userRole);
                    voiceAnswerService.saveVoiceAnswer(voiceAnswer);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Voice answers saved successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to save voice answers: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VoiceAnswer>> getVoiceAnswersByUserId(@PathVariable Long userId) {
        try {
            List<VoiceAnswer> answers = voiceAnswerService.getVoiceAnswersByUserId(userId);
            return ResponseEntity.ok(answers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/role/{userRole}")
    public ResponseEntity<List<VoiceAnswer>> getVoiceAnswersByUserIdAndRole(
            @PathVariable Long userId, @PathVariable String userRole) {
        try {
            List<VoiceAnswer> answers = voiceAnswerService.getVoiceAnswersByUserIdAndRole(userId, userRole);
            return ResponseEntity.ok(answers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/question/{questionKey}")
    public ResponseEntity<VoiceAnswer> getVoiceAnswerByUserIdAndQuestionKey(
            @PathVariable Long userId, @PathVariable String questionKey) {
        try {
            Optional<VoiceAnswer> answer = voiceAnswerService.getVoiceAnswerByUserIdAndQuestionKey(userId, questionKey);
            if (answer.isPresent()) {
                return ResponseEntity.ok(answer.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteVoiceAnswersByUserId(@PathVariable Long userId) {
        try {
            voiceAnswerService.deleteVoiceAnswersByUserId(userId);
            return ResponseEntity.ok(Map.of("message", "Voice answers deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete voice answers"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVoiceAnswer(@PathVariable Long id) {
        try {
            voiceAnswerService.deleteVoiceAnswer(id);
            return ResponseEntity.ok(Map.of("message", "Voice answer deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete voice answer"));
        }
    }
}