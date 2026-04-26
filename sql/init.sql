-- Tabla para los usuarios
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Tabla de pokemons personalizados
CREATE TABLE IF NOT EXISTS custom_pokemon (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    description TEXT
);

-- Seedeamos con datos base (Usuarios admin y user)
INSERT INTO users (username, password, role) VALUES ('admin', 'admin', 'ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO users (username, password, role) VALUES ('user', 'password', 'ROLE_USER') ON CONFLICT DO NOTHING;