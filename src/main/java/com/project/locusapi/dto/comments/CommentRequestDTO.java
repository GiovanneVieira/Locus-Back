package com.project.locusapi.dto.comments;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CommentRequestDTO {

    private String comment;

    public CommentRequestDTO(String comment) {
        this.comment = comment;
    }

}
