package org.raoamigos.trackingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String fileName;

    private  String fileType;

    @Column(nullable = false)
    private String filePath;

    private Long uploadedBy;
    private String uploaderRole;

    @CreationTimestamp
    private Instant uploadedAt;

}
