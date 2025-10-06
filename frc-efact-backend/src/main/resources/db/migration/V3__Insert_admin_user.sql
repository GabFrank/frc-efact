-- Insertar usuario de prueba adicional
-- Username: testuser
-- Password: test123
-- El hash BCrypt fue generado con strength 10

INSERT INTO persona.usuario (
    username,
    email,
    password_hash,
    is_active,
    intentos_fallidos_login,
    creado_por,
    actualizado_por
) VALUES (
    'testuser',
    'testuser@frcefact.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- test123
    true,
    0,
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (username) DO NOTHING;

-- Verificar que el usuario fue creado
-- SELECT * FROM persona.usuario WHERE username = 'testuser';
