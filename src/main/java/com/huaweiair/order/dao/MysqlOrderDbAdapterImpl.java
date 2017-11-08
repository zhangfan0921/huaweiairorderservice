
package com.huaweiair.order.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweiair.order.model.FlightOrder;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

/**
 * 
 * mysql -uroot -phuawei@123; use "+tableName+"; select * from "+tableName+";
 * 
 * 
 * create database huaweiair_order; CREATE TABLE "+tableName+" ( orderId varchar(255), flightId
 * varchar(255), name varchar(255), flightTime varchar(255), flightClass int, flightPrice int,
 * orderTime varchar(255), orderStatus int, PRIMARY KEY (orderId) );
 * 
 * <br/>
 * <p>
 * </p>
 * 
 * @author tank
 */
public class MysqlOrderDbAdapterImpl implements OrderDbAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(MysqlOrderDbAdapterImpl.class);
  private String ip = "mysql";
  private int port = 3306;
  private String dbUserName = "root";
  private String dbPassword = "huawei@123";
  private String jdbcName = "com.mysql.jdbc.Driver";
  private String databaseName = "huaweiair_order";
  private String tableName = "huaweiair_order";
//  private Statement stmt;
  private Connection con;

  public MysqlOrderDbAdapterImpl() {
    initDb();
  }

  public MysqlOrderDbAdapterImpl(String ip, int port) {
    if (null != ip) {
      this.ip = ip;
    }
    if (port > 0) {
      this.port = port;
    }
    initDb();
  }

  public MysqlOrderDbAdapterImpl(String ip, int port, String databaseName, String tableName, String dbUserName,
      String dbPassword) {
    if (null != ip) {
      this.ip = ip;
    }
    if (port > 0) {
      this.port = port;
    }
    if (null != databaseName && !tableName.isEmpty()) {
      this.databaseName = databaseName;
    }
    if (null != tableName && !tableName.isEmpty()) {
      this.tableName = tableName;
    }
    if (null != dbUserName && !dbUserName.isEmpty()) {
      this.dbUserName = dbUserName;
    }
    if (null != dbPassword && !dbPassword.isEmpty()) {
      this.tableName = dbPassword;
    }
    initDb();
  }

  /**
   * 当数据库不存在就创建库，初始化表 <br/>
   * 
   * @param stmt
   */
  private void initDb() {
	Statement stmt;
    try {
      Class.forName(jdbcName);
      LOGGER.info("DB driver load success.");
    } catch (ClassNotFoundException e) {
      LOGGER.error("DB driver load error:", e);
    }
    try {
      LOGGER.info("Get DB connection start.");
      con = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/", dbUserName, dbPassword);
      LOGGER.info("Get DB connection success.");
      stmt = con.createStatement();
    } catch (SQLException e) {
      LOGGER.error("Get DB connection error: ", e);
      return;
    }

    try {
      stmt.execute("use " + databaseName);
    } catch (SQLException e) {
      if (e instanceof MySQLSyntaxErrorException) {
        if (e.getMessage().contains("Unknown database")) {
          try {
            stmt.execute("create database " + databaseName);
            stmt.execute("use " + databaseName);
            stmt.execute("CREATE TABLE " + tableName
                + " ( orderId varchar(255),userId varchar(255), flightId varchar(255), name varchar(255), scheduledDepartureTime varchar(255), scheduledArrivalTime varchar(255), flightClass int, flightPrice int, orderTime varchar(255), orderStatus int, PRIMARY KEY (orderId) ) ");
          } catch (SQLException e1) {
            LOGGER.error("execute sql error: ", e1);
          }
        }
      } else {
        LOGGER.error("execute sql error: ", e);
      }
    }finally{
    	try {
    		if (stmt != null) {
    			stmt.close();
    		}
		} catch (SQLException e) {
			LOGGER.error("closed statement error: ", e);
		}
    }
  }

  @Override
  public boolean insertOrder(FlightOrder order) {
	Statement stmt = null;
    String sql = "INSERT INTO " + tableName + " VALUES ('" + order.getOrderId() + "', " + "'" + order.getUserId()
        + "', '" + order.getFlightId() + "', '" + order.getName() + "', '" + order.getScheduledDepartureTime() + "', '"
        + order.getScheduledArrivalTime() + "', '" + order.getFlightClass() + "', '" + order.getFlightPrice() + "', '"
        + order.getOrderTime() + "', '" + order.getOrderStatus() + "')";
    try {

    	stmt = con.createStatement();
    	stmt.executeUpdate(sql);
      return true;
    } catch (SQLException e) {
      LOGGER.error("insertOrder  error: ", e);
    }finally{
    	if (stmt != null) {
    		try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error("close statement error: ", e);
			}
    	}
    }
    return false;
  }


  @Override
  public boolean updateOrder(String orderId, int action) {
	  Statement stmt = null;
    String sql = "UPDATE " + tableName + " SET orderstatus='" + action + "'  WHERE orderId='" + orderId + "'";
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(sql);
      return true;
    } catch (SQLException e) {
      LOGGER.error("updateOrder  error: ", e);
    }finally {
    	if (stmt != null) {
    		try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error("close statement error: ", e);
			}
    	}
    }
    return false;
  }

  @Override
  public boolean deleteOrder(String orderId) {
	  Statement stmt = null;
    String sql = "DELETE FROM " + tableName + " WHERE orderId='" + orderId + "'";
    try {
       stmt = con.createStatement();
      stmt.executeUpdate(sql);
      return true;
    } catch (SQLException e) {
      LOGGER.error("deleteOrder  error: ", e);
    }finally{
    	if (stmt != null) {
    		try {
				stmt.close();
			} catch (SQLException e) {
				 LOGGER.error("close statement error: ", e);
			}
    	}
    }
    return false;
  }

  @Override
  public FlightOrder[] queryOrders(String userId) {
	  Statement stmt = null;
    String sql = "SELECT * FROM " + tableName + " WHERE userId='" + userId + "'";
    try {
       stmt = con.createStatement();
      ResultSet re = stmt.executeQuery(sql);
      List<?> reList = populate(re, FlightOrder.class);
      if (null == reList || reList.isEmpty()) {
        return new FlightOrder[] {};
      }
      return (FlightOrder[]) reList.toArray(new FlightOrder[] {new FlightOrder()});
    } catch (SQLException e) {
      LOGGER.error("queryOrders  error: ", e);
    } catch (InstantiationException e) {
      LOGGER.error("queryOrders  error: ", e);
    } catch (IllegalAccessException e) {
      LOGGER.error("queryOrders  error: ", e);
    }finally{
    	if (stmt != null) {
    		try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error("close statement error", e);
			}
    	}
    }
    return new FlightOrder[] {};
  }
}
