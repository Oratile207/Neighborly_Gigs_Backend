package co.za.neighborlygigs.service;

import co.za.neighborlygigs.domain.*;
import co.za.neighborlygigs.domain.enums.ApplicationStatus;
import co.za.neighborlygigs.domain.enums.TaskCategory;
import co.za.neighborlygigs.domain.enums.TaskStatus;
import co.za.neighborlygigs.factory.TaskFactory;
import co.za.neighborlygigs.factory.NotificationFactory;
import co.za.neighborlygigs.repository.*;
import co.za.neighborlygigs.util.GeoCodingUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final TransactionService transactionService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ApplicationRepository applicationRepository,
                           UserRepository userRepository,
                           NotificationRepository notificationRepository,
                           TransactionService transactionService) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.transactionService = transactionService;
    }

    // --- Generic CRUD methods ---
    @Override
    public Task create(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task read(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public Task update(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public boolean delete(Long id) {
        // Soft delete: mark as CANCELLED instead
        Task task = read(id);
        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);
        return true;
    }

    // --- Custom business methods ---
    @Override
    public Task createTask(String title, String description, TaskCategory category,
                           BigDecimal budget, String address, User poster) {
        String formattedAddress = GeoCodingUtil.formatAddressForGeocoding(address);
        Task task = TaskFactory.createTask(title, description, category, budget, formattedAddress, poster);
        return create(task);
    }

    @Override
    public List<Task> getAllOpenTasks() {
        return taskRepository.findByStatusOrderByIdDesc(TaskStatus.OPEN);
    }

    @Override
    public Task assignTask(Long taskId, Long applicantId, User poster) {
        Task task = read(taskId);
        if (!task.getPoster().getId().equals(poster.getId())) {
            throw new RuntimeException("Only poster can assign");
        }
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("Applicant not found"));
        task.setAssignedTo(applicant);
        task.setStatus(TaskStatus.ASSIGNED);

        // Update application
        Application app = applicationRepository.findByTask_IdAndApplicant_Id(taskId, applicantId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(ApplicationStatus.ACCEPTED);

        notificationRepository.save(
                NotificationFactory.createTaskAssignedNotification(applicant, task.getTitle())
        );

        return update(task);
    }

    @Override
    public Task completeTask(Long taskId, User currentUser) {
        Task task = read(taskId);
        if (!task.getPoster().getId().equals(currentUser.getId()) &&
                !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only involved parties can complete");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(java.time.LocalDateTime.now());
        transactionService.createTransaction(task);

        notificationRepository.save(
                NotificationFactory.createTaskCompletedNotification(task.getPoster(), task.getTitle())
        );
        notificationRepository.save(
                NotificationFactory.createTaskCompletedNotification(task.getAssignedTo(), task.getTitle())
        );

        return update(task);
    }
}

