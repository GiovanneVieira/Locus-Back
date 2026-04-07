package com.project.locusapi.repository.dashboard;

import com.project.locusapi.model.dashboard.DashboardTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DashboardTaskRepository extends JpaRepository<DashboardTask, UUID> {

    @Query("""
            select t from dashboard_task t
            join fetch t.column c
            join fetch c.board b
            join fetch b.owner o
            where t.id = :taskId and o.email = :email
            """)
    Optional<DashboardTask> findOwnedTask(@Param("email") String email, @Param("taskId") UUID taskId);
}