package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, String> {

    Optional<QRCode> findByCodeData(String codeData);

    List<QRCode> findBySubEventId(String subEventId);

    Boolean existsByCodeData(String codeData);
}