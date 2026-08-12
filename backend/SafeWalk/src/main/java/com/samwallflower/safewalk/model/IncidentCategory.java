package com.samwallflower.safewalk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class IncidentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(nullable = false, unique = true)
    private String name; // Robbery, road accident

    @Min(value = 1, message = "Severity weight must be at least 1")
    @Max(value = 20, message = "Severity weight must be at most 20")
    @Column(nullable = false)
    private Integer severityWeight; // robbery - 20 , road accident - 18

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<IncidentReport> reports;

    private String description;

    public IncidentCategory(String name) {
        this.name = name;
    }

    public IncidentCategory(String name, Integer severityWeight) {
        this.name = name;
        this.severityWeight = severityWeight;
    }
}
