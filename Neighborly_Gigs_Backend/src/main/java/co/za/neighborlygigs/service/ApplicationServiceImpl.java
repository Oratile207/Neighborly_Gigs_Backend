package co.za.neighborlygigs.service;

import co.za.neighborlygigs.domain.Application;
import co.za.neighborlygigs.domain.Notification;
import co.za.neighborlygigs.domain.Task;
import co.za.neighborlygigs.domain.User;
import co.za.neighborlygigs.domain.enums.TaskStatus;

import co.za.neighborlygigs.factory.ApplicationFactory;
import co.za.neighborlygigs.factory.NotificationFactory;
import co.za.neighborlygigs.repository.TaskRepository;
import co.za.neighborlygigs.repository.ApplicationRepository;
import co.za.neighborlygigs.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final TaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;

    public ApplicationServiceImpl(TaskRepository taskRepository,
                                  ApplicationRepository applicationRepository,
                                  NotificationRepository notificationRepository) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Application applyToTask(Long taskId, String message, User applicant) {
        Task task = taskRepository.findByIdAndStatus(taskId, TaskStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("Open task not found"));

        // Prevent duplicate applications
        if (applicationRepository.findByTask_IdAndApplicant_Id(taskId, applicant.getId()).isPresent()) {
            throw new RuntimeException("You've already applied to this task");
        }

        Application application = ApplicationFactory.createApplication(task, applicant, message);
        Application savedApp = applicationRepository.save(application);

        // Notify poster
        notificationRepository.save(
                NotificationFactory.createNewApplicationNotification(
                        task.getPoster(),
                        applicant.getFirstName() + " " + applicant.getLastName(),
                        task.getTitle()
                )
        );

        return savedApp;
    }
}