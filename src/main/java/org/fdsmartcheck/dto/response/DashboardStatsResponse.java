package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    /** Eventos não cancelados que ainda não terminaram */
    private long activeEvents;

    /** Check-ins realizados hoje */
    private long checkinsToday;

    /** Usuários distintos com pelo menos um check-in */
    private long totalParticipants;

    /** Percentual de inscrições que viraram presença completa (check-in + checkout) */
    private double attendanceRate;
}
