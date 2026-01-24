package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, String> {

    Optional<QRCode> findByCodeData(String codeData);

    List<QRCode> findBySubEventId(String subEventId);

    Boolean existsByCodeData(String codeData);

    Optional<QRCode> findBySubEventIdAndIsActive(String subEventId, Boolean isActive);

    @Modifying
    @Query("UPDATE QRCode q SET q.isActive = false WHERE q.subEvent.id = :subEventId")
    void deactivateAllBySubEventId(@Param("subEventId") String subEventId);
}