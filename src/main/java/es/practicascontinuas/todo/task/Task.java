package es.practicascontinuas.todo.task;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private int priority;
    private LocalDate dueDate;

    protected Task() {
    }

    public Task(String title, String description, TaskStatus status, int priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void applyChanges(TaskRequest request) {
        this.title = request.title();
        this.description = request.description();
        this.status = request.status();
        this.priority = request.priority();
        this.dueDate = request.dueDate();
    }
}
