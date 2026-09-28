# Cómo contribuir

Este proyecto sigue el flujo **GitHub Flow**: `main` siempre debe estar en estado desplegable.

## Flujo de trabajo

1. **Abre un Issue** describiendo el cambio (usa la plantilla de `feat` o `bug`).
2. **Crea una rama** desde `main` con el prefijo correspondiente:

   | Prefijo | Se usa para |
   |---|---|
   | `feat/` | Nueva funcionalidad |
   | `fix/` | Corrección de un bug |
   | `chore/` | Mantenimiento (dependencias, configuración) |
   | `docs/` | Cambios solo en documentación |

   ```bash
   git switch -c feat/nombre-descriptivo
   ```

3. **Implementa el cambio** y añade o actualiza los tests correspondientes. Antes de subir, comprueba:

   ```bash
   mvn clean package
   mvn spotless:check
   ```

4. **Sube la rama** y abre un Pull Request usando la plantilla, enlazando el Issue con `Closes #N`:

   ```bash
   git push -u origin feat/nombre-descriptivo
   ```

5. **Revisión cruzada**: quien no ha escrito el código lo revisa. Se resuelven todos los comentarios antes de fusionar.

6. **Si `main` avanzó mientras tu PR seguía abierto**, pon tu rama al día con `rebase` (no `merge`), para mantener el historial lineal:

   ```bash
   git fetch origin
   git rebase origin/main
   # resuelve conflictos si los hay, luego:
   git push --force-with-lease
   ```

7. **Fusión**: este repositorio usa **Squash and merge** — cada PR entra en `main` como un único commit. Así el historial queda con un commit por cambio significativo, aunque dentro del PR haya varios commits intermedios. Tras fusionar, borra la rama.

## Protección de `main`

- No se permite el push directo a `main`: todo cambio pasa por PR.
- Se requiere al menos 1 aprobación y que no haya conversaciones sin resolver.
- Las aprobaciones caducan si se suben nuevos commits al PR.
