package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubEventResponse {
    private String id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private String locationDescription;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private LocalDateTime checkinStart;
    private LocalDateTime checkinEnd;
    private LocalDateTime checkoutStart;
    private LocalDateTime checkoutEnd;

    private String eventId;
    private String eventTitle;
}