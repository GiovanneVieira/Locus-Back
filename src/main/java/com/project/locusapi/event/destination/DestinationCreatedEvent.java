package com.project.locusapi.event.destination;

import java.util.UUID;

public record DestinationCreatedEvent(UUID destinationId, String city) {}
