package com.project.locusapi.repository.dashboard;

import com.project.locusapi.model.dashboard.DashboardBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DashboardBoardRepository extends JpaRepository<DashboardBoard, UUID> {

    @Query("""
            select distinct b from dashboard_board b
            left join fetch b.columns c
            left join fetch c.tasks t
            join fetch b.owner o
            where o.email = :email
            """)
    Optional<DashboardBoard> findDetailedByOwnerEmail(@Param("email") String email);
}