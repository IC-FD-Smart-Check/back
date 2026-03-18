package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRequest {

    @NotBlank(message = "SubEvent ID é obrigatório")
    private String subEventId;

    @NotBlank(message = "User ID é obrigatório")
    private String userId;
}
