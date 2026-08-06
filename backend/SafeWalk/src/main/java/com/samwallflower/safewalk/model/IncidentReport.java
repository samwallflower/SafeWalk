package com.samwallflower.safewalk.model;

import com.samwallflower.safewalk.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class IncidentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Boolean isAnonymous = false;
    private Integer upvotes = 0;
    private Integer downvotes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="category_id", nullable=false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.ACTIVE; // ACTIVE, HIDDEN, UNDER_REVIEW

    @PrePersist
    protected void onCreate(){
        this.timestamp = LocalDateTime.now();
    }

    public IncidentReport(String description, Double latitude, Double longitude, User user, IncidentCategory category) {
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.user = user;
        this.category = category;
    }

    public IncidentReport(String description, Double latitude, Double longitude, Boolean isAnonymous, User user, IncidentCategory category) {
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAnonymous = isAnonymous;
        this.user = user;
        this.category = category;
    }
}
