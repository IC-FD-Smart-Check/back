package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findBySubEventId(String subEventId);

    boolean existsBySubEventIdAndUserId(String subEventId, String userId);

    void deleteBySubEventIdAndUserId(String subEventId, String userId);
}
