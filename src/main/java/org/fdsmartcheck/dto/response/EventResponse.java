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
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime checkinStart;
    private LocalDateTime checkinEnd;
    private String qrCode;
    private EventStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}