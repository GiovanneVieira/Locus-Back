package com.project.locusapi.mapper.comment;

import com.project.locusapi.dto.comments.address.AddressCommentRequestDTO;
import com.project.locusapi.dto.comments.address.AddressCommentResponseDTO;
import com.project.locusapi.model.CommentModel;
import org.springframework.stereotype.Component;

@Component
public class AddressCommentMapper {

    public  AddressCommentResponseDTO toResponseDTO(CommentModel model){
        return new AddressCommentResponseDTO(model.getComment(),
                                       model.getUser().getId(),
                                       model.getUser().getId());
    }

    public CommentModel.CommentModelBuilder toBuildModel(AddressCommentRequestDTO requestDTO){
        return CommentModel.builder()
                           .comment(requestDTO.getComment());
    }

}
