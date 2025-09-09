-- V10__Create_admin_user.sql
-- Create initial admin user

-- Insert initial admin user with bcrypt hashed password for 'admin'
-- Password hash generated for 'admin' using BCrypt with strength 12
INSERT INTO users (
    email, 
    password_hash, 
    first_name, 
    last_name, 
    role, 
    created_at, 
    updated_at, 
    is_active
) VALUES (
    'admin@shiksha.com', 
    '$2a$12$wagVQGeatXj7Mzk5m8W/6.3D7rZUgx8i3xvZPzrM61ZgNComg2Vzu',
    'System', 
    'Administrator', 
    'ADMIN', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    TRUE
);