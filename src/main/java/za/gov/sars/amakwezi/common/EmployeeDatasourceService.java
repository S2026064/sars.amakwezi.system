/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.common;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author S2026987
 */
public class EmployeeDatasourceService implements DatasourceService {
    
//    
//           @Override   
//    //locally we use hard coding connection 
//    public Connection getDatasourceConnection() {
//        try {
//            HikariDataSource dataSource = new HikariDataSource();
//            dataSource.setInitializationFailTimeout(0);
//            dataSource.setMaximumPoolSize(10);
//            dataSource.setDataSourceClassName("com.microsoft.sqlserver.jdbc.SQLServerDataSource");
//            dataSource.addDataSourceProperty("url", "jdbc:sqlserver://ptaqasqc08sql:1433;databaseName=Assets");  //PTAQASQC08SQL      
//            dataSource.addDataSourceProperty("user", "assets_user");
//            dataSource.addDataSourceProperty("password", "P@$$");
//            dataSource.setConnectionTimeout(3000);
//            return dataSource.getConnection();
//        } catch (SQLException ex) {
//            Logger.getLogger(DatasourceUtility.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }

    
    
//    @Override
//    //locally we use hard coding connection
//    public Connection getDatasourceConnection() {
//        try {
//            InitialContext initialContext = new InitialContext();
//          //  DataSource dataSource = (javax.sql.DataSource) initialContext.lookup("jdbc/users");
//            DataSource dataSource = (javax.sql.DataSource) initialContext.lookup("java:/users");
//            return dataSource.getConnection();
//        } catch (NamingException | SQLException ex) {
//            Logger.getLogger(EmployeeDatasourceService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
 //    when deploy to WAS we use this connection
    @Override
    public Connection getDatasourceConnection() {
        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (javax.sql.DataSource) initialContext.lookup("jdbc/users");
            //DataSource dataSource = (javax.sql.DataSource) initialContext.lookup("java:/users");
            return dataSource.getConnection();
        } catch (NamingException | SQLException e) {
            Logger.getLogger(EmployeeDatasourceService.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

}
