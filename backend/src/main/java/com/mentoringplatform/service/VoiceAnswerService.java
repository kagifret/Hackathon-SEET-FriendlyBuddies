package com.mentoringplatform.service;

import com.mentoringplatform.model.VoiceAnswer;
import com.mentoringplatform.repository.VoiceAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoiceAnswerService {

    @Autowired
    private VoiceAnswerRepository voiceAnswerRepository;

    public VoiceAnswer saveVoiceAnswer(VoiceAnswer voiceAnswer) {
        // Check if answer already exists for this user and question
        Optional<VoiceAnswer> existing = voiceAnswerRepository.findByUserIdAndQuestionKey(
            voiceAnswer.getUserId(), voiceAnswer.getQuestionKey());
        
        if (existing.isPresent()) {
            // Update existing answer
            VoiceAnswer existingAnswer = existing.get();
            existingAnswer.setAnswerText(voiceAnswer.getAnswerText());
            existingAnswer.setQuestionText(voiceAnswer.getQuestionText());
            return voiceAnswerRepository.save(existingAnswer);
        } else {
            // Create new answer
            return voiceAnswerRepository.save(voiceAnswer);
        }
    }

    public List<VoiceAnswer> getVoiceAnswersByUserId(Long userId) {
        return voiceAnswerRepository.findByUserId(userId);
    }

    public List<VoiceAnswer> getVoiceAnswersByUserIdAndRole(Long userId, String userRole) {
        return voiceAnswerRepository.findByUserIdAndUserRole(userId, userRole);
    }

    public Optional<VoiceAnswer> getVoiceAnswerByUserIdAndQuestionKey(Long userId, String questionKey) {
        return voiceAnswerRepository.findByUserIdAndQuestionKey(userId, questionKey);
    }

    public void deleteVoiceAnswersByUserId(Long userId) {
        voiceAnswerRepository.deleteByUserId(userId);
    }

    public void deleteVoiceAnswer(Long id) {
        voiceAnswerRepository.deleteById(id);
    }
}