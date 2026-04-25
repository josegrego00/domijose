package com.domicilio.domijose.repositories;

import com.domicilio.domijose.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // por el momento lo dejare hay pero no lo usare, el username sera el numero de
    // telefono
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    // -----------------------------------------------------------------------------------

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);
}