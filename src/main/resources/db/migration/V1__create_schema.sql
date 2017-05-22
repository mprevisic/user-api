CREATE TABLE users (
    id                         BIGINT NOT NULL AUTO_INCREMENT,
    email					   VARCHAR(100) NOT NULL UNIQUE,
    password				   TEXT NOT NULL,
    first_name				   VARCHAR(100),
    last_name				   VARCHAR(100),
    title   				   VARCHAR(10),
    phone_code				   VARCHAR(10),
    phone_number			   VARCHAR(20),
    role                       SMALLINT,
    PRIMARY KEY (id)
);

CREATE TABLE user_blacklist (
	id                         BIGINT NOT NULL AUTO_INCREMENT,
    email					   VARCHAR(100) NOT NULL UNIQUE,
    date_time                  BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE security_keys (
    id                         BIGINT NOT NULL AUTO_INCREMENT,
    name                       VARCHAR(100) NOT NULL UNIQUE,
    value                      BLOB NOT NULL,
    PRIMARY KEY (id)
);