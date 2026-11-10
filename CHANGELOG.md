# [1.2.0](https://github.com/GabFrank/frc-efact/compare/v1.1.4...v1.2.0) (2026-11-10)


### Bug Fixes

* **auth:** desacoplar el login local de Auth0 y aplicar el timeout de HTTP ([9e5b3dc](https://github.com/GabFrank/frc-efact/commit/9e5b3dc121acc4eb391707e99b894fc489d388d9))
* **documentos:** verificar el acceso por empresa al leer un DE ([88e3550](https://github.com/GabFrank/frc-efact/commit/88e3550b3167ed608521d982ae8a85c76bec562d))
* **facturacion:** corregir el IVA de los ítems de factura con el XML del DE ([11d5eb3](https://github.com/GabFrank/frc-efact/commit/11d5eb39d076ba90181b119dde1be624d8ca0c44))
* **facturacion:** descuento final en la moneda visible y bloquear el guardado sin IVA ([b150e53](https://github.com/GabFrank/frc-efact/commit/b150e5371f83623e9fb44a6c34f2ec7e1419bbcd))
* **facturacion:** persistir el IVA en el ítem en vez de derivarlo del catálogo ([7931992](https://github.com/GabFrank/frc-efact/commit/7931992a7e3ef7d885fae85758738465fb28fb4f))
* **integracion:** consulta en ambiente PROD rechazada por CSC de desarrollo ([f33334f](https://github.com/GabFrank/frc-efact/commit/f33334fa5b796073426ca0352e7b885cf109ae7f))
* **ruc:** centralizar el parseo del RUC y exigir el dígito verificador ([cdd1a2c](https://github.com/GabFrank/frc-efact/commit/cdd1a2ca377bafe0ba9a8352e052e0462c2abc40))
* **ruc:** corregir los tests con DV inventados y reactivar la validación ([892370b](https://github.com/GabFrank/frc-efact/commit/892370b7744479751881fd4ab548111f44c03bff))
* **sifen:** conectar la unidad de medida al catálogo de la SET y dejar de truncar la cantidad ([9916c8c](https://github.com/GabFrank/frc-efact/commit/9916c8c02986e10d1e32c7003d9e1965dd40d7d4))
* **sifen:** dejar de loguear el CSC desencriptado ([73b5ea4](https://github.com/GabFrank/frc-efact/commit/73b5ea423499b9c2a501bf387cba06cb8ff6d92a))


### Features

* **integracion:** API key y consulta de DE por CDC en SIFEN para otros sistemas ([ae573f9](https://github.com/GabFrank/frc-efact/commit/ae573f96718e8564e0d4e650bac414b0bc913c50))

## [1.1.4](https://github.com/GabFrank/frc-efact/compare/v1.1.3...v1.1.4) (2026-08-06)


### Bug Fixes

* **ci:** fetch solo de origin en el deploy, no --all ([7d50b71](https://github.com/GabFrank/frc-efact/commit/7d50b7195868b0baab15e96470e79a1cee6c8464))

## [1.1.3](https://github.com/GabFrank/frc-efact-legacy/compare/v1.1.2...v1.1.3) (2026-08-05)


### Bug Fixes

* **security:** quitar los defaults de JWT_SECRET y ENCRYPTION_KEY ([9905317](https://github.com/GabFrank/frc-efact-legacy/commit/9905317285a29070bd076a1c8d46aec80eae6a0c))

## [1.1.2](https://github.com/GabFrank/frc-efact/compare/v1.1.1...v1.1.2) (2026-08-05)


### Bug Fixes

* **ci:** --env-file en el `ps` del deploy y logs del backend si falla ([8f187e0](https://github.com/GabFrank/frc-efact/commit/8f187e0231a359a6796924c39b4fbc7fd6d2f763)), closes [#7](https://github.com/GabFrank/frc-efact/issues/7)

## [1.1.1](https://github.com/GabFrank/frc-efact/compare/v1.1.0...v1.1.1) (2026-08-05)


### Bug Fixes

* **empresa:** separar actividades económicas secundarias por ";" en vez de "," ([2cd5da0](https://github.com/GabFrank/frc-efact/commit/2cd5da0a0bdc3cfb5c39161404ecf5fe5cc748e3))

# [1.1.0](https://github.com/GabFrank/frc-efact/compare/v1.0.3...v1.1.0) (2026-08-05)


### Bug Fixes

* **deploy:** dump/restore con las tools del contenedor y verificación dura post-restore ([5ef047a](https://github.com/GabFrank/frc-efact/commit/5ef047ad17b50ef0687d9f13fc6e511b46d3df9e))
* **deploy:** evitar que compose exec consuma el stdin del script (confirmación MIGRAR) ([1e79c0a](https://github.com/GabFrank/frc-efact/commit/1e79c0a746b3d97fa3f098e81492367ad5f52e08))
* **deploy:** pg_restore --list via stdin sin /dev/stdin y abortar si el dump no trae datos ([1d44975](https://github.com/GabFrank/frc-efact/commit/1d449750417cd712bd8a0cbb0e669d170c5ca07b))
* **deploy:** setear PORT=8080 en backend — el ENTRYPOINT exec no expande ${PORT:-8080} ([4354836](https://github.com/GabFrank/frc-efact/commit/4354836080778f062168e12cae7d5f899bf3c510))


### Features

* **backend:** orígenes CORS adicionales configurables por CORS_ALLOWED_ORIGINS ([f58aac4](https://github.com/GabFrank/frc-efact/commit/f58aac4af7287872296dc8491cf7ed066cc35fa5))
* **skills:** agregar skill frc-efact-expert con playbook SIFEN y checklists del proyecto ([cf5ee6c](https://github.com/GabFrank/frc-efact/commit/cf5ee6c2869fa994bbc6d354249d70dd36cad11c))

## [1.0.3](https://github.com/GabFrank/frc-efact/compare/v1.0.2...v1.0.3) (2026-05-20)


### Bug Fixes

* **pdf:** abrir PDF en pestaña nueva en desktop en lugar de la hoja de compartir ([9d49e3e](https://github.com/GabFrank/frc-efact/commit/9d49e3eace6303cc125ca150b7c4d64992eacdc5))

## [1.0.2](https://github.com/GabFrank/frc-efact/compare/v1.0.1...v1.0.2) (2026-04-13)


### Bug Fixes

* **frontend:** leer versión de package.json en lugar de hardcodear ([0a12b10](https://github.com/GabFrank/frc-efact/commit/0a12b101b4fd8ebd15ed069ac2bf4ab1a225cff0))

## [1.0.1](https://github.com/GabFrank/frc-efact/compare/v1.0.0...v1.0.1) (2026-04-13)


### Bug Fixes

* **ci:** resolver error de submodules huérfanos en checkout de GitHub Actions ([7c57897](https://github.com/GabFrank/frc-efact/commit/7c57897847354e3c1560919b8149951411e74a63))
* **ci:** validar configuración de semantic-release ([25ef0af](https://github.com/GabFrank/frc-efact/commit/25ef0af59afe3619d7e78f8926074de14f492aba))
