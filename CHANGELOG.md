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
