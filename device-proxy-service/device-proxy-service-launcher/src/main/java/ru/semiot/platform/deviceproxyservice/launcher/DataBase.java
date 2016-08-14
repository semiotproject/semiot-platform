package ru.semiot.platform.deviceproxyservice.launcher;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {

  private static final Logger logger = LoggerFactory.getLogger(DataBase.class);
  private static final ServiceConfig config = ConfigFactory.create(ServiceConfig.class);
  private static final String INSERT_QUERY =
      "insert ignore into credentials values ('0', '${LOGIN}', '${PASSWORD}', 'internal');";

  public static Connection connection() {
    Connection connection = null;
    try {
      Class.forName("com.mysql.jdbc.Driver");
      logger.debug("MySQL JDBC Driver Registered!");
      connection = DriverManager.getConnection(
          "jdbc:mysql://mysqlbase:3306/semiot?zeroDateTimeBehavior=convertToNull", "root", "");// mysqlbase
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return connection;
  }

  public static void insertUser(Connection connection) {
    Statement stmt = null;
    try {
      if (connection != null) {
        logger.debug("You made it, take control your database now!");
        stmt = connection.createStatement();
        stmt.executeUpdate(INSERT_QUERY.replace("${LOGIN}", config.wampLogin())
            .replace("${PASSWORD}", config.wampPassword()));
      } else {
        logger.warn("Failed to make connection!");
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          logger.error(e.getMessage(), e);
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          logger.error(e.getMessage(), e);
        }
      }
    }
  }

}

