package com.project.locusapi.repository.dashboard;

import com.project.locusapi.model.dashboard.DashboardColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DashboardColumnRepository extends JpaRepository<DashboardColumn, UUID> {
}