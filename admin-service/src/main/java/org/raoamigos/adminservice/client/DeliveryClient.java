package org.raoamigos.adminservice.client;

import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "DELIVERY-SERVICE")
public interface DeliveryClient {

    @GetMapping("/deliveries/{trackingNumber}")
    ApiResponse<DeliveryDTO> getDeliveryByTrackingNumber(@PathVariable("trackingNumber") String trackingNumber);

    @PutMapping("/deliveries/{trackingNumber}/status")
    ApiResponse<DeliveryDTO> updateDeliveryStatus(
            @PathVariable("trackingNumber") String trackingNumber,
            @RequestParam("status") String status,
            @RequestParam(value = "proofImagePath", required = false) String proofImagePath,
            @RequestParam(value = "deliveryNote", required = false) String deliveryNote);

    @GetMapping("/deliveries/stats/count")
    ApiResponse<Long> getTotalDeliveries();

    @PutMapping("/deliveries/{trackingNumber}/deliver")
    ApiResponse<DeliveryDTO> markDelivered(
            @PathVariable("trackingNumber") String trackingNumber,
            @RequestParam(value = "proofImagePath", required = true) String proofImagePath,
            @RequestParam(value = "deliveryNote", required = false) String deliveryNote);

    @GetMapping("/deliveries/admin/status/{status}")
    ApiResponse<List<DeliveryDTO>> getDeliveriesByStatus(@PathVariable("status") String status);

    @GetMapping("/deliveries/admin/status/{status}/count")
    ApiResponse<Long> countDeliveriesByStatus(@PathVariable("status") String status);

    @GetMapping("/deliveries/admin/city/{city}")
    ApiResponse<List<DeliveryDTO>> getDeliveriesByCity(@PathVariable("city") String city);

    @GetMapping("/deliveries/admin/report")
    ApiResponse<List<DeliveryDTO>> getDeliveriesByDateRange(
            @RequestParam("start") String start,
            @RequestParam("end") String end);
}
