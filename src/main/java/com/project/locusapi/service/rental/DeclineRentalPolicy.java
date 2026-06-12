package com.project.locusapi.service.rental;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.model.Rental;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeclineRentalPolicy implements RentalStatusTransitionPolicy {

    @Override
    public RentalStatus getTargetStatus() {
        return RentalStatus.DECLINED;
    }

    @Override
    public void validate(Rental rental, UUID actorId) {
        boolean isHost = rental.getRentableAddress().getUser().getId().equals(actorId);
        if (!isHost) {
            throw new AccessDeniedException("Apenas o anfitrião pode recusar a reserva.");
        }

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new IllegalArgumentException("Só é possível responder a reservas pendentes.");
        }
    }
}
