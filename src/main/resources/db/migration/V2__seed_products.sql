-- Demo catalogue so the API is usable straight after `docker compose up`.
INSERT INTO products (sku, name, description, price, stock, created_at, updated_at) VALUES
    ('KB-001', 'Mechanical Keyboard', 'Tenkeyless, hot-swappable switches', 89.99, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MS-001', 'Wireless Mouse', 'Ergonomic, 4000 DPI', 39.50, 120, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MN-027', '27" 4K Monitor', 'IPS panel, USB-C 65W', 349.00, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-USB', 'USB-C Hub', '7-in-1, HDMI + Ethernet', 45.00, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
