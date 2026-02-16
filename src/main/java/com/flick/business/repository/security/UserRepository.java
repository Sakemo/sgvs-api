package com.flick.business.repository.security;

import com.flick.business.core.entity.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username. This method is crucial for Spring Security's
     * UserDetailsService.
     *
     * @param username The username to search for.
     * @return An Optional containing the found user or an empty Optional if not
     *         found.
     */
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);
  }
