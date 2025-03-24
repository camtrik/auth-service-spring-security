package com.ebbilogue.authservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ebbilogue.authservice.models.ERole;
import com.ebbilogue.authservice.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByName(ERole name);    
    boolean existsByName(ERole name); 
}
