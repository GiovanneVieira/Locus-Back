package com.project.locusapi.service.dashboard;

import com.project.locusapi.constant.dashboard.TaskColumnCode;
import com.project.locusapi.dto.dashboard.*;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.model.dashboard.DashboardBoard;
import com.project.locusapi.model.dashboard.DashboardColumn;
import com.project.locusapi.model.dashboard.DashboardTask;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.repository.dashboard.DashboardBoardRepository;
import com.project.locusapi.repository.dashboard.DashboardColumnRepository;
import com.project.locusapi.repository.dashboard.DashboardTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardBoardRepository dashboardBoardRepository;
    private final DashboardColumnRepository dashboardColumnRepository;
    private final DashboardTaskRepository dashboardTaskRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardResponseDTO getBoardForUser(String email) {
        var board = findOrCreateBoard(email);
        return toBoardResponse(board);
    }

    @Transactional
    public BoardTaskResponseDTO createTask(String email, CreateTaskRequestDTO requestDTO) {
        var board = findOrCreateBoard(email);
        var column = findBoardColumn(board, requestDTO.columnId());

        var nextPosition = requestDTO.position() != null ? requestDTO.position() : getNextPosition(column);

        var task = DashboardTask.builder()
                .jiraCode(requestDTO.jiraCode().trim())
                .title(requestDTO.title().trim())
                .description(isBlank(requestDTO.description()) ? null : requestDTO.description().trim())
                .priority(requestDTO.priority())
                .position(nextPosition)
                .storyPoints(requestDTO.storyPoints())
                .assignee(isBlank(requestDTO.assignee()) ? null : requestDTO.assignee().trim())
                .column(column)
                .build();

        var savedTask = dashboardTaskRepository.save(task);
        return toTaskResponse(savedTask);
    }

    @Transactional
    public BoardTaskResponseDTO updateTask(String email, UUID taskId, UpdateTaskRequestDTO requestDTO) {
        var task = dashboardTaskRepository.findOwnedTask(email, taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task não encontrada"));

        if (!isBlank(requestDTO.jiraCode())) {
            task.setJiraCode(requestDTO.jiraCode().trim());
        }
        if (!isBlank(requestDTO.title())) {
            task.setTitle(requestDTO.title().trim());
        }
        if (requestDTO.description() != null) {
            task.setDescription(isBlank(requestDTO.description()) ? null : requestDTO.description().trim());
        }
        if (requestDTO.priority() != null) {
            task.setPriority(requestDTO.priority());
        }
        if (requestDTO.storyPoints() != null) {
            task.setStoryPoints(requestDTO.storyPoints());
        }
        if (requestDTO.assignee() != null) {
            task.setAssignee(isBlank(requestDTO.assignee()) ? null : requestDTO.assignee().trim());
        }
        if (requestDTO.columnId() != null) {
            var targetBoard = task.getColumn().getBoard();
            var targetColumn = findBoardColumn(targetBoard, requestDTO.columnId());
            task.setColumn(targetColumn);
        }
        if (requestDTO.position() != null) {
            task.setPosition(requestDTO.position());
        } else if (requestDTO.columnId() != null) {
            task.setPosition(getNextPosition(task.getColumn()));
        }

        var savedTask = dashboardTaskRepository.save(task);
        return toTaskResponse(savedTask);
    }

    @Transactional
    public void deleteTask(String email, UUID taskId) {
        var task = dashboardTaskRepository.findOwnedTask(email, taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task não encontrada"));
        dashboardTaskRepository.delete(task);
    }

    private DashboardBoard findOrCreateBoard(String email) {
        return dashboardBoardRepository.findDetailedByOwnerEmail(email)
                .orElseGet(() -> createDefaultBoard(email));
    }

    private DashboardBoard createDefaultBoard(String email) {
        UserModel owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));

        var board = DashboardBoard.builder()
                .name("Sprint atual - Implementação")
                .description("Quadro da sprint sincronizado entre front-end e back-end.")
                .owner(owner)
                .build();

        board.addColumn(DashboardColumn.builder()
                .title("Planejado")
                .code(TaskColumnCode.TODO)
                .position(1)
                .build());

        board.addColumn(DashboardColumn.builder()
                .title("Em andamento")
                .code(TaskColumnCode.IN_PROGRESS)
                .position(2)
                .build());

        board.addColumn(DashboardColumn.builder()
                .title("Concluído")
                .code(TaskColumnCode.DONE)
                .position(3)
                .build());

        return dashboardBoardRepository.save(board);
    }

    private DashboardColumn findBoardColumn(DashboardBoard board, UUID columnId) {
        return board.getColumns().stream()
                .filter(column -> column.getId().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Coluna não encontrada no quadro do usuário"));
    }

    private Integer getNextPosition(DashboardColumn column) {
        return column.getTasks().stream()
                .map(DashboardTask::getPosition)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private BoardResponseDTO toBoardResponse(DashboardBoard board) {
        var columns = board.getColumns().stream()
                .sorted(Comparator.comparing(DashboardColumn::getPosition))
                .map(this::toColumnResponse)
                .toList();

        return new BoardResponseDTO(
                board.getId(),
                board.getName(),
                board.getDescription(),
                columns
        );
    }

    private BoardColumnResponseDTO toColumnResponse(DashboardColumn column) {
        var tasks = column.getTasks().stream()
                .sorted(Comparator.comparing(DashboardTask::getPosition))
                .map(this::toTaskResponse)
                .toList();

        return new BoardColumnResponseDTO(
                column.getId(),
                column.getTitle(),
                column.getCode(),
                column.getPosition(),
                tasks
        );
    }

    private BoardTaskResponseDTO toTaskResponse(DashboardTask task) {
        return new BoardTaskResponseDTO(
                task.getId(),
                task.getJiraCode(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getPosition(),
                task.getStoryPoints(),
                task.getAssignee(),
                task.getColumn().getId(),
                task.getColumn().getCode(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}