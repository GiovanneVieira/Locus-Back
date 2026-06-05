package com.project.locusapi.dto.rating;

import java.util.List;
import java.util.UUID;

public record RatingAvgResponseDTO (Double ratingAvg,
                                    List<UUID> userIds,
                                    UUID address){
}
