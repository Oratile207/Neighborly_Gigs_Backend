package co.za.neighborlygigs.service;

import co.za.neighborlygigs.domain.Task;
import co.za.neighborlygigs.domain.enums.TaskCategory;
import co.za.neighborlygigs.domain.User;

import java.math.BigDecimal;
import java.util.List;

public interface TaskService extends IService<Task, Long> {
    Task createTask(String title, String description, TaskCategory category,
                    BigDecimal budget, String address, User poster);
    List<Task> getAllOpenTasks();
    Task assignTask(Long taskId, Long applicantId, User poster);
    Task completeTask(Long taskId, User currentUser);
}