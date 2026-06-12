package com.project.locusapi.service.rental;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.model.Rental;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RentalStatusTransitionService {

    private final Map<RentalStatus, RentalStatusTransitionPolicy> policyMap;

    public RentalStatusTransitionService(List<RentalStatusTransitionPolicy> policies) {
        this.policyMap = policies.stream()
                .collect(Collectors.toMap(RentalStatusTransitionPolicy::getTargetStatus, Function.identity()));
    }

    public void validate(Rental rental, UUID actorId, RentalStatus targetStatus) {
        RentalStatusTransitionPolicy policy = policyMap.get(targetStatus);
        if (policy == null) {
            throw new IllegalArgumentException("Transição de status inválida.");
        }
        policy.validate(rental, actorId);
    }
}
