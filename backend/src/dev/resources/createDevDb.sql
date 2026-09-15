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
DELETE FROM Budgeting;
-------
-- 2024
-------
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'i1', 'Income 1', '94060|85750|23580|37040|23460|53670|12230|62980|48560|27140|83310|68730');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'i1.1', 'Income 1.1', '210.0', '302.18');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'i1.2', 'Income 1.2', '211.8', '601.5');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'i1.3', 'Income 1.3', '211.9', '601.6');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'i2', 'Income 2', 'all=30880');  -- UPDATE LATER
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'i2.1', 'Income 2.1', '210.0', '302.4');  -- UPDATE LATER

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2024', 'me1', 'Fixed Cost 1', '520.0', '520.0', 'all=15240');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2024', 'me2', 'Fixed Cost 2', '220.26', '210.0', 'all=2020');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'me3', 'Fixed Cost 3', 'all=24950');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'me3.1', 'Fixed Cost 3.1', '220.28', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'me3.2', 'Fixed Cost 3.2', '220.29', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2024', 'me4', 'Fixed Cost 4', '412.0', '412.0', 'all=3520');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2024', 'me5', 'Fixed Cost 5', '524.1', '524.1', 'all=300');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'e1', 'Spending 1', 'all=42930');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e1.1', 'Spending 1.1', '510%', '510%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e1.2', 'Spending 1.2', '511%', '511%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e1.3', 'Spending 1.3', '513%', '513%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e1.4', 'Spending 1.4', '514%', '514%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'e2', 'Spending 2', 'all=1210');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e2.1', 'Spending 2.1', '530%', '530%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e2.2', 'Spending 2.2', '531%', '531%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e2.3', 'Spending 2.3', '532%', '532%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'e2.4', 'Spending 2.4', '533%', '533%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2024', 'of1', 'Other Flow 1', 'all=46560', '5%', '5%', 'vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'of2', 'Other Flow 2', 'all=9120');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.1.1', 'Other Flow 2.1.1', '546.0-0', '230.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.1.2', 'Other Flow 2.1.2', '546.0-9', '230.9', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.2.1', 'Other Flow 2.2.1', '546.1-0', '231.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.2.2', 'Other Flow 2.2.2', '546.1-1', '231.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.3.1', 'Other Flow 2.3.1', '546.2-0', '232.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.4.1', 'Other Flow 2.4.1', '546.3-0', '233.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2024', 'of2.4.2', 'Other Flow 2.4.2', '546.3-1', '233.1', 'finXasset');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2024', 'of3', 'Other Flow 3', 'all=0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'of3.1', 'Other Flow 3.1', '211.7', '211.7');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'of3.2', 'Other Flow 3.2', '211.0', '211.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2024', 'of3.3', 'Other Flow 3.3', '211.10', '211.10');
-------
-- 2023
-------
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2023', 'i1', 'Income 1', '69860|95460|31600|74300|25630|65560|49800|36720|51150|82960|43200|27490');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2023', 'i1.1', 'Income 1.1', '210.0', '302.18');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2023', 'i1.2', 'Income 1.2', '211.8', '601.5');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2023', 'i1.3', 'Income 1.3', '211.9', '601.6');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2023', 'i2', 'Income 2', '210.0', '302.4', 'all=400');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2023', 'me1', 'Fixed Cost 1', '520.0', '520.0', '24370|50540|71130|29490|49570|28890|31060|11320|53600|26150|29470|53700');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2023', 'me2', 'Fixed Cost 2', '523.10', '523.10', '4420|2080|9810|4950|3740|3460|9370|4180|5970|9090|7040|7660');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2023', 'me3', 'Fixed Cost 3', '220.26', '210.0', 'all=5500');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2023', 'me4', 'Fixed Cost 4', 'all=13700');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'me4.1', 'Fixed Cost 4.1', '220.28', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'me4.2', 'Fixed Cost 4.2', '220.29', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2023', 'me5', 'Fixed Cost 5', '412.0', '412.0', 'all=5940');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2023', 'me6', 'Fixed Cost 6', '524.1', '524.1', 'all=500');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2023', 'e1', 'Spending 1', 'all=1930');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e1.1', 'Spending 1.1', '510%', '510%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e1.2', 'Spending 1.2', '511%', '511%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e1.4', 'Spending 1.4', '513%', '513%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e1.5', 'Spending 1.5', '514%', '514%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2023', 'e2', 'Spending 2', 'all=2900');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e2.1', 'Spending 2.1', '530%', '530%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e2.2', 'Spending 2.2', '531%', '531%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e2.3', 'Spending 2.3', '532%', '532%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'e2.4', 'Spending 2.4', '533%', '533%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning, debit, credit) VALUES ('2023', 'e4', 'Spending 4', 'all=1420', '542.0', '542.0');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2023', 'of1', 'Other Flow 1', 'all=4460', '5%', '5%', 'vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2023', 'of2', 'Other Flow 2', '23190|36200|99120|57840|17000|33190|68990|87600|40010|97350|91160|84990');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.1.1', 'Other Flow 2.1.1', '546.0-0', '230.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.1.2', 'Other Flow 2.1.2', '546.0-2', '230.2', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.2.1', 'Other Flow 2.2.1', '546.1-0', '231.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.2.2', 'Other Flow 2.2.2', '546.1-1', '231.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.3.1', 'Other Flow 2.3.1', '546.2-0', '232.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.4.1', 'Other Flow 2.4.1', '546.3-0', '233.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2023', 'of2.4.2', 'Other Flow 2.4.2', '546.3-1', '233.1', 'finXasset');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2023', 'of3', 'Other Flow 3', '46330|82150|95830|66880|72680|61730|27020|88090|30490|50550|25980|89200');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2023', 'of3.1', 'Other Flow 3.1', '211.7', '211.7');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2023', 'of3.2', 'Other Flow 3.2', '211.0', '211.0');
-------
-- 2022
-------
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'i1', 'Income 1', 'all=80670');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i1.1', 'Income 1.1', '210.0', '302.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i1.2', 'Income 1.2', '211.6', '601.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i1.3', 'Income 1.3', '211.1', '601.4');
INSERT INTO Budgeting (year, id, name) VALUES ('2022', 'i2', 'Income 2');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i2.1', 'Income 2.1', '210.0', '302.18');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i2.2', 'Income 2.2', '211.8', '601.5');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i2.3', 'Income 2.3', '211.9', '601.6');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'i3', 'Income 3', '210.0', '633.0');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'i4', 'Income 4', '210.0', '302.4', 'all=410');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me1', 'Fixed Cost 1', '520.0', '210.0', 'all=17140');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'me2', 'Fixed Cost 2', 'all=4950');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me2.1', 'Fixed Cost 2.1', '502.3-3', '092.3-3');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me2.2', 'Fixed Cost 2.2', '523.10', '523.10');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'me3', 'Fixed Cost 3', 'all=4000');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me3.1', 'Fixed Cost 3.1', '553.0', '553.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'me3.2', 'Fixed Cost 3.2', '546.0-2', '546.0-2');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me4', 'Fixed Cost 4', '220.26', '210.0', 'all=8120');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'me5', 'Fixed Cost 5', 'all=3240');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'me5.1', 'Fixed Cost 5.1', '220.27', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'me5.2', 'Fixed Cost 5.2', '220.28', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'me5.3', 'Fixed Cost 5.3', '220.29', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me6', 'Fixed Cost 6', '412.0', '412.0', 'all=2640');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2022', 'me7', 'Fixed Cost 7', '524.1', '524.1', 'all=260');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'e1', 'Spending 1', 'all=4760');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e1.1', 'Spending 1.1', '510%', '510%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e1.2', 'Spending 1.2', '511%', '511%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e1.4', 'Spending 1.4', '513%', '513%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e1.5', 'Spending 1.5', '514%', '514%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'e2', 'Spending 2', 'all=4920');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e2.1', 'Spending 2.1', '530%', '530%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e2.2', 'Spending 2.2', '531%', '531%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e2.3', 'Spending 2.3', '532%', '532%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'e2.4', 'Spending 2.4', '533%', '533%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning, debit, credit) VALUES ('2022', 'e3', 'Spending 3', 'all=8000', '542.0', '542.0');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2022', 'of1', 'Other Flow 1', 'all=98210', '5%', '5%', 'vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'of2', 'Other Flow 2', 'all=47400');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.1', 'Other Flow 2.1.1', '546.0-0', '230.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.2', 'Other Flow 2.1.2', '546.0-2', '230.2', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.3', 'Other Flow 2.1.3', '546.0-3', '230.3', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.4', 'Other Flow 2.1.4', '546.0-6', '230.6', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.5', 'Other Flow 2.1.5', '546.0-7', '230.7', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.6', 'Other Flow 2.1.6', '546.0-8', '230.8', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.1.7', 'Other Flow 2.1.7', '546.0-9', '230.9', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.2.1', 'Other Flow 2.2.1', '546.1-0', '231.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.2.2', 'Other Flow 2.2.2', '546.1-1', '231.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.3.1', 'Other Flow 2.3.1', '546.2-0', '232.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.4.1', 'Other Flow 2.4.1', '546.3-0', '233.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2022', 'of2.4.2', 'Other Flow 2.4.2', '546.3-1', '233.1', 'finXasset');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2022', 'of3', 'Other Flow 3', 'all=6500');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'of3.1', 'Other Flow 3.1', '211.7', '211.7');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2022', 'of3.2', 'Other Flow 3.2', '211.0', '211.0');
-------
-- 2021
-------
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'i1', 'Income 1', 'all=83470');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'i1.1', 'Income 1.1', '210.0', '302.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'i1.2', 'Income 1.2', '211.6', '601.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'i1.3', 'Income 1.3', '211.1', '601.4');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'i1.4', 'Income 1.4', '312.2', '601.4');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'i1.5', 'Income 1.5', '210.0', '601.4', 'allowance');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2021', 'i2', 'Income 2', '210.0', '302.4', 'all=610');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2021', 'i3', 'Income 3', '632.0', '632.0', 'all=910');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2021', 'me1', 'Fixed Cost 1', '520.0', '210.0', 'all=88930');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'me2', 'Fixed Cost 2', 'all=3260');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'me2.1', 'Fixed Cost 2.1', '502.3-3', '092.3-3');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'me2.2', 'Fixed Cost 2.2', '502.3-2', '092.3-2');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'me3', 'Fixed Cost 3', 'all=5940');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'me3.1', 'Fixed Cost 3.1', '553.0', '553.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'me3.2', 'Fixed Cost 3.2', '546.0-2', '546.0-2');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'me4', 'Fixed Cost 4', 'all=1000');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'me4.1', 'Fixed Cost 4.1', '220.20', '210.0', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'me4.2', 'Fixed Cost 4.2', '220.26', '210.0', 'splatka');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'me5', 'Fixed Cost 5', 'all=2820');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'me5.1', 'Fixed Cost 5.1', '220.23', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'me5.2', 'Fixed Cost 5.2', '220.24', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'me5.3', 'Fixed Cost 5.3', '220.25', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'me5.4', 'Fixed Cost 5.4', '220.27', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2021', 'me6', 'Fixed Cost 6', '412.0', '412.0', 'all=0');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2021', 'me7', 'Fixed Cost 7', '524.1', '524.1', 'all=80');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'e1', 'Spending 1', 'all=1590');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.0', 'Spending 1.0', '510.0-%', '510.0-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.1', 'Spending 1.1', '510.1-%', '510.1-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.2', 'Spending 1.2', '510.2-%', '510.2-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.3', 'Spending 1.3', '510.3-%', '510.3-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.4', 'Spending 1.4', '510.4-%', '510.4-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.5', 'Spending 1.5', '510.5-%', '510.5-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.6', 'Spending 1.6', '510.6-%', '510.6-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.7', 'Spending 1.7', '510.7-%', '510.7-%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e1.9', 'Spending 1.9', '510.9-%', '510.9-%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'e2', 'Spending 2', 'all=5950');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e2.1', 'Spending 2.1', '530%', '530%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e2.2', 'Spending 2.2', '531%', '531%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e2.3', 'Spending 2.3', '532%', '532%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e2.4', 'Spending 2.4', '533%', '533%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'e2.5', 'Spending 2.5', '534%', '534%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning, debit, credit) VALUES ('2021', 'e3', 'Spending 3', 'all=5220', '542.0', '542.0');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2021', 'of1', 'Other Flow 1', 'all=1230', '5%', '5%', 'vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'of2', 'Other Flow 2', 'all=7500');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.1', 'Other Flow 2.1.1', '546.0-0', '230.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.2', 'Other Flow 2.1.2', '546.0-2', '230.2', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.3', 'Other Flow 2.1.3', '546.0-3', '230.3', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.4', 'Other Flow 2.1.4', '546.0-6', '230.6', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.5', 'Other Flow 2.1.5', '546.0-7', '230.7', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.6', 'Other Flow 2.1.6', '546.0-8', '230.8', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.1.7', 'Other Flow 2.1.7', '546.0-9', '230.9', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.2.1', 'Other Flow 2.2.1', '546.1-0', '231.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.2.2', 'Other Flow 2.2.2', '546.1-1', '231.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.3.1', 'Other Flow 2.3.1', '546.2-0', '232.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2021', 'of2.4.1', 'Other Flow 2.4.1', '546.3-0', '233.0', 'finXasset');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2021', 'of3', 'Other Flow 3', 'all=4120');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'of3.1', 'Other Flow 3.1', '211.7', '211.7');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2021', 'of3.2', 'Other Flow 3.2', '211.0', '211.0');
-------
-- 2020
-------
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'i1', 'Income 1', 'all=21500');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'i1.1', 'Income 1.1', '210.0', '302.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'i1.2', 'Income 1.2', '', '601.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'i1.3', 'Income 1.3', '211.1', '601.4');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'i1.4', 'Income 1.4', '302.0', '601.4');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2020', 'i2', 'Income 2', '210.0', '302.4', 'all=880');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2020', 'i3', 'Income 3', '632.0', '632.0', 'all=760');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2020', 'me1', 'Fixed Cost 1', '520.0', '210.0', 'all=83560');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'me2', 'Fixed Cost 2', 'all=7980');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'me2.1', 'Fixed Cost 2.1', '502.3-2', '092.3-2');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'me2.2', 'Fixed Cost 2.2', '502.3-1', '092.3-1');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'me3', 'Fixed Cost 3', 'all=1450');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'me3.1', 'Fixed Cost 3.1', '553.0', '553.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'me3.2', 'Fixed Cost 3.2', '546.2', '546.2');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'me4', 'Fixed Cost 4', 'all=6190');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me4.1', 'Fixed Cost 4.1', '220.4', '210.0', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me4.2', 'Fixed Cost 4.2', '220.20', '210.0', 'splatka');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'me5', 'Fixed Cost 5', 'all=7130');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me5.1', 'Fixed Cost 5.1', '220.19', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me5.2', 'Fixed Cost 5.2', '220.21', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me5.3', 'Fixed Cost 5.3', '220.22', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me5.4', 'Fixed Cost 5.4', '220.23', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'me5.5', 'Fixed Cost 5.5', '220.24', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2020', 'me6', 'Fixed Cost 6', '412.0', '412.0', 'all=0');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2020', 'me7', 'Fixed Cost 7', '524.1', '524.1', 'all=90');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'e1', 'Spending 1', 'all=7100');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.0', 'Spending 1.0', '510%', '510%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.1', 'Spending 1.1', '511%', '511%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.2', 'Spending 1.2', '512%', '512%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.3', 'Spending 1.3', '513%', '513%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.4', 'Spending 1.4', '514%', '514%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.5', 'Spending 1.5', '515%', '515%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.60', 'Spending 1.60', '516.0%', '516.0%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.61', 'Spending 1.61', '516.1%', '516.1%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.62', 'Spending 1.62', '516.2%', '516.2%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e1.63', 'Spending 1.63', '516.3%', '516.3%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'e2', 'Spending 2', 'all=5660');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e2.1', 'Spending 2.1', '530%', '530%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e2.2', 'Spending 2.2', '531%', '531%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e2.3', 'Spending 2.3', '532%', '532%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e2.4', 'Spending 2.4', '533%', '533%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'e2.5', 'Spending 2.5', '534%', '534%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning, debit, credit) VALUES ('2020', 'e3', 'Spending 3', 'all=8810', '542.0', '542.0');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2020', 'of1', 'Other Flow 1', 'all=6270', '5%', '5%', 'vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'of2', 'Other Flow 2', 'all=1030');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.0', 'Other Flow 2.0', '546.0', '230.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.1', 'Other Flow 2.1', '546.1', '230.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.2', 'Other Flow 2.2', '546.2', '230.2', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.3', 'Other Flow 2.3', '546.3', '230.3', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.4', 'Other Flow 2.4', '546.4', '230.4', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.5', 'Other Flow 2.5', '546.5', '230.5', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.6', 'Other Flow 2.6', '546.6', '230.6', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.7', 'Other Flow 2.7', '546.7', '230.7', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.8', 'Other Flow 2.8', '546.8', '230.8', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2020', 'of2.9', 'Other Flow 2.9', '546.9', '230.9', 'finXasset');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2020', 'of3', 'Other Flow 3', 'all=6450');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'of3.1', 'Other Flow 3.1', '211.7', '211.7');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2020', 'of3.2', 'Other Flow 3.2', '211.0', '211.0');
-------
-- 2019
-------
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'i1', 'Income 1', 'all=51940');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i1.1', 'Income 1.1', '210.0', '302.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i1.2', 'Income 1.2', '601.0', '601.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i1.3', 'Income 1.3', '601.4', '601.4');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'i2', 'Income 2', 'all=210');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i2.1', 'Income 2.1', '210.0', '302.4');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i2.2', 'Income 2.2', '210.0', '600.1');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'i2.3', 'Income 2.3', '220.0', '302.4');

INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2019', 'me1', 'Fixed Cost 1', '520.0', '210.0', 'all=36300');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'me2', 'Fixed Cost 2', 'all=9620');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'me2.1', 'Fixed Cost 2.1', '502.3-1', '092.3-1');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'me2.2', 'Fixed Cost 2.2', '502.3-0', '092.3-0');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'me3', 'Fixed Cost 3', 'all=8300');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'me3.1', 'Fixed Cost 3.1', '553.0', '553.0');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'me3.2', 'Fixed Cost 3.2', '546.2', '546.2');
INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2019', 'me4', 'Fixed Cost 4', 'all=2960', '220.4', '210.0', 'splatka');
INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'me5', 'Fixed Cost 5', 'all=8780');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.01', 'Fixed Cost 5.01', '220.5', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.02', 'Fixed Cost 5.02', '220.7', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.03', 'Fixed Cost 5.03', '220.8', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.04', 'Fixed Cost 5.04', '220.9', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.05', 'Fixed Cost 5.05', '220.10', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.06', 'Fixed Cost 5.06', '220.11', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.07', 'Fixed Cost 5.07', '220.12', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.08', 'Fixed Cost 5.08', '220.13', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.09', 'Fixed Cost 5.09', '220.15', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.10', 'Fixed Cost 5.10', '220.16', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.11', 'Fixed Cost 5.11', '220.17', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'me5.12', 'Fixed Cost 5.12', '220.18', '210.1', 'splatka');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2019', 'me6', 'Fixed Cost 6', '412.0', '412.0', 'all=2620');
INSERT INTO Budgeting (year, id, name, debit, credit, planning) VALUES ('2019', 'me7', 'Fixed Cost 7', '524.1', '524.1', 'all=590');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'e1', 'Spending 1', 'all=9660');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.0', 'Spending 1.0', '510%', '510%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.1', 'Spending 1.1', '511%', '511%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.2', 'Spending 1.2', '512%', '512%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.3', 'Spending 1.3', '513%', '513%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.4', 'Spending 1.4', '514%', '514%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.5', 'Spending 1.5', '515%', '515%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.60', 'Spending 1.60', '516.0%', '516.0%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.61', 'Spending 1.61', '516.1%', '516.1%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.62', 'Spending 1.62', '516.2%', '516.2%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e1.63', 'Spending 1.63', '516.3%', '516.3%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'e2', 'Spending 2', 'all=4600');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e2.1', 'Spending 2.1', '530%', '530%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e2.2', 'Spending 2.2', '531%', '531%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e2.3', 'Spending 2.3', '532%', '532%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e2.4', 'Spending 2.4', '533%', '533%', '!vac=');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'e2.5', 'Spending 2.5', '534%', '534%', '!vac=');

