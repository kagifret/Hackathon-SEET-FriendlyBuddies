package com.mentoringplatform.repository;

import com.mentoringplatform.model.ManualConstraint;
import com.mentoringplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ManualConstraintRepository extends JpaRepository<ManualConstraint, Long> {
    List<ManualConstraint> findByMenteeAndMentor(User mentee, User mentor);
    List<ManualConstraint> findByMenteeAndMentorAndType(User mentee, User mentor, ManualConstraint.ConstraintType type);
}