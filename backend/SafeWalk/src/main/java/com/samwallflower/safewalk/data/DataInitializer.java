package com.samwallflower.safewalk.data;

import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.model.EmergencyAuthority;
import com.samwallflower.safewalk.model.EmergencyContact;
import com.samwallflower.safewalk.model.Role;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.EmergencyAuthorityRepository;
import com.samwallflower.safewalk.repository.EmergencyContactRepository;
import com.samwallflower.safewalk.repository.RoleRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    /**
     * This class is responsible for initializing data when the application is ready.
     * It implements ApplicationListener to listen for ApplicationReadyEvent.
     */
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final EmergencyAuthorityRepository emergencyAuthorityRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Set<String> defaultRoles = Set.of("ROLE_USER", "ROLE_ADMIN");
        createDefaultRoleIfNotExist(defaultRoles);
        createDefaultUserIfNotExists();
        createDefaultAdminIfNotExists();
        createDefaultEmergencyAuthoritiesIfNotExists();
    }

    private void createDefaultRoleIfNotExist(Set<String> roles){
        roles.stream()
                .filter(role-> roleRepository.findByName(role).isEmpty())
                .map(Role::new)
                .forEach(roleRepository::save);
    }

    // so here we create some users
    // then for each user we create 2 default emergency contact
    private void createDefaultUserIfNotExists(){
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        for (int i = 1; i <= 5; i++) {
            String defaultEmail = "user" + i + "@email.com";
            if(userRepository.findByEmail(defaultEmail).isPresent()){
                continue;
            }
            User user = new User();
            user.setEmail(defaultEmail);
            user.setFirstName("The User");
            user.setLastName("user "+ i);
            user.setPassword("123456");
            user.setPhoneNumber("1234567890");
            user.setRoles(Set.of(role));
            User savedUser = userRepository.save(user);
            log.info("Default user created: {} created successfully with email: {}", i, defaultEmail);
            createDefaultContactIfNotExists(savedUser);
        }
    }

    private void createDefaultAdminIfNotExists(){
        Role role = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        for (int i = 1; i <= 2; i++) {
            String defaultEmail = "admin" + i + "@email.com";
            if(userRepository.findByEmail(defaultEmail).isPresent()){
                continue;
            }
            User user = new User();
            user.setEmail(defaultEmail);
            user.setFirstName("The Admin");
            user.setLastName("admin "+ i);
            user.setPassword("123456");
            user.setRoles(Set.of(role));
            userRepository.save(user);
            log.info("Default admin created: {} created successfully with email: {}", i, defaultEmail);
        }
    }


    private void createDefaultContactIfNotExists(User user){
        List<EmergencyContact> existingContacts = emergencyContactRepository.findByUserId(user.getId());
        if (!existingContacts.isEmpty()) {
            log.info("Emergency contacts already exist for user: {}", user.getEmail());
            return;
        }
        List<EmergencyContact> newContacts = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            String defaultEmail ="user_" + user.getId() + "_contact" + i + "@email.com";
            EmergencyContact contact = new EmergencyContact();
            contact.setContactName("The Contact " + i);
            contact.setContactEmail(defaultEmail);
            contact.setContactPhone("1234567890");
            contact.setUser(user);
            newContacts.add(contact);
        }
        emergencyContactRepository.saveAll(newContacts);

        log.info("Default contacts created for user: {}", user.getEmail());
    }

    private void createDefaultEmergencyAuthoritiesIfNotExists() {
        List<EmergencyAuthority> defaultAuthorities = List.of(
                createAuthority("HU", "Hungary", "107", "104", "112"),
                createAuthority("US", "United States", "911", "911", "911"),
                createAuthority("GB", "United Kingdom", "999", "999", "112"),
                createAuthority("AU", "Australia", "000", "000", "000"),
                createAuthority("IN", "India", "100", "102", "112"),
                createAuthority("BD", "Bangladesh", "999", "999", "999")
        );

        int addedCount = 0;

        for (EmergencyAuthority authority : defaultAuthorities) {
            // Check individually using the exact country code
            if (!emergencyAuthorityRepository.existsByCountryCode(authority.getCountryCode())) {
                emergencyAuthorityRepository.save(authority);
                addedCount++;
                log.info("Seeded emergency authority for: {}", authority.getCountryName());
            }
        }

        if (addedCount > 0) {
            log.info("Successfully added {} new default emergency authorities.", addedCount);
        } else {
            log.info("All default emergency authorities already exist in the database.");
        }
    }

    private EmergencyAuthority createAuthority(String code, String name, String police, String ambulance, String general) {
        EmergencyAuthority authority = new EmergencyAuthority();
        authority.setCountryCode(code);
        authority.setCountryName(name);
        authority.setPoliceNumber(police);
        authority.setAmbulanceNumber(ambulance);
        authority.setGeneralEmergencyNumber(general);
        return authority;
    }
}
