package com.samwallflower.safewalk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String polyline;

    private Double actualDistanceMeters;
    private Double safetyPenaltyMeters;
    private Double virtualDistanceMeters;
    private Integer rank; // 1 = safest/recommended , 2,3,...
    private String routeRequestId; // groups sibling routes from same / recommend call

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    // here if a walk session is deleted it's related route will also be deleted
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalkSession> walkSessions;

    public Route(String polyline) {
        this.polyline = polyline;
    }
}
