package com.project.locusapi.service.comment;

import com.project.locusapi.dto.comments.address.AddressCommentRequestDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.comment.AddressCommentMapper;
import com.project.locusapi.model.CommentModel;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.repository.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressCommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RentableAddressRepository rentableAddressRepository;
    private final AddressCommentMapper commentMapper;

    @Transactional // Garante a consistência física no banco de dados
    public CommentModel postComment(AddressCommentRequestDTO requestDto) {

        var user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        var address = rentableAddressRepository.findById(requestDto.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        var comment = commentMapper.toBuildModel(requestDto).user(user).rentableAddress(address).build();
        comment.setUser(user);
        comment.setRentableAddress(address);

        // 4. Salvamos APENAS o comentário.
        // O Hibernate já sabe mapear o ID do usuário e do endereço na tabela de comentários.
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentModel> getCommentsByAddressId(UUID addressId) {
        if (!rentableAddressRepository.existsById(addressId)) {
            throw new AddressNotFoundException("Address not found");
        }
        return commentRepository.findByAddressId(addressId);
    }
}