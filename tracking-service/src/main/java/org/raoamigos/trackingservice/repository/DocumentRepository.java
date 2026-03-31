package org.raoamigos.trackingservice.repository;

import org.raoamigos.trackingservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
