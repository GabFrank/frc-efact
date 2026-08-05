# Workflow: Agregar una entidad de punta a punta

Guía verificada contra el par de referencia **Vehiculo** (esquema `transporte`, migración `V35`). Seguí el mismo patrón para tu entidad nueva. Orden: **backend primero** (migración → model → repo → dto/mapper → service → controller), **después frontend** (model → api → feature → ruta).

> Antes de empezar leé [../conventions/backend-rules.md](../conventions/backend-rules.md), [../conventions/database-standards.md](../conventions/database-standards.md) y [../conventions/frontend-rules.md](../conventions/frontend-rules.md). Flujo largo y comentado: `docs/FLUJO_SISTEMA_ENTIDADES.md`.

---

## BACKEND

### 1. Migración Flyway `V36__...`
- Revisá cuál es el último `V<N>__*.sql` en `frc-efact-backend/src/main/resources/db/migration/` ([índice](../reference/migrations-index.md)) y nombrá la tuya `V<N+1>__descripcion.sql`. **Nunca modifiques una migración ya aplicada.**
- Usá un **esquema de dominio** existente (`persona`, `empresa`, `financiero`, `productos`, `clientes`, `auditoria`, `geografia`, `transporte`) o creá uno con `CREATE SCHEMA IF NOT EXISTS`. **Nada en `public`. No existe `catalogo`.**
- Toda tabla lleva PK `id BIGSERIAL` + los 4 campos de auditoría y FKs referenciando esquema.esquema:

```sql
CREATE SCHEMA IF NOT EXISTS transporte;

CREATE TABLE IF NOT EXISTS transporte.mi_entidad (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(50),
    CONSTRAINT uk_mi_entidad_empresa_nombre UNIQUE (empresa_id, nombre),
    CONSTRAINT fk_mi_entidad_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id)
);
CREATE INDEX IF NOT EXISTS idx_mi_entidad_empresa ON transporte.mi_entidad(empresa_id);
```

- ⚠️ **Nota de fidelidad:** `DATABASE_STANDARDS.md` pide el trigger `actualizar_timestamp_modificacion()`, pero las entidades nuevas reales (Vehiculo/Chofer, V35) **NO** crean trigger: los timestamps los maneja JPA vía `AuditableEntity` (`@PrePersist`/`@PreUpdate`) + `DEFAULT CURRENT_TIMESTAMP` en el DDL. Seguí el patrón de `AuditableEntity` salvo que necesites el timestamp también fuera de la app (ahí sí agregá el trigger).
- Validá antes de correr: `cd frc-efact-backend && ./check-migrations.sh`.

### 2. Model (`model/MiEntidad.java`)
- `extends AuditableEntity` (aporta `creadoEn/creadoPor/actualizadoEn/actualizadoPor`). Ver `model/base/AuditableEntity.java`.
- `@Entity @Table(name="mi_entidad", schema="transporte", ...)` con `@Index`/`@UniqueConstraint` que **espejen** la migración.
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Relaciones `@ManyToOne(fetch = FetchType.LAZY)` con `@JoinColumn(name="empresa_id")`. Validaciones Jakarta (`@NotNull`, `@NotBlank`, `@Size`).
- `ddl-auto: validate` → si el mapeo no coincide con el DDL, la app **no arranca**.

### 3. Repository (`repository/MiEntidadRepository.java`)
- `extends JpaRepository<MiEntidad, Long>`. Derived queries por empresa: `findByEmpresaIdAndActivoTrue(...)`, `existsByEmpresaIdAndNombreAndActivoTrue(...)`.
- Búsquedas con `@Query` JPQL + `Pageable` (ver `VehiculoRepository.buscarVehiculos`).
- **Specification** solo si vas a filtrar dinámicamente por muchos campos: extendé `JpaSpecificationExecutor` y poné la spec en `repository/specification/`. Para CRUD por empresa con búsqueda simple, `@Query` alcanza (Vehiculo no usa specification).

### 4. DTO + Mapper
- `dto/MiEntidadDto.java`: campos planos, sin la entidad relacionada completa (solo `empresaId`). Validaciones Jakarta duplicadas del model.
- `dto/mapper/MiEntidadMapper.java` (`@Component`): `toDto()`, `toEntity()` (NO carga la empresa, solo setea escalares — el service resuelve la relación), `updateEntityFromDto()`.

### 5. Service (`service/MiEntidadService.java`)
- `@Service @Transactional`, inyección por constructor.
- **Chequeo de acceso multi-empresa obligatorio**: `empresaSecurityService.verificarAccesoEscritura(empresaId)` / `verificarAccesoLectura(...)` al inicio de cada método.
- Cargá la `Empresa` real con `empresaRepository.findById(empresaId)`, seteala en la entidad, validá duplicados, `repository.save(...)`.
- Soft-delete: método `desactivar(...)` que setea `activo=false` (no `delete`).

