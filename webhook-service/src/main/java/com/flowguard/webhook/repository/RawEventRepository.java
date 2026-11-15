package com.flowguard.webhook.repository;

import com.flowguard.webhook.entity.RawEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawEventRepository extends JpaRepository<RawEvent, Long> {
    Page<RawEvent> findAllByOrderByReceivedAtDesc(Pageable pageable);
    boolean existsByEventId(String eventId);
}
