package com.crt.server.repository;

import com.crt.server.model.Role;
import com.crt.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailOrUsername(String email, String username);
    
    Long countByRole(Role role);

    List<User> findByRole(Role role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'FACULTY' AND u.isActive = true ORDER BY u.name ASC")
    List<User> findAllFacultyUsers();
    
    // Optimized query with better performance
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true ORDER BY u.name ASC")
    List<User> findActiveUsersByRole(@Param("role") Role role);
}
