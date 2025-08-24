-- Create default admin user (password: admin123)
INSERT INTO users (username, email, password, first_name, last_name, role, is_active, created_at, updated_at) 
VALUES (
    'admin', 
    'admin@darimac.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
    'Admin', 
    'User', 
    'ADMIN', 
    true, 
    NOW(), 
    NOW()
) ON CONFLICT (username) DO NOTHING;

-- Create default regular user (password: user123)
INSERT INTO users (username, email, password, first_name, last_name, role, is_active, created_at, updated_at) 
VALUES (
    'user', 
    'user@darimac.com', 
    '$2a$10$3CiOQJhIHPqGb.GxLSDNru7WA5KsjLTU/Q90EY7yAEEjWlT9Qb2DG', 
    'Regular', 
    'User', 
    'USER', 
    true, 
    NOW(), 
    NOW()
) ON CONFLICT (username) DO NOTHING;