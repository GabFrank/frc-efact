# Índice de migraciones Flyway

35 migraciones (`V1`–`V35`) en `frc-efact-backend/src/main/resources/db/migration/`. Verificado 2026-08-05.

**Reglas:** nunca modificar una migración ya aplicada — siempre crear una nueva `V36__...`. `ddl-auto: validate` (Hibernate solo valida). Ver [../conventions/database-standards.md] y `DATABASE_STANDARDS.md`.

| V | Nombre | Qué hace |
|---|---|---|
| V1 | Create_users_table | Tabla usuarios inicial |
| V2 | Refactor_to_schema_based_structure | Migra a esquemas (persona, empresa, …) |
| V3 | Insert_admin_user | Siembra `admin` + `testuser` |
| V4 | Update_user_passwords | Fija hashes reales: `admin`/`admin123`, `testuser`/`test123` |
| V5 | Create_complete_database_structure | Estructura completa + roles (ADMIN/EMPRESA_ADMIN/FACTURADOR/LECTOR) |
| V6 | Add_descripcion_to_audit_log | |
| V7 | Fix_audit_log_column_names | |
| V8 | Add_missing_columns_created_by_hibernate | |
| V9 | Create_Geografia_Tables | Esquema `geografia` (Pais/Depto/Ciudad/Distrito/Barrio) |
| V10 | refactor_empresa_domicilio_fiscal | |
| V11 | Add_tipo_contribuyente_to_empresa | |
| V12 | Add_facturador_to_empresa_roles | |
| V13 | Simplify_timbrado_remove_duplicate_fields | |
| V14 | Remove_CSC_fields_from_empresa | CSC pasa a timbrado |
| V15 | Refactor_timbrado_detalle_geografia | |
| V16 | Assign_all_roles_to_admin | |
| V17 | Allow_null_ranges_for_electronic_timbrados | |
| V18 | Add_tipo_transaccion_and_unidad_medida_to_producto | |
| V19 | Convert_tipo_transaccion_enum_to_varchar | |
| V20 | Add_sifen_fields_to_cliente | Campos SIFEN del receptor |
| V21 | Update_sifen_models | |
| V22 | Alter_documento_estado_to_varchar | `EstadoDE` como varchar |
| V23 | Add_evento_tables_and_update_enum | Eventos cancelación/inutilización/nominación |
| V24 | Fix_invoice_totals_calculation | |
| V25 | Add_moneda_extranjera_tipo_cambio_factura_legal | Moneda extranjera |
| V26 | Fix_facturas_moneda_extranjera_totales_en_guaranies | Totales en guaraníes |
| V27 | add_auth0_fields | Campos Auth0 en usuario |
| V28 | implement_notes_tables | Notas crédito/débito/remisión |
| V29 | fix_notes_tables_audit_columns_and_foreign_keys | |
| V30 | add_imagen_perfil_to_usuario | |
| V31 | add_geography_ids_to_nota_remision | |
| V32 | add_distrito_ids_to_nota_remision | |
| V33 | add_transportista_fields_to_nota_remision | |
| V34 | add_fecha_estimada_factura_to_nota_remision | |
| V35 | create_vehiculo_chofer_tables | Esquema `transporte` (Vehiculo/Chofer) |

**Próxima migración = `V36__...`**
