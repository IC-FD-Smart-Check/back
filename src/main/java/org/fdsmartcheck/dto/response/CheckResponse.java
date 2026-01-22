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
    private String userId;
    private String type;
    private LocalDateTime createdAt;
    private String message;
}