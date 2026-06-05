package com.project.locusapi.dto.comments.address;

import com.project.locusapi.dto.comments.CommentResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddressCommentResponseDTO extends CommentResponseDTO {

    private final UUID userId;
    private final UUID addressId;

    public AddressCommentResponseDTO(String comment, UUID userId, UUID addressId) {
        super(comment);
        this.userId = userId;
        this.addressId = addressId;
    }

}
