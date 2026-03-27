package org.fdsmartcheck.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.GeoSignRequest;
import org.fdsmartcheck.dto.response.GeoSignResponse;
import org.fdsmartcheck.service.GeoSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoSecurityService geoSecurityService;

    @PostMapping("/sign")
    public ResponseEntity<GeoSignResponse> signPayload(@Valid @RequestBody GeoSignRequest request) {
        String signature = geoSecurityService.signPayload(request);

        GeoSignResponse response = GeoSignResponse.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .timestamp(request.getTimestamp())
                .deviceId(request.getDeviceId())
                .signature(signature)
                .build();

        return ResponseEntity.ok(response);
    }
}
