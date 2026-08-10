package com.samwallflower.safewalk.model;

import com.samwallflower.safewalk.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WalkSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // basically many walk sessions could point to one route
    // meaning many people may choose the same route
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private Double lastKnownLatitude;
    private Double lastKnownLongitude;
    private LocalDateTime lastLocationUpdate;

    private LocalDateTime lastArrivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status; //ACTIVE, COMPLETED, EMERGENCY, ABANDONED

    private Boolean alarmTriggered = false;
    private Boolean autoCompleted = false;

    public WalkSession(Double originLatitude, Double originLongitude, Double destinationLatitude, Double destinationLongitude) {
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
    }


    public WalkSession(Double originLatitude, Double originLongitude, Double destinationLatitude, Double destinationLongitude, Route route) {
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.route = route;
    }

    @PrePersist
    protected void onCreate(){
        this.startTime = LocalDateTime.now();
    }
}
