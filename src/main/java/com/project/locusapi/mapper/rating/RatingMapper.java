package com.project.locusapi.mapper.rating;

import com.project.locusapi.dto.rating.RatingRequestDTO;
import com.project.locusapi.dto.rating.RatingResponseDTO;
import com.project.locusapi.model.RatingModel;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingModel toRatingModel(RatingRequestDTO ratingDTO) {
        return RatingModel.builder()
                        .ratingValue(ratingDTO.ratingValue()).build();
    }

    public RatingResponseDTO toRatingResponseDTO(RatingModel ratingModel) {
        return new RatingResponseDTO(ratingModel.getRatingValue(),
                                    ratingModel.getRentableAddress().getId(),
                                    ratingModel.getUser().getId());
    }

}
