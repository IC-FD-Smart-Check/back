package org.fdsmartcheck.controller;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.DashboardStatsResponse;
import org.fdsmartcheck.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Métricas agregadas exibidas nos cards da tela inicial
     *
     * GET /api/dashboard/stats
     * Acesso: ADMIN
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
