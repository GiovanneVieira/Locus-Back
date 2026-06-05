package com.project.locusapi.dto.comments;

public abstract class CommentResponseDTO {

    public String comment;

    public CommentResponseDTO(String comment) {
        this.comment = comment;
    }
}
