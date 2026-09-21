package es.practicascontinuas.todo.task;

public class DuplicateTaskException extends RuntimeException {

    public DuplicateTaskException(String title) {
        super("Ya existe una tarea con el titulo '" + title + "'");
    }
}
