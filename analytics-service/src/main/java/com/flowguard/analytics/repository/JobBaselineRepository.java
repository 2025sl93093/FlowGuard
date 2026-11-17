package com.flowguard.analytics.repository;

import com.flowguard.analytics.entity.JobBaseline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobBaselineRepository extends JpaRepository<JobBaseline, Long> {
    Optional<JobBaseline> findByRepoAndJobNameAndHeadBranch(String repo, String jobName, String headBranch);
}
