package es.practicascontinuas.todo.task;

public class TaskNotFoundException extends RuntimeException {

  public TaskNotFoundException(Long id) {
    super("No existe una tarea con id " + id);
  }
}
