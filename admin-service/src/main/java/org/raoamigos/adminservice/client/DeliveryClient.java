package org.raoamigos.adminservice.client;

import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "DELIVERY-SERVICE", url = "http://localhost:8082")
public interface DeliveryClient {

    @GetMapping("/deliveries/{trackingNumber}")
    ApiResponse<DeliveryDTO> getDeliveryByTrackingNumber(@PathVariable("trackingNumber") String trackingNumber);
}