### 6. Controller (`controller/MiEntidadController.java`)
- `@RestController @RequestMapping("/mi-entidad")` — **SIN `/api/`** (el context-path `/api` ya lo agrega; ver regla dura #1). Path anidado por empresa: `@PostMapping("/empresa/{empresaId}")`, etc.
- `@PreAuthorize` por operación (patrón Vehiculo):
  - Crear/actualizar → `hasAnyRole('ADMIN','EMPRESA_ADMIN','FACTURADOR')`
  - Leer/listar/buscar → `hasAnyRole('ADMIN','EMPRESA_ADMIN','FACTURADOR','LECTOR')`
  - Desactivar/reactivar → `hasAnyRole('ADMIN','EMPRESA_ADMIN')`
- Mapear entrada/salida con el mapper, devolver `ResponseEntity` con status correcto (`201` en create). Anotá Swagger (`@Tag`, `@Operation`).

### ✅ Compilar backend
```bash
cd frc-efact-backend && ./mvnw compile
```
Si falla, **no sigas** — arreglá primero.

---

## FRONTEND

### 7. Model (`src/app/models/mi-entidad.model.ts`)
Interfaz TS espejo del DTO (kebab-case en el archivo):
```ts
export interface MiEntidad {
  id?: number;
  empresaId: number;
  nombre: string;
  activo: boolean;
}
```

### 8. API service (`src/app/core/api/mi-entidad-api.service.ts`)
- `@Injectable({ providedIn: 'root' })`, `inject(HttpClient)`.
- **La URL SÍ incluye `/api/`** vía `environment.apiUrl`: `` `${environment.apiUrl}/mi-entidad` `` (regla dura #5).
- Métodos anidados por empresa que espejen el controller: `getByEmpresa`, `getById`, `buscar`, `create`, `update`, `delete`, `reactivar`, `contar`. Reutilizá `PageResponse<T>` de `cliente-api.service.ts` para paginados.

### 9. ¿NgRx o API directo?
- **Rama NgRx** (`core/state/mi-entidad/` con `actions/effects/reducer/selectors`) **solo** si la entidad es principal y su estado se comparte entre varias vistas (como `facturacion`, `documentos`, `notas`, `empresas`, `usuarios`, `timbrados`).
- **API directo** (componente inyecta el `*ApiService`) si es CRUD localizado. **Transporte (Vehiculo/Chofer) NO tiene NgRx** — usa el api-service directo. No todas las entidades tienen store; ver [../reference/ngrx-state-index.md](../reference/ngrx-state-index.md).

### 10. Feature (`src/app/features/mi-dominio/`)
- Standalone components, separados: `mi-entidad-list.component.ts` (+`.html`/`.scss`) y form/dialog. Transporte usa `*-list.component.ts` + un `*-dialog` en `shared/components/`.
- Archivo de rutas del feature `mi-entidad.routes.ts` con guards `authGuard`, `empresaSelectedGuard`, `roleGuard` y `data.roles` (ver `features/transporte/vehiculos/vehiculos.routes.ts`).

### 11. ⚠️ Cablear la ruta DE VERDAD en `app.routes.ts`
- Agregá el `path` bajo el layout con `loadChildren`/`loadComponent` apuntando a **tu componente/rutas reales**:
```ts
{
  path: 'mi-dominio',
  loadChildren: () => import('./features/mi-dominio/mi-dominio.routes').then(m => m.MI_DOMINIO_ROUTES)
}
```
- **NO dejes `TestPageComponent` como placeholder.** Bug conocido (QA-2, ver [../reference/known-bugs.md](../reference/known-bugs.md)): las rutas `reportes` y `auditoria` cargan `test-page.component` aunque las features existen — no cometas el mismo error. Verificá que la ruta resuelve al componente real y que el ítem aparece en el sidebar/layout.

### ✅ Compilar frontend
```bash
cd frc-efact-frontend && npm run build:dev   # o: npm run lint
```

---

## Checklist final
- [ ] `V<N+1>__*.sql` en esquema de dominio (no `public`), con auditoría + FKs por esquema.
- [ ] Model `extends AuditableEntity`, mapeo espeja el DDL (`ddl-auto: validate`).
- [ ] Controller `@RequestMapping` **sin** `/api/`, `@PreAuthorize` por operación.
- [ ] Service con `empresaSecurityService.verificarAcceso*`.
- [ ] `./mvnw compile` OK.
- [ ] API service front con `${environment.apiUrl}/...`.
- [ ] Ruta **cableada real** en `app.routes.ts` (no `TestPageComponent`) + ítem de menú.
- [ ] `npm run build:dev` / `npm run lint` OK.
- [ ] **No commitees sin preguntar** — push a `main` = deploy a prod (ver [deploy-render.md](deploy-render.md)).
