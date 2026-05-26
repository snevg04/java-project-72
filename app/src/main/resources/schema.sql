DROP TABLE IF EXISTS url_checks;
DROP TABLE IF EXISTS urls;

CREATE TABLE urls (
    id SERIAL PRIMARY KEY,
    name varchar(255),
    created_at timestamp
);

CREATE TABLE url_checks (
    id SERIAL PRIMARY KEY,
    url_id bigint REFERENCES urls(id) ON DELETE CASCADE,
    status_code int,
    h1 varchar(200),
    title varchar(200),
    description varchar(200),
    created_at timestamp
);