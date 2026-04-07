package com.project.locusapi.model.dashboard;

import com.project.locusapi.constant.dashboard.TaskColumnCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "dashboard_column")
@Table(name = "dashboard_column")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskColumnCode code;

    @Column(nullable = false)
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private DashboardBoard board;

    @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DashboardTask> tasks = new ArrayList<>();

    public void addTask(DashboardTask task) {
        tasks.add(task);
        task.setColumn(this);
    }
}