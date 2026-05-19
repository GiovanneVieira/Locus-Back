package com.project.locusapi.repository.s3file;

import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentableAddressImageRepository extends BaseFileRepository<RentableAddressImage> {

    // O Spring Data resolve automaticamente navegando de 'address' para o 'id' dele
    List<RentableAddressImage> findByAddressId(UUID addressId);

    Optional<RentableAddressImage> findByAddressIdAndIsMainTrue(UUID addressId);

    Page<RentableAddressImage> findByAddressIdAndOriginalNameContainingIgnoreCase(UUID addressId, String originalName, Pageable pageable);

    List<RentableAddressImage> findByHostId(UUID hostId);

    @Modifying
    @Query("UPDATE rentable_address_image_table i SET i.isMain = false WHERE i.address.id = :addressId")
    void disableMainImageForAddress(@Param("addressId") UUID addressId);
}
