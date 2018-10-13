# RUN DATABASE CONTAINER
docker run -p 3306:3306 --detach --name=test-mysql --env="MYSQL_ROOT_PASSWORD=mypassword" mysql
docker run -p 3306:3306 --detach --name=test-mysql --env="MYSQL_ROOT_PASSWORD=mypassword" --volume=//c/Users/Stanley/Documents/DOCKER_DATA/mysql:/var/lib/mysql mysql
docker run -p 3306:3306 --detach --name=test-mysql --env="MYSQL_ROOT_PASSWORD=mypassword" --volume=/c/Users/Stanley/Documents/DOCKER_DATA/mysql:/var/lib/mysql mysql --innodb_flush_method=O_DSYNC --innodb-use-native-aio=0 --log_bin=ON

# CONNECT TO DATABASE CONTAINER
docker-machine ip default
Downloads\mysql-8.0.11-winx64\bin\mysql.exe -uroot -pmypassword -h 192.168.99.100 -P 3306

# DATABASE MAINTENANCE
CREATE DATABASE testdb;
USE testdb

CREATE TABLE testt (id INT NOT NULL auto_increment, _KEY varchar(255), _VALUE varchar(255), PRIMARY KEY (id));
SHOW TABLES;
DESCRIBE testt;

SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA='testdb';

# HANDLE APP DOCKER IMAGE 
mvn -Pdockerimage package