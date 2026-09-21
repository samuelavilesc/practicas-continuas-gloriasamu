package es.practicascontinuas.todo.task;

public class InvalidTaskStateException extends RuntimeException {

  public InvalidTaskStateException(String message) {
    super(message);
  }
}
