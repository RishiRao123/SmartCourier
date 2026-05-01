package org.raoamigos.deliveryservice.repository;

import org.raoamigos.deliveryservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByDeliveryId(Long deliveryId);

    Optional<Invoice> findByDeliveryTrackingNumber(String trackingNumber);
}
