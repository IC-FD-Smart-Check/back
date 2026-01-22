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
public class CheckResponse {
    private String id;
    private String eventId;
    private String eventTitle;
    private String userId;
    private String userName;
    private String type;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private LocalDateTime createdAt;
    private String message;
}