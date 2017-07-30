CREATE SCHEMA tenant1;
CREATE TABLE tenant1.users(  id BIGINT PRIMARY KEY,  user_name VARCHAR(255) NOT NULL);
CREATE SCHEMA tenant2;
CREATE TABLE tenant2.users(  id BIGINT PRIMARY KEY,  user_name VARCHAR(255) NOT NULL);

INSERT INTO tenant1.users VALUES (1, 'ernie');
INSERT INTO tenant2.users VALUES (2, 'bert');