package org.fdsmartcheck.dto.response;

import org.fdsmartcheck.model.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}