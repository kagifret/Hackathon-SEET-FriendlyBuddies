package com.mentoringplatform.repository;

import com.mentoringplatform.model.ChatMessage;
import com.mentoringplatform.model.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByMatchRequestOrderBySentAtAsc(MatchRequest matchRequest);
    
    List<ChatMessage> findByMatchRequest(MatchRequest matchRequest);
    
    @Query("SELECT c FROM ChatMessage c WHERE c.matchRequest.id = :matchRequestId ORDER BY c.sentAt ASC")
    List<ChatMessage> findByMatchRequestIdOrderBySentAtAsc(@Param("matchRequestId") Long matchRequestId);
    
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.matchRequest.id = :matchRequestId")
    long countByMatchRequestId(@Param("matchRequestId") Long matchRequestId);
}