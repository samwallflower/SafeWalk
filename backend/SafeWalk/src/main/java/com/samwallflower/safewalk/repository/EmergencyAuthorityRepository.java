package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.model.EmergencyAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmergencyAuthorityRepository extends JpaRepository<EmergencyAuthority, Long> {
    Optional<EmergencyAuthority> findByCountryCode(String countryCode);
    Optional<EmergencyAuthority> findByCountryNameIgnoreCase(String countryName);

    boolean existsByCountryCode(String countryCode);
    boolean existsByCountryName(String countryName);
}
