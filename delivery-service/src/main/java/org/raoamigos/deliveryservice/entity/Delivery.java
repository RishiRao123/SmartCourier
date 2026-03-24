package org.raoamigos.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    private String senderName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "sender_street")),
            @AttributeOverride(name = "city", column = @Column(name = "sender_city")),
            @AttributeOverride(name = "state", column = @Column(name = "sender_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "sender_zip_code"))
    })
    private Address senderAddress;

    private String receiverName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "receiver_street")),
            @AttributeOverride(name = "city", column = @Column(name = "receiver_city")),
            @AttributeOverride(name = "state", column = @Column(name = "receiver_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "receiver_zip_code"))
    })
    private Address receiverAddress;

    @Embedded
    private PackageDetails packageDetails;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}