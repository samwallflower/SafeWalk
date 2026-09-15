package com.samwallflower.safewalk.model;

import com.samwallflower.safewalk.enums.EmergencyTriggerSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Emergency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // many emergencies can belong to one session
    // route deviation , connection lost etc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_session_id", nullable = false)
    private WalkSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyTriggerSource triggerSource;

    @Column(nullable = false)
    private Double triggerLatitude;

    @Column(nullable = false)
    private Double triggerLongitude;

    // variable number contacts may be notified for variable number of emergencies
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "emergency_notified_contacts",
        joinColumns = @JoinColumn(name = "emergency_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private List<EmergencyContact> notifiedEmergencyContacts = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime triggerTimestamp;

    @PrePersist
    protected void onCreate(){
        this.triggerTimestamp = LocalDateTime.now();
    }
}
