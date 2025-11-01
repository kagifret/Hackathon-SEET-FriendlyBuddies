package com.mentoringplatform.repository;

import com.mentoringplatform.model.MatchRequest;
import com.mentoringplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    List<MatchRequest> findByMenteeAndStatus(User mentee, MatchRequest.MatchStatus status);
    List<MatchRequest> findByMentorAndStatus(User mentor, MatchRequest.MatchStatus status);
    List<MatchRequest> findByStatus(MatchRequest.MatchStatus status);
    List<MatchRequest> findByMenteeId(Long menteeId);
    List<MatchRequest> findByMentorId(Long mentorId);
    boolean existsByMenteeAndMentor(User mentee, User mentor);
    
    // Check for active (non-rejected) requests between specific mentee and mentor
    boolean existsByMenteeAndMentorAndStatusIn(User mentee, User mentor, List<MatchRequest.MatchStatus> statuses);
    
    List<MatchRequest> findByMenteeOrMentorAndStatusIn(User mentee, User mentor, List<MatchRequest.MatchStatus> statuses);
}