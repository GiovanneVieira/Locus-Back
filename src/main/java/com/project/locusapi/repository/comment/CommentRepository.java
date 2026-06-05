package com.project.locusapi.repository.comment;

import com.project.locusapi.model.CommentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentModel, UUID> {
    @Query("SELECT c FROM comment_table c WHERE c.rentableAddress.id = :addressId")
    List<CommentModel> findByAddressId(@Param("addressId") UUID addressId);
}
