package com.project.locusapi.repository.ratings;

import com.project.locusapi.model.RatingModel;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<RatingModel, UUID> {
    @Query("SELECT r from rating_table r JOIN  FETCH r.rentableAddress a WHERE a.id = :addressId ")
    List<RatingModel> findRatingsByAddressId(@Param("addressId") UUID addressId);
}
