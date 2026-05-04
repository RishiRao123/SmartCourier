package org.raoamigos.trackingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tracking_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String status;

    @Enumerated(EnumType.STRING)
    private HubLocation location;

    @Column(length = 500)
    private String message;

    private String proofImagePath;
    private String deliveryNote;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant timestamp;
}