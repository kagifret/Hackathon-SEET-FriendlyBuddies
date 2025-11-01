package com.mentoringplatform.controller;

import com.mentoringplatform.model.ChatMessage;
import com.mentoringplatform.model.MatchRequest;
import com.mentoringplatform.model.User;
import com.mentoringplatform.repository.ChatMessageRepository;
import com.mentoringplatform.repository.MatchRequestRepository;
import com.mentoringplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://matching-tan.vercel.app"})
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // DTO for WebSocket messages
    public static class ChatMessageRequest {
        private Long matchRequestId;
        private Long senderId;
        private String content;

        // Getters and setters
        public Long getMatchRequestId() { return matchRequestId; }
        public void setMatchRequestId(Long matchRequestId) { this.matchRequestId = matchRequestId; }
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // WebSocket message handling
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request) {
        // Create and save message
        ChatMessage chatMessage = createChatMessage(request.getMatchRequestId(), request.getSenderId(), request.getContent());
        
        if (chatMessage != null) {
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            // Send message to specific match room
            messagingTemplate.convertAndSend("/topic/match/" + request.getMatchRequestId(), savedMessage);

            // Update chat score based on engagement
            updateChatScore(request.getMatchRequestId());
        }
    }

    // REST endpoint to get chat history
    @GetMapping("/match/{matchRequestId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long matchRequestId) {
        List<ChatMessage> messages = chatMessageRepository.findByMatchRequestIdOrderBySentAtAsc(matchRequestId);
        return ResponseEntity.ok(messages);
    }

    // REST endpoint to send message (alternative to WebSocket)
    @PostMapping("/match/{matchRequestId}/message")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable Long matchRequestId, 
            @RequestBody ChatMessageRequest request) {
        
        ChatMessage chatMessage = createChatMessage(matchRequestId, request.getSenderId(), request.getContent());
        
        if (chatMessage == null) {
            return ResponseEntity.badRequest().build();
        }

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Send via WebSocket to real-time listeners
        messagingTemplate.convertAndSend("/topic/match/" + matchRequestId, savedMessage);

        // Update chat score
        updateChatScore(matchRequestId);

        return ResponseEntity.ok(savedMessage);
    }

    // Check if user can access this chat (they must be part of the match)
    @GetMapping("/match/{matchRequestId}/access/{userId}")
    public ResponseEntity<Boolean> checkChatAccess(@PathVariable Long matchRequestId, @PathVariable Long userId) {
        Optional<MatchRequest> matchRequest = matchRequestRepository.findById(matchRequestId);
        
        if (matchRequest.isPresent()) {
            MatchRequest match = matchRequest.get();
            boolean hasAccess = match.getMentee().getId().equals(userId) || match.getMentor().getId().equals(userId);
            
            // Only allow access if match is accepted by mentor
            boolean isAccepted = "MENTOR_ACCEPTED".equals(match.getStatus().toString()) || 
                               "ADMIN_APPROVED".equals(match.getStatus().toString());
            
            return ResponseEntity.ok(hasAccess && isAccepted);
        }
        
        return ResponseEntity.ok(false);
    }

    private ChatMessage createChatMessage(Long matchRequestId, Long senderId, String content) {
        Optional<MatchRequest> matchRequestOpt = matchRequestRepository.findById(matchRequestId);
        Optional<User> senderOpt = userRepository.findById(senderId);
        
        if (matchRequestOpt.isPresent() && senderOpt.isPresent()) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMatchRequest(matchRequestOpt.get());
            chatMessage.setSender(senderOpt.get());
            chatMessage.setContent(content);
            chatMessage.setSentAt(LocalDateTime.now());
            return chatMessage;
        }
        
        return null;
    }

    private void updateChatScore(Long matchRequestId) {
        Optional<MatchRequest> matchRequestOpt = matchRequestRepository.findById(matchRequestId);
        
        if (matchRequestOpt.isPresent()) {
            MatchRequest matchRequest = matchRequestOpt.get();
            
            // Count total messages in this chat
            long messageCount = chatMessageRepository.countByMatchRequestId(matchRequestId);
            
            // Simple chat engagement scoring (0-100 scale)
            // More messages = higher engagement score
            double chatScore = Math.min(100.0, messageCount * 2.0);
            
            matchRequest.setChatScore(chatScore);
            matchRequestRepository.save(matchRequest);
        }
    }
}