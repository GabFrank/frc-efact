-- Add imagen_perfil column to usuario table for storing Google/Auth0 profile picture

-- Add imagen_perfil column to store the profile picture URL from Google/Auth0
ALTER TABLE persona.usuario ADD COLUMN imagen_perfil VARCHAR(500);

-- Add comment to document the column
COMMENT ON COLUMN persona.usuario.imagen_perfil IS 'URL de la imagen de perfil del usuario obtenida de Google/Auth0';

