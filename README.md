# To-Do API

API REST de gestion de tareas, creada para el Boletin 1 de Practicas Continuas, ampliada en pareja en el Boletin 2 (GitHub y Pull Requests) y contenerizada con Docker en el Boletin 3.

**Autores:** [Samuel Avilés Conesa](https://github.com/samuelavilesc) · [Gloria Sánchez](https://github.com/glooriasanchezzz)

## Requisitos

- Docker y Docker Compose (forma recomendada de arrancar el proyecto)
- Para compilar fuera de Docker: Java 21 (LTS) o superior y Maven 3.9+

## Arranque con Docker

La API usa PostgreSQL como base de datos. Docker Compose levanta los dos contenedores (`api` y `db`), espera a que la base de datos esté lista y conserva los datos en el volumen `db-data`.

1. Crea tu fichero de configuración local a partir de la plantilla y cambia la contraseña. `.env` nunca se sube al repositorio:

   ```bash
   cp .env.example .env
   ```

2. Construye la imagen y levanta el stack:

   ```bash
   docker compose up --build -d
   docker compose ps            # espera a que api y db aparezcan como (healthy)
   ```

3. Comprueba que responde:

   ```bash
   curl http://localhost:8080/actuator/health   # {"status":"UP"}
   curl http://localhost:8080/api/tasks
   ```

La API queda disponible en `http://localhost:8080/api/tasks` (puerto 8080 por defecto) y responde siempre en JSON. PostgreSQL se publica solo en `127.0.0.1:5432` para conectarte con un cliente SQL.

| Comando | Efecto |
|---|---|
| `docker compose logs -f api` | Ver los logs de la API |
| `docker compose down` | Para y borra los contenedores; **los datos se conservan** |
| `docker compose down -v` | Borra también el volumen: **se pierden los datos** |

> Si cambias las credenciales de `.env` con el volumen ya creado, PostgreSQL seguirá con las antiguas. En desarrollo, ejecuta `docker compose down -v` y vuelve a levantar el stack.

## Arranque con Dev Container

El repositorio incluye `.devcontainer/devcontainer.json` con el entorno de desarrollo completo (JDK 21, Maven y Docker dentro del contenedor), así que no hace falta instalar nada salvo Docker y VS Code.

1. Instala la extensión **Dev Containers** en VS Code.
2. Abre la carpeta del repositorio y ejecuta *Dev Containers: Reopen in Container*.
3. Al crearse, el contenedor copia `.env.example` a `.env` si no existe, activa el hook de `.githooks` y descarga las dependencias de Maven.
4. Dentro del contenedor, levanta el stack igual que arriba:

   ```bash
   docker compose up --build
   ```

5. Los puertos 8080 y 5432 se reenvían a tu máquina: abre `http://127.0.0.1:8080/actuator/health` en tu navegador.

## Arranque sin Docker

Necesitas un PostgreSQL accesible en `localhost:5432` con una base de datos `tareas`:

```bash
mvn clean package
SPRING_DATASOURCE_USERNAME=app SPRING_DATASOURCE_PASSWORD=... java -jar target/todo-api-0.0.1-SNAPSHOT.jar
```

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
| GET | /actuator/health | Estado de la aplicacion (lo usan los healthchecks de Docker) |

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
