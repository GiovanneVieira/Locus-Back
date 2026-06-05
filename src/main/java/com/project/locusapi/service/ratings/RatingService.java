package com.project.locusapi.service.ratings;

import com.project.locusapi.dto.rating.RatingAvgResponseDTO;
import com.project.locusapi.dto.rating.RatingRequestDTO;
import com.project.locusapi.dto.rating.RatingResponseDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.RatingsNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.rating.RatingMapper;
import com.project.locusapi.model.RatingModel;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.repository.ratings.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;
    private final RentableAddressRepository rentableAddressRepository;

    @Transactional
    public RatingModel postRatingToAddress(RatingRequestDTO ratingDTO, UserModel principal, UUID addressId) {
        if (principal == null) {
            throw new UserNotFoundException("Usuário não autenticado");
        }

        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException(principal.getId()));

        var rentableAddress = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));

        var ratingModel = this.ratingMapper.toRatingModel(ratingDTO);

        ratingModel.setUser(user);
        ratingModel.setRentableAddress(rentableAddress);

        rentableAddress.getRatings().add(ratingModel);
        user.getRatings().add(ratingModel);

        return ratingRepository.save(ratingModel);
    }

    @Transactional
    public List<RatingResponseDTO> getAllRatingsToAddress(UUID addressId) {

        var ratings = this.ratingRepository.findRatingsByAddressId(addressId);
        if(ratings.isEmpty()) {
            throw new RatingsNotFoundException("Rating not found");
        }
        return ratings.stream().map(ratingMapper::toRatingResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public RatingAvgResponseDTO getAverageRatingsToAddress(UUID addressId){

        var rawRatings = this.ratingRepository.findRatingsByAddressId(addressId);
        if(rawRatings.isEmpty()) {
            throw new RatingsNotFoundException("Rating not found");
        }

        Double avgRating = rawRatings.stream()
                                     .mapToDouble(RatingModel::getRatingValue)
                                     .average()
                                     .orElse(0.0);

        List<UUID> userIds = rawRatings.stream().map(rating -> rating.getUser().getId()).toList();

        return new RatingAvgResponseDTO(avgRating, userIds, addressId);
    }

}