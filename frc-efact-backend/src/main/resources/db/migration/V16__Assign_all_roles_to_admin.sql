-- V16 Migration: Assign all system roles to admin user
-- This migration assigns all available roles (ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR) 
-- to the default admin user to ensure full system access

-- Insert roles for admin user
-- Uses subqueries to get the IDs dynamically and avoids conflicts with unique constraint
INSERT INTO persona.usuario_rol (usuario_id, rol_id, creado_en)
SELECT 
    u.id as usuario_id,
    r.id as rol_id,
    CURRENT_TIMESTAMP as creado_en
FROM persona.usuario u
CROSS JOIN persona.rol r
WHERE u.username = 'admin'
ON CONFLICT (usuario_id, rol_id) DO NOTHING;

-- Verify that all roles were assigned to admin
-- SELECT 
--     u.username,
--     r.nombre as rol,
--     r.descripcion
-- FROM persona.usuario u
-- JOIN persona.usuario_rol ur ON u.id = ur.usuario_id
-- JOIN persona.rol r ON ur.rol_id = r.id
-- WHERE u.username = 'admin'
-- ORDER BY r.nombre;

