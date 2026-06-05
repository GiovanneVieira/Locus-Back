package com.project.locusapi.dto.comments.address;

import com.project.locusapi.dto.comments.CommentRequestDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddressCommentRequestDTO extends CommentRequestDTO {

    private UUID userId;
    private UUID addressId;

    public AddressCommentRequestDTO(String comment, UUID userId, UUID addressId) {
        super(comment);
        this.userId = userId;
        this.addressId = addressId;
    }
}
