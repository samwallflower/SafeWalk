package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserId(Long userId);
    boolean existsByContactEmail(String email);

    // past emergencies keep a list of the contacts that were told; those links must go before the contact can
    // be deleted
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM emergency_notified_contacts WHERE contact_id = :contactId", nativeQuery = true)
    void removeFromNotifiedLists(@Param("contactId") Long contactId);

}
