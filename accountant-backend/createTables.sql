CREATE TABLE Transaction (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    year CHAR(4) NOT NULL,
    date CHAR(4) NOT NULL,
    description TINYTEXT NOT NULL,
    amount INT NOT NULL,
    debit VARCHAR(8) NOT NULL,
    credit VARCHAR(8) NOT NULL
);