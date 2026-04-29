package org.raoamigos.adminservice.client;

import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.TrackingEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "TRACKING-SERVICE")
public interface TrackingClient {

    @GetMapping("/tracking/admin/stats/count")
    ApiResponse<Long> getTotalTrackingEvents();

    @GetMapping("/tracking/{trackingNumber}")
    ApiResponse<List<TrackingEventDTO>> getTrackingHistory(@PathVariable("trackingNumber") String trackingNumber);

    @GetMapping("/tracking/admin/status/{status}")
    List<TrackingEventDTO> getTrackingEventsByStatus(@PathVariable("status") String status);

    @GetMapping("/tracking/admin/recent")
    List<TrackingEventDTO> getRecentSystemEvents(@RequestParam("days") int days);
}
