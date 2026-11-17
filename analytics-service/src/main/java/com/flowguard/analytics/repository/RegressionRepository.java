package com.flowguard.analytics.repository;

import com.flowguard.analytics.entity.Regression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegressionRepository extends JpaRepository<Regression, Long> {
    List<Regression> findByRepoAndResolvedAtIsNullOrderByDetectedAtDesc(String repo);
    List<Regression> findAllByResolvedAtIsNullOrderByDetectedAtDesc();
    Optional<Regression> findByRepoAndJobNameAndHeadBranchAndResolvedAtIsNull(
            String repo, String jobName, String headBranch);
}
