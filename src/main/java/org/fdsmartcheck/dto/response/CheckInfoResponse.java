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
public class CheckInfoResponse {
    // Informações do Evento
    private String eventId;
    private String eventTitle;
    private String eventDescription;

    // Informações do SubEvento
    private String subEventId;
    private String subEventTitle;
    private String subEventDescription;
    private String locationDescription;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Janelas de Check-in/Checkout
    private LocalDateTime checkinStart;
    private LocalDateTime checkinEnd;
    private LocalDateTime checkoutStart;
    private LocalDateTime checkoutEnd;

    // Ação que o usuário deve realizar
    private String actionType; // "CHECKIN" ou "CHECKOUT"

    // Mensagem explicativa
    private String message;

    // Status do check anterior (se houver)
    private Boolean hasCheckedIn;
    private LocalDateTime checkinTime;
    private Boolean hasCheckedOut;
    private LocalDateTime checkoutTime;

    // Validações
    private Boolean canPerformAction; // true se pode fazer a ação agora
    private String validationMessage; // mensagem de erro se não puder
}