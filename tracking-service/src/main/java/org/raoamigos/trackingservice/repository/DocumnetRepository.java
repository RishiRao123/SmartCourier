package org.raoamigos.trackingservice.repository;

import org.raoamigos.trackingservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumnetRepository extends JpaRepository<Document, Long> {
}
