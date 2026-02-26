CREATE TABLE IF NOT EXISTS suggestion (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT,
    item_id BIGINT
);
