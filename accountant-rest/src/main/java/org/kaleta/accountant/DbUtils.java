package org.kaleta.accountant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Stanley on 17.5.2018.
 */
public class DbUtils {

    public static void createTable() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        //Class.forName("com.mysql.jdbc.Driver").newInstance();
        String url = "jdbc:mysql://192.168.99.100:3306/testdb";
        String query = "select * from testt";
        try (Connection connection = DriverManager.getConnection(url, "root", "mypassword")) {
            try (Statement statement = connection.createStatement()) {
//                try (ResultSet resultSet = statement.executeQuery(query)){
//                    while (resultSet.next()) {
//                        System.out.println("[ " + resultSet.getInt(1) + " | "
//                                + resultSet.getString(2) + " ]");
//                    }
//                }

//                statement.executeUpdate("insert into testt values (3, \"forth\")");

                statement.executeUpdate("CREATE TABLE testt (id INT NOT NULL auto_increment, _KEY varchar(255), _VALUE varchar(255), PRIMARY KEY (id));");
            }
        }
    }

}
