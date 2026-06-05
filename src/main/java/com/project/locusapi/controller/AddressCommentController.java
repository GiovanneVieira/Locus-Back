package com.project.locusapi.controller;

import com.project.locusapi.dto.comments.address.AddressCommentRequestDTO;
import com.project.locusapi.mapper.comment.AddressCommentMapper;
import com.project.locusapi.service.comment.AddressCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/address/rentable/comment")
@RequiredArgsConstructor
public class AddressCommentController {

    private final AddressCommentService addressCommentService;
    private final AddressCommentMapper addressCommentMapper;


//    TODO: Extrair usuario autenticado para inserir no DTO

    @PostMapping()
    public ResponseEntity<?> postCommentToAddress(@RequestBody AddressCommentRequestDTO requestDTO){
        var comment = this.addressCommentService.postComment(requestDTO);
        return ResponseEntity.ok().body(addressCommentMapper.toResponseDTO(comment));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<?> getAddressComments(@PathVariable("addressId") UUID addressId){
        var comments = this.addressCommentService.getCommentsByAddressId(addressId);
        var response = comments.stream().map(addressCommentMapper::toResponseDTO).toList();
        return ResponseEntity.ok().body(response);
    }


}
