package com.mentoringplatform.repository;

import com.mentoringplatform.model.MenteeProfile;
import com.mentoringplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MenteeProfileRepository extends JpaRepository<MenteeProfile, Long> {
    Optional<MenteeProfile> findByUser(User user);
}