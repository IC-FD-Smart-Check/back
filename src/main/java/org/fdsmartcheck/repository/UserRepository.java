package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findByRa(String ra);
    Boolean existsByRa(String ra);
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRaContainingIgnoreCase(
            String name, String email, String ra);
}