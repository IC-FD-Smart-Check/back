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
public class QRCodeResponse {
    private String id;
    private String codeData;
    private String subEventId;
    private String subEventTitle;
    private Boolean isActive;
    private LocalDateTime createdAt;
}