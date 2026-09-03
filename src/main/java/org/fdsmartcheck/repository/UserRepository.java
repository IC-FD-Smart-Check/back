package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    long countByClassGroupIdAndIsActiveTrue(String classGroupId);

    // Busca por id porque o principal autenticado passou a ser o user.getId()
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.classGroup cg LEFT JOIN FETCH cg.course WHERE u.id = :id")
    Optional<User> findByIdWithClassGroup(@Param("id") String id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.classGroup cg LEFT JOIN FETCH cg.course WHERE u.isActive = true")
    List<User> findAllActiveWithClassGroup();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.classGroup cg LEFT JOIN FETCH cg.course " +
            "WHERE u.isActive = true AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.ra) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchActiveWithClassGroup(@Param("query") String query);
}
