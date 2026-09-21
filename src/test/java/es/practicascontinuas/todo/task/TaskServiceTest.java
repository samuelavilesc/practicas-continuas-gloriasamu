package es.practicascontinuas.todo.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    private TaskRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new TaskRequest(
                "Preparar el boletin", "Documentar los commits", TaskStatus.PENDING, 3, LocalDate.now().plusDays(7));
    }

    @Test
    void noPermiteCrearUnaTareaConTituloDuplicado() {
        when(repository.findByTitleIgnoreCase("Preparar el boletin"))
                .thenReturn(Optional.of(new Task("Preparar el boletin", "otra", TaskStatus.PENDING, 1, LocalDate.now())));

        assertThatThrownBy(() -> service.create(validRequest)).isInstanceOf(DuplicateTaskException.class);
    }

    @Test
    void creaUnaTareaCuandoElTituloEsUnico() {
        when(repository.findByTitleIgnoreCase("Preparar el boletin")).thenReturn(Optional.empty());
        when(repository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = service.create(validRequest);

        assertThat(response.title()).isEqualTo("Preparar el boletin");
        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void lanzaTaskNotFoundExceptionSiLaTareaNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void devuelveLaTareaCuandoExiste() {
        Task task = new Task("Repasar Maven", "Ciclo de vida", TaskStatus.PENDING, 2, LocalDate.now());
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = service.findById(1L);

        assertThat(response.title()).isEqualTo("Repasar Maven");
    }

    @Test
    void noPermiteReactivarUnaTareaCompletada() {
        Task completedTask = new Task("Entregar boletin", "desc", TaskStatus.COMPLETED, 1, LocalDate.now());
        when(repository.findById(5L)).thenReturn(Optional.of(completedTask));

        TaskRequest reactivar = new TaskRequest(
                "Entregar boletin", "desc", TaskStatus.PENDING, 1, LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.update(5L, reactivar)).isInstanceOf(InvalidTaskStateException.class);
    }

    @Test
    void lanzaTaskNotFoundExceptionAlBorrarUnaTareaInexistente() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L)).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void borraLaTareaCuandoExiste() {
        Task task = new Task("Limpiar target", "desc", TaskStatus.PENDING, 1, LocalDate.now());
        when(repository.findById(7L)).thenReturn(Optional.of(task));

        service.delete(7L);

        verify(repository).delete(task);
    }

    @Test
    void filtraLasTareasPorEstado() {
        Task pendiente = new Task("Tarea pendiente", "desc", TaskStatus.PENDING, 1, LocalDate.now());
        when(repository.findByStatus(TaskStatus.PENDING)).thenReturn(List.of(pendiente));

        List<TaskResponse> resultado = service.findAll(TaskStatus.PENDING);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).status()).isEqualTo(TaskStatus.PENDING);
    }
}
