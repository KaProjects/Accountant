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

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'i1', 'RedHat', '56000|56000|56000|56000|56000|56000|56000|0|0|0|0|0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i1.1', 'RedHat - Net Salary', '210.0', '302.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i1.2', 'RedHat - Stravenky', '211.6', '601.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i1.3', 'RedHat - Volny Cas', '211.1', '601.4');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'i2', 'Oracle', '0|0|0|0|0|0|0|0|0|142000|70000|70000');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i2.1', 'Oracle - Net Salary', '210.0', '302.18');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i2.2', 'Oracle - Stravenky', '211.8', '601.5');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i2.3', 'Oracle - Volny Cas', '211.9', '601.6');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i3', 'Podpora', '210.0', '633.0');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'i4', 'MultiSport', '210.0', '302.4', 'all=500');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me1', 'Najom', '520.0', '210.0', 'all=10275');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'me2', 'Parking', '2500|2500|2500|2500|2500|2500|2500|2500|2500|2500|2500|2500');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me2.1', 'Domini park', '502.3-3', '092.3-3');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me2.2', 'Kozi parking', '523.10', '');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'me3', 'Metlife', 'all=1711');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me3.1', 'poistenie', '553.0', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me3.2', 'sporenie', '546.0-2', '');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me4', 'splatky CSOB', '220.26', '210.0', 'all=9069');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'me5', 'splatky AB', 'all=5238');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'me5.1', 'splatkyAB22', '220.27', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'me5.2', 'splatkyAB23', '220.28', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'me5.3', 'splatkyAB24', '220.29', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me6', 'splatky SPF', '412.0', '', 'all=2194');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'e1', 'Consumption', 'all=7000');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e1.1', 'Hlavne Kategorie', '510', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e1.2', 'Pohonne Hmoty', '511', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e1.3', 'Neodpisovany Majetok', '512', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e1.4', 'Drogeria', '513', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e1.5', 'Lieky & Doplnky Stravy', '514', '');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'e2', 'Prevadzky', 'all=6000');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e2.1', 'Restauracie', '530', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e2.2', 'Fast Food', '531', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e2.3', 'Kaviarne', '532', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e2.4', 'Bary', '533', '');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'e2.5', 'Bufety', '534', '');



