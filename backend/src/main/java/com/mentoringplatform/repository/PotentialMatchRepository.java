package com.mentoringplatform.repository;

import com.mentoringplatform.model.PotentialMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PotentialMatchRepository extends JpaRepository<PotentialMatch, Long> {
    
    // Find all compatible matches for a specific mentee
    @Query("SELECT pm FROM PotentialMatch pm WHERE pm.mentee.id = :menteeId AND pm.isCompatible = true ORDER BY pm.compatibilityScore DESC")
    List<PotentialMatch> findCompatibleMatchesByMenteeId(@Param("menteeId") Long menteeId);
    
    // Find all matches (compatible and incompatible) for a specific mentee
    @Query("SELECT pm FROM PotentialMatch pm WHERE pm.mentee.id = :menteeId ORDER BY pm.compatibilityScore DESC")
    List<PotentialMatch> findAllMatchesByMenteeId(@Param("menteeId") Long menteeId);
    
    // Find all compatible matches for a specific mentor
    @Query("SELECT pm FROM PotentialMatch pm WHERE pm.mentor.id = :mentorId AND pm.isCompatible = true ORDER BY pm.compatibilityScore DESC")
    List<PotentialMatch> findCompatibleMatchesByMentorId(@Param("mentorId") Long mentorId);
    
    // Check if a specific mentor-mentee combination exists
    Optional<PotentialMatch> findByMenteeIdAndMentorId(Long menteeId, Long mentorId);
    
    // Delete all matches for a specific mentee (when profile is updated)
    void deleteByMenteeId(Long menteeId);
    
    // Delete all matches for a specific mentor (when profile is updated)
    void deleteByMentorId(Long mentorId);
    
    // Count compatible matches for a mentee
    @Query("SELECT COUNT(pm) FROM PotentialMatch pm WHERE pm.mentee.id = :menteeId AND pm.isCompatible = true")
    Long countCompatibleMatchesByMenteeId(@Param("menteeId") Long menteeId);
    
    // Find top N compatible matches for a mentee
    @Query("SELECT pm FROM PotentialMatch pm WHERE pm.mentee.id = :menteeId AND pm.isCompatible = true ORDER BY pm.compatibilityScore DESC LIMIT :limit")
    List<PotentialMatch> findTopCompatibleMatches(@Param("menteeId") Long menteeId, @Param("limit") int limit);
}