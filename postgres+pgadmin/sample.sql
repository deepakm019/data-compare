CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE packages (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE activity_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (username, email, password) VALUES
('user1', 'user1@example.com', 'password1'),
('user2', 'user2@example.com', 'password2'),
('user3', 'user3@example.com', 'password3'),
('user4', 'user4@example.com', 'password4'),
('user5', 'user5@example.com', 'password5');

INSERT INTO roles (name)
VALUES
    ('role1'),
    ('role2'),
    ('role3'),
    ('role4'),
    ('role5');

INSERT INTO packages (name)
VALUES
    ('package1'),
    ('package2'),
    ('package3'),
    ('package4'),
    ('package5');

INSERT INTO users (username, email, password) VALUES
('user6', 'user6@example.com', 'password6');

INSERT INTO activity_log (user_id, type, action)
VALUES
   (2, 'login', 'logged in'),
(2, 'logout', 'logged out'),
(3, 'login', 'logged in'),
(3, 'logout', 'logged out'),
(4, 'login', 'logged in'),
(4, 'logout', 'logged out'),
(5, 'login', 'logged in'),
(5, 'logout', 'logged out'),
(6, 'login', 'logged in'),
(6, 'logout', 'logged out'),
(2, 'login', 'logged in'),
(2, 'logout', 'logged out'),
(3, 'login', 'logged in'),
(3, 'logout', 'logged out'),
(4, 'login', 'logged in'),
(4, 'logout', 'logged out'),
(5, 'login', 'logged in'),
(5, 'logout', 'logged out'),
(6, 'login', 'logged in'),
(6, 'logout', 'logged out');