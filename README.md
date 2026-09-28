# To-Do API

API REST de gestion de tareas, creada para el Boletin 1 de Practicas Continuas y ampliada en pareja en el Boletin 2 (GitHub y Pull Requests).

**Autores:** [Samuel Avilés Conesa](https://github.com/samuelavilesc) · [Gloria Sánchez](https://github.com/glooriasanchezzz)

## Requisitos

- Java 21 (LTS) o superior
- Maven 3.9+

## Arranque

```bash
mvn clean package
java -jar target/todo-api-0.0.1-SNAPSHOT.jar
```

La API queda disponible en `http://localhost:8080/api/tasks` (puerto 8080 por defecto) y responde siempre en JSON.

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/tasks | Lista todas las tareas |
| GET | /api/tasks?status=PENDING | Filtra por estado (PENDING, IN_PROGRESS, COMPLETED) |
| GET | /api/tasks/page?page=0&size=10&sort=id | Lista paginada y ordenada de tareas |
| GET | /api/tasks/priority?min=N | Filtra tareas con prioridad mayor o igual a N |
| GET | /api/tasks/search?q=texto | Busca tareas cuyo titulo o descripcion contienen el texto (sin distinguir mayusculas/minusculas) |
| GET | /api/tasks/{id} | Obtiene una tarea por id |
| POST | /api/tasks | Crea una tarea |
| PUT | /api/tasks/{id} | Actualiza una tarea |
| DELETE | /api/tasks/{id} | Elimina una tarea |

## Ejemplo

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Preparar el boletin","description":"Documentar los commits","status":"PENDING","priority":3,"dueDate":"2026-09-28"}'

curl http://localhost:8080/api/tasks
```

## Desarrollo

Despues de clonar el repositorio, activa el hook de formateo versionado:

```bash
git config core.hooksPath .githooks
```

Comprueba el formato con `mvn spotless:check` y corrigelo con `mvn spotless:apply`.

Para el flujo de contribución (ramas, Pull Requests, revisión), consulta [CONTRIBUTING.md](CONTRIBUTING.md).
