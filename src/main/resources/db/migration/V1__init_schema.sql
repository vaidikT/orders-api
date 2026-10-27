CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(64)    NOT NULL UNIQUE,
    name        VARCHAR(255)   NOT NULL,
    description VARCHAR(2000),
    price       NUMERIC(12, 2) NOT NULL,
    stock       INTEGER        NOT NULL,
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT         NOT NULL REFERENCES users (id),
    status      VARCHAR(20)    NOT NULL,
    total       NUMERIC(12, 2) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products (id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_order_items_order ON order_items (order_id);
