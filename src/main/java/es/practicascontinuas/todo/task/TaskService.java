package es.practicascontinuas.todo.task;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository repository;

  public TaskService(TaskRepository repository) {
    this.repository = repository;
  }

  public List<TaskResponse> findAll(TaskStatus status) {
    List<Task> tasks = status == null ? repository.findAll() : repository.findByStatus(status);
    return tasks.stream().map(TaskResponse::from).toList();
  }

  public Page<TaskResponse> findAllPaged(Pageable pageable) {
    return repository.findAll(pageable).map(TaskResponse::from);
  }

  public List<TaskResponse> findByMinPriority(int minPriority) {
    return repository.findByPriorityGreaterThanEqual(minPriority).stream()
        .map(TaskResponse::from)
        .toList();
  }

  public List<TaskResponse> search(String q) {
    return repository
        .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q)
        .stream()
        .map(TaskResponse::from)
        .toList();
  }

  public TaskResponse findById(Long id) {
    return TaskResponse.from(getOrThrow(id));
  }

  public TaskResponse create(TaskRequest request) {
    repository
        .findByTitleIgnoreCase(request.title())
        .ifPresent(
            existing -> {
              throw new DuplicateTaskException(request.title());
            });
    Task task =
        new Task(
            request.title(),
            request.description(),
            request.status(),
            request.priority(),
            request.dueDate());
    return TaskResponse.from(repository.save(task));
  }

  public TaskResponse update(Long id, TaskRequest request) {
    Task task = getOrThrow(id);

    if (task.getStatus() == TaskStatus.COMPLETED && request.status() != TaskStatus.COMPLETED) {
      throw new InvalidTaskStateException("Una tarea completada no se puede reactivar");
    }

    repository
        .findByTitleIgnoreCase(request.title())
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(
            existing -> {
              throw new DuplicateTaskException(request.title());
            });

    task.applyChanges(request);
    return TaskResponse.from(repository.save(task));
  }

  public void delete(Long id) {
    Task task = getOrThrow(id);
    repository.delete(task);
  }

  private Task getOrThrow(Long id) {
    return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
  }
}
