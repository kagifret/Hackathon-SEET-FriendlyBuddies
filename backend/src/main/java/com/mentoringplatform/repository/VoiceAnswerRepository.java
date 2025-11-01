package com.mentoringplatform.repository;

import com.mentoringplatform.model.VoiceAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceAnswerRepository extends JpaRepository<VoiceAnswer, Long> {
    
    List<VoiceAnswer> findByUserId(Long userId);
    
    List<VoiceAnswer> findByUserIdAndUserRole(Long userId, String userRole);
    
    Optional<VoiceAnswer> findByUserIdAndQuestionKey(Long userId, String questionKey);
    
    void deleteByUserId(Long userId);
}