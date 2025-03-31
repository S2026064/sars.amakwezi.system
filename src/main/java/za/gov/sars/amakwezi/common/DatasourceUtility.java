/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.common;

import com.zaxxer.hikari.HikariDataSource;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author vongani
 */
public class DatasourceUtility {

    public static DataSource getDatasource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setInitializationFailTimeout(0);

        dataSource.setMaximumPoolSize(10);
        dataSource.setDataSourceClassName("com.microsoft.sqlserver.jdbc.SQLServerDataSource");

//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAUS09\\SQLEXPRESS:1433;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "sarsdev");
//        dataSource.addDataSourceProperty("password", "sarsdev");
//         dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAXN73\\SQLEXPRESS14:2010;databaseName=AMAKHWEZI_DB");
//         dataSource.addDataSourceProperty("user", "terry");
//         dataSource.addDataSourceProperty("password", "P@sswords.");
////        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAUX53\\SQLEXPRESS:2010;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAUX53\\SQLEXPRESS:2010;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "Mpelwane");
//        dataSource.addDataSourceProperty("password", "Mpelwane12345");
////
//          dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAXN73\\SQLEXPRESS14:2010;databaseName=AMAKHWEZI_DB");
//          dataSource.addDataSourceProperty("user", "terry");
//          dataSource.addDataSourceProperty("password", "P@sswords.");
////
//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAXR60\\SQLE:2010;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "Jiyeza");
//        dataSource.addDataSourceProperty("password", "P@ssw0rd");
//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAXX56\\SQLEXPRESS:2010;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "tebx");
//        dataSource.addDataSourceProperty("password", "tebx1234");
//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAXR60\\SQLE:2010;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "Jiyeza");
//        dataSource.addDataSourceProperty("password", "P@ssw0rd");
        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://LPTAXX56\\SQLEXPRESS:2010;databaseName=AMAKHWEZI_DB");
        dataSource.addDataSourceProperty("user", "tebx");
        dataSource.addDataSourceProperty("password", "tebx1234");

//        dataSource.addDataSourceProperty("url","jdbc:sqlserver://PTADVSQL18:1433;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "sa");        
//        dataSource.addDataSourceProperty("password", "P@ssw0rd");//MANDB_PreProd
//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://PTAQASQC08SQL:1433;databaseName=AMAKHWEZI_DB");
//        dataSource.addDataSourceProperty("user", "amakhwezi_user");
//        dataSource.addDataSourceProperty("password", "@Kw3zi22u$@r");
//        dataSource.addDataSourceProperty("url", "jdbc:sqlserver://PTAQASQC08SQL:1433;databaseName=AMAKHWEZI_DB_Preprod");
//        dataSource.addDataSourceProperty("user", "amakhwezi_user");
//        dataSource.addDataSourceProperty("password", "@Kw3zi22u$@r");
        return dataSource;
    }

    public static DataSource getDatasourceLookup() {
        try {
            InitialContext initialContext = new InitialContext();
       //     DataSource dataSource = (DataSource) initialContext.lookup("jdbc/amakhweziDS");
              DataSource dataSource = (DataSource) initialContext.lookup("java:/amakhweziDS");
            return dataSource;
        } catch (NamingException ex) {
            Logger.getLogger(DatasourceUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
