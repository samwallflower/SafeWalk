package com.samwallflower.safewalk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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

    public IncidentCategory(String name) {
        this.name = name;
    }

    public IncidentCategory(String name, Integer severityWeight) {
        this.name = name;
        this.severityWeight = severityWeight;
    }
}
