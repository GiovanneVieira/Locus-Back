package com.project.locusapi.service.rental;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.model.Rental;

import java.util.UUID;

public interface RentalStatusTransitionPolicy {
    RentalStatus getTargetStatus();

    void validate(Rental rental, UUID actorId);
}
