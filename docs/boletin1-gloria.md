# Trazabilidad — Boletín 1: Git y Maven

Documento con el proceso seguido para el Boletín 1: entorno, generación de la API, build, Git, Spotless y el conflicto de la parte final.

## Índice

- [Trazabilidad — Boletín 1: Git y Maven](#trazabilidad--boletín-1-git-y-maven)
  - [Índice](#índice)
  - [Entorno](#entorno)
  - [Parte A — Entorno](#parte-a--entorno)
  - [Parte B — La API](#parte-b--la-api)
  - [Parte C — Build y prueba local](#parte-c--build-y-prueba-local)
  - [Parte D — Git](#parte-d--git)
  - [Parte E — Spotless + hook](#parte-e--spotless--hook)
  - [Parte F — Conflicto](#parte-f--conflicto)

## Entorno

- Git 2.50.1 (Apple Git-155)
- OpenJDK 25.0.2 (Homebrew, aarch64)
- Maven 3.9.15
- macOS 26.6.2

La identidad de Git ya la tenía puesta de antes, así que en la parte A solo la comprobé.

---

## Parte A — Entorno

Comprobación básica de que todo responde desde la terminal:

```
gloria@MacBook-Air-de-Gloria ~ % git --version
git version 2.50.1 (Apple Git-155)
gloria@MacBook-Air-de-Gloria ~ % java -version
openjdk version "25.0.2" 2026-01-20
OpenJDK Runtime Environment Homebrew (build 25.0.2)
OpenJDK 64-Bit Server VM Homebrew (build 25.0.2, mixed mode, sharing)
gloria@MacBook-Air-de-Gloria ~ % mvn -version
Apache Maven 3.9.15 (98b2cdbfdb5f1ac8781f537ea9acccaed7922349)
Maven home: /opt/homebrew/Cellar/maven/3.9.15/libexec
Java version: 25.0.2, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home
Default locale: es_ES, platform encoding: UTF-8
OS name: "mac os x", version: "26.6.2", arch: "aarch64", family: "mac"
```

Y la config global:

```bash
git config --global user.name "Gloria Sánchez Alonso"
git config --global user.email "gloriasanchezalonso04@gmail.com"
git config --global pull.ff only
git config --global init.defaultBranch main
```

`pull.ff only` hace que si `git pull` no puede resolverse como fast-forward (osea si local y remoto han divergido), falle en vez de meter un merge automático sin que te des cuenta. Prefiero que falle y decidir yo qué hacer (merge o rebase) antes que encontrarme un commit de merge que no pedí.

---

## Parte B — La API

Hice una API de gestión de tareas con Spring Boot 3.3.5 + Maven, persistencia en memoria con un `HashMap` (nada de base de datos, así los tests no necesitan levantar Spring ni H2).

Estructura:

```
tasks-api/
├── pom.xml
├── .mvn/jvm.config
├── .githooks/pre-commit
├── .gitignore
├── .gitattributes
├── .editorconfig
└── src/
    ├── main/java/com/gloria/tasksapi/
    │   ├── TasksApiApplication.java
    │   ├── controller/TaskController.java
    │   ├── service/TaskService.java
    │   ├── repository/TaskRepository.java     (HashMap + AtomicLong para los ids)
    │   ├── model/Task.java, TaskStatus.java, Priority.java
    │   ├── dto/TaskRequest.java
    │   └── exception/
    │       ├── TaskNotFoundException.java
    │       ├── InvalidTaskException.java
    │       └── GlobalExceptionHandler.java
    └── test/java/.../service/TaskServiceTest.java
```

Tres capas: controlador (recibe la petición y devuelve la respuesta, sin lógica), servicio (las reglas de negocio, que es lo que testeo) y repositorio (el HashMap). Así los tests no dependen de Spring ni de HTTP.

Entidad `Task`: `id` (lo pone el repo), `title` (`@NotBlank`), `description` (opcional), `status` (enum, `@NotNull`), `priority` (enum, `@NotNull`), `dueDate` (`@NotNull` + no puede ser pasada — esto último se comprueba en el servicio porque depende del día actual, no es solo un tema de formato).

Errores centralizados con `@RestControllerAdvice` en vez de dejar que salten trazas al cliente:

```json
{
  "timestamp": "2026-09-21T19:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Tarea no encontrada con id: 99"
}
```

`TaskNotFoundException` → 404, `InvalidTaskException` → 400 (regla de negocio), y si falla el Bean Validation → 400 también.

Endpoints:

```
POST   /api/tasks                    → 201
GET    /api/tasks                    → 200
GET    /api/tasks?status=PENDING     → 200
GET    /api/tasks?priority=HIGH      → 200
GET    /api/tasks/{id}               → 200
PUT    /api/tasks/{id}               → 200
DELETE /api/tasks/{id}               → 204
```

(el filtro por prioridad lo añadí después, en la rama `feat/filtro-prioridad`, ver parte D)

7 tests en `TaskServiceTest`, sin mocks, instanciando el servicio directo con un repo real:

- `createTask_withPastDueDate_throwsInvalidTaskException`
- `createTask_withFutureDueDate_savesAndReturnsId`
- `findById_withNonExistentId_throwsTaskNotFoundException`
- `update_withNonExistentId_throwsTaskNotFoundException`
- `update_withPastDueDate_throwsInvalidTaskException`
- `delete_withNonExistentId_throwsTaskNotFoundException`
- `findByStatus_returnsOnlyMatchingTasks`

`mvn test` → 7 tests, 0 fallos, 0 errores.

**Nota Java 25:** al meter `googleJavaFormat` en Spotless petaba con:

```
java.lang.NoSuchMethodError: 'java.util.Queue
  com.sun.tools.javac.util.Log$DeferredDiagnosticHandler.getDiagnostics()'
```

Es porque `google-java-format` usa clases internas del compilador (`com.sun.tools.javac.util`) y en Java 25 cambió la firma de `getDiagnostics()`. La versión que trae Spotless 2.43.0 no lo soporta todavía. Cambié al formateador `eclipse`, que hace básicamente lo mismo (indentación, imports, espacios) sin depender de esas APIs internas. Dejé el `--add-opens` en `.mvn/jvm.config` por si en algún momento actualizan la librería y quiero volver a `googleJavaFormat`.

---

## Parte C — Build y prueba local

```bash
mvn clean package
```

clean borra `target/`, compile compila, test corre los 7 tests, package genera el jar.

```
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Es un fat jar (`target/tasks-api-0.0.1-SNAPSHOT.jar`), lleva todo dentro incluido Tomcat, así que se arranca directo:

```bash
java -jar target/tasks-api-0.0.1-SNAPSHOT.jar
```

Escucha en `localhost:8080`. Probé los endpoints con curl:

```bash
# crear tarea
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Estudiar Git","description":"Repasar commits, ramas y merges","status":"PENDING","priority":"HIGH","dueDate":"2026-12-31"}'
# → 201, id:1

curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Preparar entrega","description":"Subir el repositorio","status":"IN_PROGRESS","priority":"MEDIUM","dueDate":"2026-11-15"}'
# → 201, id:2

curl http://localhost:8080/api/tasks
# → 200, las dos tareas

curl "http://localhost:8080/api/tasks?status=PENDING"
# → 200, solo id:1

curl "http://localhost:8080/api/tasks?priority=HIGH"
# → 200, solo id:1

curl http://localhost:8080/api/tasks/1
# → 200

curl -s -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Estudiar Git","description":"Completado","status":"DONE","priority":"HIGH","dueDate":"2026-12-31"}'
# → 200

curl -X DELETE http://localhost:8080/api/tasks/2
# → 204

# id que no existe
curl -v http://localhost:8080/api/tasks/999
# → 404, {"status":404,"error":"Not Found","message":"Tarea no encontrada con id: 999"}

# fecha pasada
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Tarea imposible","description":"","status":"PENDING","priority":"LOW","dueDate":"2020-01-01"}'
# → 400, {"message":"La fecha límite no puede estar en el pasado"}
```

Los dos últimos son los que de verdad demuestran lo pedido: 404 en vez de traza, y 400 por la regla de negocio.

---

## Parte D — Git

```bash
git init
```

`.gitignore`: `target/`, `*.class`, `.idea/`, `.env`.

`.gitattributes`: `* text=auto eol=lf` y `*.jar binary` — normaliza los finales de línea a LF y evita que Git intente diffear los jars. Sin esto un script guardado en Windows con CRLF falla dentro del contenedor Linux (lo veremos en el Boletín 3).

`.editorconfig` aparte, en su propio commit, porque es config de herramientas y no código.

Commits siguiendo Conventional Commits (`tipo(ámbito): descripción`):

- `a31ef77` — `chore: proyecto Maven inicial de la API de tareas`
- `8c2901e` — `chore(editor): añade .editorconfig`
- `1cdd895` — `feat(api): añade filtro de búsqueda por prioridad`

Para el filtro de prioridad usé una rama en vez de commitear directo en main:

```bash
git switch -c feat/filtro-prioridad
# cambios en repositorio, servicio y controlador
git add . && git commit -m "feat(api): añade filtro de búsqueda por prioridad"
git switch main
git merge feat/filtro-prioridad
```

Como main no se había movido, el merge fue fast-forward (git solo mueve el puntero, no genera commit de merge):

```
a31ef77 ── 8c2901e ── 1cdd895   (main, feat/filtro-prioridad)
```

---

## Parte E — Spotless + hook

Plugin en el `pom.xml`:

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.43.0</version>
    <configuration>
        <java>
            <eclipse/>
            <removeUnusedImports/>
            <trimTrailingWhitespace/>
            <endWithNewline/>
        </java>
    </configuration>
</plugin>
```

```bash
mvn spotless:check   # falla si algo no está formateado
mvn spotless:apply   # lo arregla
```

Hook en `.githooks/pre-commit` (no en `.git/hooks/` porque esa carpeta no se versiona y no llegaría a nadie que clone el repo):

```sh
#!/bin/sh
echo "Formatting with Spotless..."
mvn -q spotless:apply || exit 1
git add -u
```

El `git add -u` es necesario porque Spotless modifica los ficheros después de que ya hiciste `git add`, así que hay que volver a añadirlos para que el commit lleve la versión formateada.

```bash
chmod +x .githooks/pre-commit
git config core.hooksPath .githooks
```

Esto último (`core.hooksPath`) es local a cada clon, cada uno tiene que ejecutarlo una vez. Y los hooks se pueden saltar con `--no-verify`, por eso en el Boletín 4 esto mismo va a CI.

Para comprobar que funcionaba, metí indentación rara a propósito en `Priority.java`:

```java
public enum Priority {
  LOW,
    MEDIUM,
  HIGH
}
```

Y el commit (`763e3a1`) ya quedó con el formato bien:

```java
public enum Priority {
    LOW, MEDIUM, HIGH
}
```

Commits: `6a1550a` (plugin), `4d57ba4` (hook), `763e3a1` (verificación).

---

## Parte F — Conflicto

Añadí una constante `MAX_TITLE_LENGTH` a `TaskService` y creé dos ramas desde el mismo commit (`763e3a1`) que la ponen distinta:

- `feat/prioridad-default` → 100
- `feat/titulo-max-length` → 255

Primer merge, sin problema (fast-forward, main no había avanzado):

```bash
git switch main
git merge feat/prioridad-default
# Fast-forward
```

Segundo merge, conflicto porque ahora main (con 100) y la otra rama (con 255) tocan la misma línea:

```bash
git merge feat/titulo-max-length
# CONFLICT (content): Merge conflict in TaskService.java
```

```java
<<<<<<< HEAD
    static final int MAX_TITLE_LENGTH = 100;
=======
    static final int MAX_TITLE_LENGTH = 255;
>>>>>>> feat/titulo-max-length
```

Lo de arriba (entre `<<<<<<<` y `=======`) es lo que hay en la rama en la que estoy, lo de abajo es lo que trae la rama que fusiono. Me quedé con 255 porque es un límite más razonable para un campo de texto (100 se queda corto):

```java
static final int MAX_TITLE_LENGTH = 255;
```

```bash
git add src/main/java/com/gloria/tasksapi/service/TaskService.java
git commit -m "fix(merge): resuelve conflicto en MAX_TITLE_LENGTH, se adopta 255"
```

Historial final:

```
*   fc72a59 fix(merge): resuelve conflicto en MAX_TITLE_LENGTH, se adopta 255
|\
| * 79562fd feat(service): añade constante MAX_TITLE_LENGTH 255
* | b6fd91f feat(service): añade constante MAX_TITLE_LENGTH 100
|/
* 763e3a1 test(hooks): verifica que el hook formatea antes del commit
* 4d57ba4 chore(hooks): añade hook de pre-commit con Spotless
* 6a1550a chore(build): añade Spotless para formateo automático
* 1cdd895 feat(api): añade filtro de búsqueda por prioridad
* 8c2901e chore(editor): añade .editorconfig
* a31ef77 chore: proyecto Maven inicial de la API de tareas
```

Las dos ramas parten de `763e3a1` y se juntan en `fc72a59`, que tiene dos padres.

