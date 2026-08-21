package com.samwallflower.safewalk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EmergencyAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String countryCode;         // ISO 3166-1 alpha-2, e.g. "HU"
    private String countryName;
    @Column(nullable = false)
    private String policeNumber;        // e.g. "107" (Hungary)
    @Column(nullable = false)
    private String ambulanceNumber;     // e.g. "104" (Hungary)
    private String generalEmergencyNumber;      // e.g. "112"
}
