package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String locationDescription;
    private String eventId;
    private String eventTitle;
}