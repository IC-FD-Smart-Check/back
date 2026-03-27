package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoSignResponse {
    private Double latitude;
    private Double longitude;
    private Long timestamp;
    private String deviceId;
    private String signature;
}
