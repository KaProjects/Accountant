# RUN DATABASE CONTAINER
docker run -p 3306:3306 --detach --name=test-mysql --env="MYSQL_ROOT_PASSWORD=mypassword" mysql

# CONNECT TO DATABASE CONTAINER
docker-machine ip default
Downloads\mysql-8.0.11-winx64\bin\mysql.exe -uroot -pmypassword -h 192.168.99.100 -P 3306

# DATABASE MAINTENANCE
CREATE DATABASE testdb;
USE testdb

SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA='testdb';