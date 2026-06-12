package com.project.locusapi.service.rental;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.model.Rental;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CancelRentalPolicy implements RentalStatusTransitionPolicy {

    @Override
    public RentalStatus getTargetStatus() {
        return RentalStatus.CANCELLED;
    }

    @Override
    public void validate(Rental rental, UUID actorId) {
        boolean isGuest = rental.getRenter().getId().equals(actorId);
        if (!isGuest) {
            throw new AccessDeniedException("Apenas o hóspede pode cancelar a reserva.");
        }

        if (rental.getStatus() == RentalStatus.DECLINED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new IllegalArgumentException("Esta reserva não pode mais ser cancelada.");
        }
    }
}
