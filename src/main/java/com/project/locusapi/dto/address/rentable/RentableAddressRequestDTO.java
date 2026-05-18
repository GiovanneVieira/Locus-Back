package com.project.locusapi.dto.address.rentable;

import com.project.locusapi.dto.address.AddressRequestDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class RentableAddressRequestDTO extends AddressRequestDTO {

    @NotBlank(message = "O título do anúncio é obrigatório")
    private String title;

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    private String complement;

    @NotNull(message = "O preço por noite é obrigatório")
    @Min(value = 1, message = "O preço deve ser maior que zero")
    private Integer pricePerNight;

    @NotNull(message = "Informe a capacidade máxima de hóspedes")
    @Min(value = 1, message = "Deve aceitar pelo menos 1 hóspede")
    private Integer maxGuests;

    @NotEmpty(message = "Adicione pelo menos uma imagem")
    private List<String> imageUrls;

    private List<String> amenities;

    @NotNull(message = "A data de disponibilidade inicial é obrigatória")
    private LocalDate availableFrom;

    @NotNull(message = "A data de disponibilidade final é obrigatória")
    private LocalDate availableTo;
}