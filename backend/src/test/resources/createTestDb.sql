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


INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '0', 'class0', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '1', 'class1', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '2', 'class2', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '20', 'group20', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '200', 'account200', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '201', 'account201', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '21', 'group21', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '210', 'account210', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '22', 'group22', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '220', 'account220', 'L');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '23', 'group23', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '230', 'account230', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '3', 'class3', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '30', 'group30', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '31', 'group31', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '700', 'account700', 'X');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '4', 'class4', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '40', 'group40', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '400', 'account400', 'L');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '5', 'class5', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '50', 'group50', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '51', 'group51', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '52', 'group52', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '53', 'group53', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '54', 'group54', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '55', 'group55', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '550', 'account550', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '551', 'account551', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '552', 'account552', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '553', 'account553', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '554', 'account554', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '555', 'account555', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '56', 'group56', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '6', 'class6', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '60', 'group60', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '61', 'group61', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '62', 'group62', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '63', 'group63', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '630', 'account630', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '631', 'account631', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '632', 'account632', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '633', 'account633', 'R');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '700', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '200', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '201', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '210', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '220', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '230', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '400', '0', 'general', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '1000', '200.0', '700.0', '20xi1', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '0', '201.0', '700.0', '20xi0', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '2000', '210.0', '700.0', '20xi2', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '3000', '700.0', '220.0', '20xi3', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '4000', '230.0', '700.0', '20xi4', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0101', '4000', '700.0', '400.0', '20xi5', 'init');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0504', '100', '201.0', '200.0', '20x1', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0504', '100', '201.0', '200.0', '20x2', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x3', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x4', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x5', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x6', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x7', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x8', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x9', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x10', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x11', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '0505', '100', '200.0', '220.0', '20x12', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x13', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x14', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x15', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '200.0', '220.0', '20x16', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '210.0', '200.0', '20x17', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1006', '100', '210.0', '200.0', '20x18', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x19', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x20', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x21', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x22', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x23', 'x');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1508', '100', '210.0', '200.0', '20x24', 'x');


INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '5', 'class5', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '50', 'group50', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '500', 'account500', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '51', 'group51', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '510', 'account510', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '52', 'group52', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '520', 'account520', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '53', 'group53', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '530', 'account530', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '54', 'group54', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '540', 'account540', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '55', 'group55', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '550', 'account550', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '551', 'account551', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '552', 'account552', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '553', 'account553', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '554', 'account554', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '555', 'account555', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '56', 'group56', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '560', 'account560', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '6', 'class6', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '60', 'group60', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '600', 'account600', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '61', 'group61', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '610', 'account610', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '62', 'group62', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '620', 'account620', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '63', 'group63', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '630', 'account630', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '631', 'account631', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '632', 'account632', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '633', 'account633', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '701', 'account701', 'X');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '2', 'class2', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '20', 'group20', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '201', 'account201', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '202', 'account202', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '21', 'group21', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2019', '210', 'account210', 'A');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '600', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '600', '1', 'second', 'aaaa');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '550', '0', 'general of x', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '631', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '520', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '553', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '554', '0', 'sda ad', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '630', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '701', '0', 'ds asd d', 'ssss');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '202', '10', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '202', '20', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '201', '0', 'sads sd as', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '201', '1', 'sd s dasda s', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '210', '0', 'general', 'a');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '210', '1', 'generaly', 'b');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2019', '210', '2', 'generalz', 'c');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x1', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x2', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x3', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x4', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x5', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x6', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x7', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0502', '100', '550.0', '600.0', '19x8', 'same summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x11', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x12', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x13', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x14', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x15', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x16', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x17', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x18', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0503', '100', '553.0', '631.0', '19x19', 'different summary');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x21', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x22', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x23', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x24', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x25', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x26', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1005', '1000', '520.0', '600.1', '19x27', 'different semantic account');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '10000', '701.0', '600.0', '19xx1', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '10000', '550.0', '701.0', '19xx2', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1007', '100', '202.10', '630.0', '19x31', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1007', '100', '202.10', '630.0', '19x32', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1007', '100', '202.10', '630.0', '19x33', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1007', '100', '520.0', '202.10', '19x34', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1008', '100', '202.10', '630.0', '19x35', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1008', '100', '202.10', '630.0', '19x36', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1008', '100', '202.10', '630.0', '19x37', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1008', '100', '520.0', '202.10', '19x38', 'profit');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1509', '10', '553.0', '554.0', '19x41', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '1009', '10', '553.0', '554.0', '19x42', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '0909', '10', '553.0', '554.0', '19x43', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '2009', '10', '553.0', '554.0', '19x44', 'same group');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '2109', '10', '553.0', '554.0', '19x45', 'same group');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '2109', '10000', '202.10', '201.1', '19x51', 'vac=xxx');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '2104', '6000', '210.0', '210.2', '19x52', '');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '2104', '5000', '210.2', '210.1', '19x53', 'Sale of asds');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'i1', 'i1', 'all=1000');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i1.1', 'i1.1', '202.10', '630.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i1.2', 'i1.2', '520.0', '600.1');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2019', 'i2', 'i2', '553.0', '554.0', '50|50|50|50|50|50|100|100|100|100|100|100');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2019', 'me1', 'me1', '550.0', '600.0', 'all=100');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'me2', 'me2', 'all=2000');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'me2.1', 'me2.1', '553.0', '631.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'me2.2', 'me2.2', '520.0', '600.1');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'e1', 'e1', 'all=500');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.1', 'e1.1', '20%', '20%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'e1.2', 'e1.2', '55%', '55%');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2019', 'of1', 'of1', 'all=1000', '202%', '201%', 'vac=');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'of2', 'of2', '1000|1000|1000|1000|1000|1000|1000|1000|0|0|0|0');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.1', 'of2.1', '210.0', '210.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'of2.2', 'of2.2', '202.10', '202.10');


INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '1000', '701.0', '200.0', 'cx1', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '2000', '701.0', '210.0', 'cx2', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '3000', '220.0', '701.0', 'cx3', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '4000', '701.0', '230.0', 'cx4', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '18000', '400.0', '701.0', 'cx4b', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '1100', '701.0', '200.0', 'cx5', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '2200', '701.0', '210.0', 'cx6', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '3300', '220.0', '701.0', 'cx7', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '4400', '701.0', '230.0', 'cx8', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '5800', '400.0', '701.0', 'cx8b', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '1500', '701.0', '200.0', 'cx9', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '2500', '701.0', '210.0', 'cx10', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '3500', '220.0', '701.0', 'cx11', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '4500', '701.0', '230.0', 'cx12', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '5140', '400.0', '701.0', 'cx12b', 'closing');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '1000', '600.0', '710.0', 'px1', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '2000', '710.0', '550.0', 'px2', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '3000', '631.0', '710.0', 'px3', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '4000', '710.0', '510.0', 'px4', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '5000', '710.0', '520.0', 'px5', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '6000', '710.0', '530.0', 'px6', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '7000', '630.0', '710.0', 'px7', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2017', '3112', '8000', '710.0', '553.0', 'px8', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '100', '600.0', '710.0', 'px12', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '200', '710.0', '550.0', 'px22', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '300', '631.0', '710.0', 'px32', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '400', '710.0', '510.0', 'px42', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '500', '710.0', '520.0', 'px52', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '600', '710.0', '530.0', 'px62', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '700', '630.0', '710.0', 'px72', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '3112', '800', '710.0', '553.0', 'px82', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '10', '600.0', '710.0', 'px13', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '20', '710.0', '550.0', 'px23', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '30', '631.0', '710.0', 'px33', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '40', '710.0', '510.0', 'px43', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '50', '710.0', '520.0', 'px53', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '60', '710.0', '530.0', 'px63', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '70', '630.0', '710.0', 'px73', 'closing');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2019', '3112', '80', '710.0', '553.0', 'px83', 'closing');

INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '600', 'account600', 'R');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '510', 'account510', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2020', '520', 'account520', 'E');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '550', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '600', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '510', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '520', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2020', '630', '0', 'general', '');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1209', '1000', '550.0', '600.0', 'px91', '');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1209', '1000', '510.0', '520.0', 'px92', '');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2020', '1209', '1000', '400.0', '630.0', 'px93', '');


INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '5', 'class5', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '50', 'group50', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '500', 'account500', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '51', 'group51', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '510', 'account510', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '52', 'group52', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '520', 'account520', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '2', 'class2', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '20', 'group20', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2018', '200', 'account200', 'L');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2018', '500', '0', 'general', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2018', '510', '0', 'doprava', 'view=zzz');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2018', '520', '0', 'dolava', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2018', '200', '0', 'cash', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '2001', '1000', '500.0', '200.0', '18x1', 'vac=xxx');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '1102', '2000', '510.0', '200.0', '18x2', 'vac=xxx');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0402', '3000', '500.0', '200.0', '18x3', 'vac=xxx');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0403', '3000', '500.0', '200.0', '18x4', 'vac=yyy');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0503', '3000', '510.0', '200.0', '18x5', 'vac=yyy');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0603', '1000', '520.0', '510.0', '18x6', 'vac=yyy');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0703', '1000', '200.0', '500.0', '18x7', 'vac=yyy');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0905', '100', '520.0', '200.0', '18x8', 'view=zzz');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0704', '100', '200.0', '520.0', '18x9', 'view=zzz');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2018', '0707', '1', '200.0', '500.0', '18x10', 'view=ww');


INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '700', 'account700', 'X');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '2', 'class2', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '20', 'group20', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '200', 'account200', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '23', 'group23', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '230', 'account230', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '231', 'account231', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '5', 'class5', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '54', 'group54', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '546', 'account546', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '544', 'account544', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '6', 'class6', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '62', 'group62', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2021', '624', 'account624', 'R');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '700', '0', 'inits', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '200', '0', 'cash', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '230', '0', 'fin0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '230', '1', 'fin0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '231', '0', 'fin1-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '546', '0-0', 'cre0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '546', '0-1', 'cre0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '546', '1-0', 'cre1-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '544', '0-0', 'erev0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '544', '0-1', 'erev0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '544', '1-0', 'erev1-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '624', '0-0', 'rrev0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '624', '0-1', 'rrev0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2021', '624', '1-0', 'rrev1-0', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0101', '1000', '230.0', '700.0', '21x1', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0101', '2000', '230.1', '700.0', '21x2', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0101', '3000', '231.0', '700.0', '21x3', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0102', '1000', '546.0-1', '200.0', '21x4', 'cre02');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0103', '1000', '546.0-1', '200.0', '21x5', 'cre03');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0104', '1000', '544.0-1', '230.1', '21x6', 'nrev04');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0106', '1000', '230.1', '624.0-1', '21x7', 'rrev06');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0108', '1000', '200.0', '230.1', '21x8', 'with08');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0109', '1000', '230.1', '624.0-1', '21x9', 'rrev09');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0110', '1000', '546.0-1', '200.0', '21x10', 'cre10');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2021', '0111', '1000', '544.0-1', '230.1', '21x11', 'nrev11');

INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '7', 'class7', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '70', 'group70', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '700', 'account700', 'X');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '2', 'class2', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '20', 'group20', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '200', 'account200', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '23', 'group23', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '230', 'account230', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '231', 'account231', 'A');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '5', 'class5', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '54', 'group54', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '546', 'account546', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '544', 'account544', 'E');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '6', 'class6', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '62', 'group62', '');
INSERT INTO ASchema (year, id, name, type) VALUES ('2022', '624', 'account624', 'R');

INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '700', '0', 'inits', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '200', '0', 'cash', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '230', '0', 'fin0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '230', '1', 'fin0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '231', '0', 'fin1-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '546', '0-0', 'cre0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '546', '0-1', 'cre0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '546', '1-0', 'cre1-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '544', '0-0', 'erev0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '544', '0-1', 'erev0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '544', '1-0', 'erev1-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '624', '0-0', 'rrev0-0', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '624', '0-1', 'rrev0-1', '');
INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES ('2022', '624', '1-0', 'rrev1-0', '');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0101', '1000', '230.0', '700.0', '22x1', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0101', '1000', '230.1', '700.0', '22x2', 'init');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0101', '3000', '231.0', '700.0', '22x3', 'init');

INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0102', '1000', '546.1-0', '200.0', '22x4', 'cre02');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0103', '1000', '546.1-0', '200.0', '22x5', 'cre03');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '1503', '1500', '231.0', '624.1-0', '22x6', 'rrev03');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0104', '500', '544.1-0', '231.0', '22x7', 'nrev04');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0106', '1000', '546.1-0', '200.0', '22x8', 'cre06');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '1506', '1000', '231.0', '624.1-0', '22x9', 'rrev06');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0108', '1000', '200.0', '231.0', '22x10', 'with08');
INSERT INTO Transaction (year, date, amount, debit, credit, id, description) VALUES ('2022', '0111', '1000', '544.1-0', '231.0', '22x11', 'nrev11');
