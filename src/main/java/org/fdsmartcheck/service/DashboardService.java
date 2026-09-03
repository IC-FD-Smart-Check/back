package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.DashboardStatsResponse;
import org.fdsmartcheck.repository.CheckRepository;
import org.fdsmartcheck.repository.EventRepository;
import org.fdsmartcheck.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final CheckRepository checkRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        long activeEvents = eventRepository.countActiveEvents(now);
        long checkinsToday = checkRepository.countCheckinsBetween(startOfToday, startOfTomorrow);
        long totalParticipants = checkRepository.countDistinctParticipants();

        return DashboardStatsResponse.builder()
                .activeEvents(activeEvents)
                .checkinsToday(checkinsToday)
                .totalParticipants(totalParticipants)
                .attendanceRate(calculateAttendanceRate())
                .build();
    }

    /**
     * Presença completa (check-in + checkout) sobre o total de inscrições.
     * Mesma definição de "presente" usada na tela de relatórios.
     */
    private double calculateAttendanceRate() {
        long totalSubscriptions = subscriptionRepository.count();

        if (totalSubscriptions == 0) {
            return 0.0;
        }

        long completedChecks = checkRepository.countByCheckoutTimeIsNotNull();

        // Arredonda para uma casa decimal
        return Math.round(completedChecks * 1000.0 / totalSubscriptions) / 10.0;
    }
}
