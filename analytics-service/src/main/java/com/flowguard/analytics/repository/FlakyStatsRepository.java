package com.flowguard.analytics.repository;

import com.flowguard.analytics.entity.FlakyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlakyStatsRepository extends JpaRepository<FlakyStats, Long> {
    Optional<FlakyStats> findByRepoAndJobNameAndHeadBranch(String repo, String jobName, String headBranch);
    List<FlakyStats> findByRepoAndIsFlakyTrueOrderByFailureRateDesc(String repo);
    List<FlakyStats> findAllByIsFlakyTrueOrderByFailureRateDesc();
}
