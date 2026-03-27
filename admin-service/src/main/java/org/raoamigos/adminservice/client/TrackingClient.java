package org.raoamigos.adminservice.client;

import org.raoamigos.adminservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "TRACKING-SERVICE")
public interface TrackingClient {

    @GetMapping("/tracking/stats/count")
    ApiResponse<Long> getTotalTrackingEvents();
}
