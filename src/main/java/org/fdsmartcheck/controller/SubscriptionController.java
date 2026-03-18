package org.fdsmartcheck.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.SubscriptionRequest;
import org.fdsmartcheck.dto.response.SubscriptionResponse;
import org.fdsmartcheck.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.subscribe(request));
    }

    @DeleteMapping("/subevent/{subEventId}/user/{userId}")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable String subEventId,
            @PathVariable String userId) {
        subscriptionService.unsubscribe(subEventId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subevent/{subEventId}")
    public ResponseEntity<List<SubscriptionResponse>> listBySubEvent(@PathVariable String subEventId) {
        return ResponseEntity.ok(subscriptionService.listBySubEvent(subEventId));
    }
}
