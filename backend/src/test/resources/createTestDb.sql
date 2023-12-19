DROP TABLE IF EXISTS Transaction;
DROP TABLE IF EXISTS ASchema;
DROP TABLE IF EXISTS Account;
DROP TABLE IF EXISTS Budgeting;
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
CREATE TABLE Budgeting (
                           year CHAR(4) NOT NULL,
                           id VARCHAR(10) NOT NULL,
                           name TINYTEXT NOT NULL,
                           debit VARCHAR(9),
                           credit VARCHAR(9),
                           description TINYTEXT,
                           planning TINYTEXT,
                           PRIMARY KEY(year, id)
);
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '0', 'class0', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '00', 'group00', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '000', 'account000', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '001', 'account001', 'L');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '002', 'account002', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '003', 'account003', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '01', 'group01', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '010', 'account010', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '700', 'account700', 'X');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '000', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '001', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '700', '0', 'general', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0101', '1000', '000.0', '700.0', '22x1', '22tr init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0101', '1000', '700.0', '001.0', '22x2', '22tr init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0505', '500', '001.0', '000.0', '22x3', '22tr 1');

INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '0', 'class0', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '00', 'group00', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '000', 'account000', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '001', 'account001', 'L');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '002', 'account002', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '003', 'account003', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '01', 'group01', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '010', 'account010', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2023', '700', 'account700', 'X');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '000', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '000', '1', 'general of 000', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '000', '2', 'account000.2', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '001', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '001', '1', 'general of 001', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '001', '2', 'account001.2', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '002', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '002', '1', 'general of 002', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '002', '2', 'account002.2', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '003', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '003', '1', 'general of 003', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '003', '2', 'account003.2', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '010', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '010', '1', 'general of 010', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '010', '2', 'account010.2', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2023', '700', '0', 'general', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '000.0', '700.0', '1', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '000.1', '700.0', '2', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '000.2', '700.0', '3', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '700.0', '001.0', '4', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '700.0', '001.1', '5', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '700.0', '001.2', '6', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '010.0', '700.0', '7', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '010.1', '700.0', '8', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0101', '1000', '010.2', '700.0', '9', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0504', '100', '000.0', '003.0', '10', 'month4 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1004', '200', '000.0', '001.0', '11', 'month4 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1504', '300', '001.1', '000.1', '12', 'month4 000 -amount');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '2004', '400', '000.2', '001.2', '13', 'month4 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '2005', '500', '000.0', '003.0', '14', 'month5 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0505', '600', '000.0', '001.0', '15', 'month5 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1505', '700', '001.1', '000.1', '16', 'month5 000 -amount');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1005', '800', '000.2', '001.2', '17', 'month5 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '0506', '900', '000.0', '003.0', '18', 'month6 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1006', '1000', '000.0', '001.0', '19', 'month6 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1506', '1100', '001.1', '000.1', '20', 'month6 000 -amount');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '2006', '1200', '000.2', '001.2', '21', 'month6 000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '1005', '2000', '002.0', '001.0', '22', 'month5 !000');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2023', '3005', '3000', '000.0', '000.1', '23', 'month5 000correction');


INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '2', 'class0', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '20', 'group00', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '200', 'account000', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '201', 'account001', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '21', 'group01', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '210', 'account010', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '22', 'group01', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '220', 'account010', 'L');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '23', 'group01', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '230', 'account010', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '700', 'account700', 'X');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '700', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '200', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '201', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '210', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '220', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '230', '0', 'general', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '1000', '200.0', '700.0', '20xi1', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '0', '201.0', '700.0', '20xi0', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '2000', '210.0', '700.0', '20xi2', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '3000', '700.0', '220.0', '20xi3', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '4000', '230.0', '700.0', '20xi4', 'init');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0504', '100', '201.0', '200.0', '20x1', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0504', '100', '201.0', '200.0', '20x2', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x3', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x4', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x5', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x6', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x7', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x8', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x9', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x10', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x11', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x12', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x13', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x14', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x15', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x16', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '210.0', '200.0', '20x17', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '210.0', '200.0', '20x18', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x19', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x20', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x21', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x22', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x23', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x24', 'init');



