-- Actualizar contraseñas de usuarios con hashes BCrypt correctos
-- Esta migración corrige los hashes de contraseñas que no eran válidos

-- Actualizar contraseña del usuario admin
-- Username: admin
-- Password: admin123
UPDATE persona.usuario 
SET password_hash = '$2a$10$fBWW6bNMT6danKwNvYbNgeJi3v/le0EpizjswqG2/dXMHKjshHBH.',
    actualizado_en = CURRENT_TIMESTAMP,
    actualizado_por = 'SYSTEM'
WHERE username = 'admin';

-- Actualizar contraseña del usuario testuser
-- Username: testuser
-- Password: test123
UPDATE persona.usuario 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    actualizado_en = CURRENT_TIMESTAMP,
    actualizado_por = 'SYSTEM'
WHERE username = 'testuser';

-- Verificar que las contraseñas fueron actualizadas
-- SELECT username, email, substring(password_hash, 1, 20) as hash_preview FROM persona.usuario;
