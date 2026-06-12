package com.project.locusapi.service.ai;

import com.project.locusapi.dto.destination.DestinationAIResponse;
import com.project.locusapi.event.destination.DestinationCreatedEvent;
import com.project.locusapi.exception.business.DestinationAIException;
import com.project.locusapi.service.DestinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationAIListener {

    private final DestinationAIService destinationAIService;
    private final DestinationService destinationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDestinationCreated(DestinationCreatedEvent event) {
        try {
            DestinationAIResponse response = destinationAIService.recommendTouristPoints(event.city());
            destinationService.updateTouristPoints(event.destinationId(), response);
        } catch (DestinationAIException ex) {
            log.warn("Falha ao enriquecer destino {} com pontos turísticos: {}", event.destinationId(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Erro inesperado ao enriquecer destino {} com pontos turísticos", event.destinationId(), ex);
        }
    }
}
