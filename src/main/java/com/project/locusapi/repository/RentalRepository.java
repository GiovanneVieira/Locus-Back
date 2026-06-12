package com.project.locusapi.repository;

import com.project.locusapi.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {

    @Query("""
            select distinct r from rental_table r
            join fetch r.renter
            join fetch r.rentableAddress a
            join fetch a.user
            left join fetch a.images
            where r.renter.id = :renterId
            order by r.createdAt desc
            """)
    List<Rental> findBookingsWithDetailsByRenterId(@Param("renterId") UUID renterId);

    @Query("""
            select distinct r from rental_table r
            join fetch r.renter
            join fetch r.rentableAddress a
            join fetch a.user
            left join fetch a.images
            where a.user.id = :hostId
            order by r.createdAt desc
            """)
    List<Rental> findBookingsWithDetailsByHostId(@Param("hostId") UUID hostId);

    @Query("""
            select distinct r from rental_table r
            join fetch r.renter
            join fetch r.rentableAddress a
            join fetch a.user
            left join fetch a.images
            where r.id = :rentalId
            """)
    Optional<Rental> findByIdWithDetails(@Param("rentalId") UUID rentalId);
}
