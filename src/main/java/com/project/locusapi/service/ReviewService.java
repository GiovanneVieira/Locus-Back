package com.project.locusapi.service;

import com.project.locusapi.dto.review.ReviewRequestDTO;
import com.project.locusapi.dto.review.ReviewResponseDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.ReviewNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.model.Review;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.ReviewRepository;
import com.project.locusapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentableAddressRepository rentableAddressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> listByAddress(UUID addressId) {
        return reviewRepository.findAllByRentableAddressIdOrderByCreatedAtDesc(addressId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Cria ou atualiza a avaliação do usuário para o imóvel.
     * Regra de negócio: uma avaliação por usuário por imóvel — a nova substitui a anterior.
     */
    @Transactional
    public ReviewResponseDTO createOrUpdate(UUID addressId, UUID userId, ReviewRequestDTO dto) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));

        if (address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Você não pode avaliar o seu próprio imóvel.");
        }

        var review = reviewRepository.findByRentableAddressIdAndAuthorId(addressId, userId)
                .orElseGet(() -> {
                    var author = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));
                    return Review.builder()
                            .author(author)
                            .rentableAddress(address)
                            .build();
                });

        review.setRating(dto.rating());
        review.setComment(dto.comment().trim());

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(UUID reviewId, UUID userId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Você só pode remover a sua própria avaliação.");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponseDTO toResponse(Review review) {
        var author = review.getAuthor();
        return new ReviewResponseDTO(
                review.getId(),
                review.getRentableAddress().getId(),
                author.getId(),
                author.getName(),
                author.getPfpUrl(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
