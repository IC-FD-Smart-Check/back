package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.SubscriptionRequest;
import org.fdsmartcheck.dto.response.SubscriptionResponse;
import org.fdsmartcheck.model.SubEvent;
import org.fdsmartcheck.model.Subscription;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.repository.SubEventRepository;
import org.fdsmartcheck.repository.SubscriptionRepository;
import org.fdsmartcheck.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubEventRepository subEventRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request) {
        SubEvent subEvent = subEventRepository.findById(request.getSubEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (subscriptionRepository.existsBySubEventIdAndUserId(subEvent.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já está inscrito neste subevento");
        }

        Subscription subscription = Subscription.builder()
                .subEvent(subEvent)
                .user(user)
                .build();

        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public void unsubscribe(String subEventId, String userId) {
        if (!subEventRepository.existsById(subEventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        if (!subscriptionRepository.existsBySubEventIdAndUserId(subEventId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada");
        }

        subscriptionRepository.deleteBySubEventIdAndUserId(subEventId, userId);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listBySubEvent(String subEventId) {
        if (!subEventRepository.existsById(subEventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado");
        }

        return subscriptionRepository.findBySubEventId(subEventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())
                .userName(subscription.getUser().getName())
                .userEmail(subscription.getUser().getEmail())
                .subEventId(subscription.getSubEvent().getId())
                .subEventTitle(subscription.getSubEvent().getTitle())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
