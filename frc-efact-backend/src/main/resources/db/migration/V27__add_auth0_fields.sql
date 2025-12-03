-- Add Auth0 support fields to usuario table

-- Add auth0_id column to store the unique identifier from Auth0
ALTER TABLE persona.usuario ADD COLUMN auth0_id VARCHAR(255);

-- Add unique constraint to auth0_id to ensuring one-to-one mapping if set
ALTER TABLE persona.usuario ADD CONSTRAINT uk_usuario_auth0_id UNIQUE (auth0_id);

-- Make password_hash nullable to support Auth0-only users (social login)
ALTER TABLE persona.usuario ALTER COLUMN password_hash DROP NOT NULL;

-- Add index for faster lookups by auth0_id
CREATE INDEX idx_usuario_auth0_id ON persona.usuario(auth0_id);

