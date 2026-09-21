package es.practicascontinuas.todo.task;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskRequest(
    @NotBlank(message = "El titulo es obligatorio") String title,
    String description,
    @NotNull(message = "El estado es obligatorio") TaskStatus status,
    @Min(value = 1, message = "La prioridad minima es 1")
        @Max(value = 5, message = "La prioridad maxima es 5")
        int priority,
    @NotNull(message = "La fecha limite es obligatoria")
        @FutureOrPresent(message = "La fecha limite no puede estar en el pasado")
        LocalDate dueDate) {}
