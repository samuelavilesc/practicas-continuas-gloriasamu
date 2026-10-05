package es.practicascontinuas.todo.task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  Optional<Task> findByTitleIgnoreCase(String title);

  List<Task> findByStatus(TaskStatus status);

  List<Task> findByPriorityGreaterThanEqual(int minPriority);

  List<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
      String title, String description);

  List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
}