INSERT INTO Budgeting (year, id, name, planning, debit, credit) VALUES ('2019', 'e3', 'Spending 3', 'all=1300', '542.0', '542.0');

INSERT INTO Budgeting (year, id, name, planning, debit, credit, description) VALUES ('2019', 'of1', 'Other Flow 1', 'all=3810', '5%', '5%', 'vac=');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'of2', 'Other Flow 2', 'all=5850');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.0', 'Other Flow 2.0', '546.0', '230.0', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.1', 'Other Flow 2.1', '546.1', '230.1', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.2', 'Other Flow 2.2', '546.2', '230.2', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.3', 'Other Flow 2.3', '546.3', '230.3', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.4', 'Other Flow 2.4', '546.4', '230.4', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.5', 'Other Flow 2.5', '546.5', '230.5', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.6', 'Other Flow 2.6', '546.6', '230.6', 'finXasset');
INSERT INTO Budgeting (year, id, name, debit, credit, description) VALUES ('2019', 'of2.7', 'Other Flow 2.7', '546.7', '230.7', 'finXasset');

INSERT INTO Budgeting (year, id, name, planning) VALUES ('2019', 'of3', 'Other Flow 3', 'all=4320');
INSERT INTO Budgeting (year, id, name, debit, credit) VALUES ('2019', 'of3.1', 'Other Flow 3.1', '211.0', '211.0');
