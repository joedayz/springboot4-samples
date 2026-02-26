CREATE TABLE IF NOT EXISTS speaker (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    affiliation VARCHAR(50),
    email VARCHAR(255)
);
