CREATE TABLE Transaction (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    year CHAR(4) NOT NULL,
    date CHAR(4) NOT NULL,
    description TINYTEXT NOT NULL,
    amount INT NOT NULL,
    debit VARCHAR(9) NOT NULL,
    credit VARCHAR(9) NOT NULL
);
CREATE TABLE ASchema (
    year CHAR(4) NOT NULL,
    id VARCHAR(3) NOT NULL,
    name TINYTEXT NOT NULL,
    type CHAR(1) NOT NULL,
    PRIMARY KEY(year, id)
);
CREATE TABLE Account (
    year CHAR(4) NOT NULL,
    schema_id CHAR(3) NOT NULL,
    semantic_id VARCHAR(5) NOT NULL,
    name TINYTEXT NOT NULL,
    metadata TINYTEXT NOT NULL,
    PRIMARY KEY(year, schema_id, semantic_id)
);