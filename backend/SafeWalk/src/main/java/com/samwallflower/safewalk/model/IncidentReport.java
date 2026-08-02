package com.samwallflower.safewalk.model;

import com.samwallflower.safewalk.enums.ReportStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class IncidentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Boolean isAnonymous;
    private Integer upvotes = 0;
    private Integer downvotes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="category_id", nullable=false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReportStatus status = ReportStatus.ACTIVE; // ACTIVE, HIDDEN, UNDER_REVIEW

    protected void onCreate(){
        this.timestamp = LocalDateTime.now();
    }

}
