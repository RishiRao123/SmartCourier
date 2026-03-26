package org.raoamigos.adminservice.client;

import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DELIVERY-SERVICE", url = "http://localhost:8082")
public interface DeliveryClient {

    @GetMapping("/deliveries/{trackingNumber}")
    ApiResponse<DeliveryDTO> getDeliveryByTrackingNumber(@PathVariable("trackingNumber") String trackingNumber);

    @PutMapping("/deliveries/{trackingNumber}/status")
    ApiResponse<DeliveryDTO> updateDeliveryStatus(
            @PathVariable("trackingNumber") String trackingNumber,
            @RequestParam("status") String status);
}
