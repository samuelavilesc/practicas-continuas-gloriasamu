package es.practicascontinuas.todo.task;

import java.time.LocalDate;

public record TaskResponse(
    Long id, String title, String description, TaskStatus status, int priority, LocalDate dueDate) {

  public static TaskResponse from(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getStatus(),
        task.getPriority(),
        task.getDueDate());
  }
}
