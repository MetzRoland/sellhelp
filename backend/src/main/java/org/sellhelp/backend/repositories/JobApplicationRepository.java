package org.sellhelp.backend.repositories;

import org.sellhelp.backend.entities.JobApplication;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Integer> {
    boolean existsByApplicantAndJobPost(User applicant, Post jobPost);

    Optional<JobApplication> findByApplicantAndJobPost(User applicant, Post jobPost);
}
