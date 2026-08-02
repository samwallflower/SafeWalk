package com.samwallflower.safewalk.model;

import jakarta.persistence.*;

@Entity
public class IncidentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name; // Robbery, road accident

    @Column(nullable = false)
    private Integer severityWeight; // robbery - 20 , road accident - 18

    private String description;
}
