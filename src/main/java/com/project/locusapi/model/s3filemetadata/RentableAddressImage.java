package com.project.locusapi.model.s3filemetadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.locusapi.model.RentableAddressModel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity(name = "rentable_address_image_table") // O Hibernate criará essa tabela física com TODOS os campos
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "rentable_address_image_table",
        indexes = {
                @Index(name = "idx_img_address", columnList = "address_id"),
                @Index(name = "idx_img_host", columnList = "host_id")
        }
)
public class RentableAddressImage extends AbstractFileMetadata {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = true)
    @JsonIgnoreProperties("images")
    private RentableAddressModel address;

    @Column(name = "host_id", nullable = false)
    private UUID hostId;

    @Builder.Default
    @Column(nullable = false)
    private boolean isMain = false;
}
