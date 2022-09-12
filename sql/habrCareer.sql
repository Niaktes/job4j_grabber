CREATE TABLE posts.post (
    id serial PRIMARY KEY,
    name varchar NOT NULL,
    link varchar NOT NULL UNIQUE,
    text text,
    created timestamp NOT NULL
);