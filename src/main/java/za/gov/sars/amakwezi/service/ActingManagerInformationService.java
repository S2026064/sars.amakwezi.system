/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.poi.hssf.record.DrawingSelectionRecord.sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.gov.sars.amakhwezi.common.ConnectionType;
import za.gov.sars.amakhwezi.common.DatasourceFactory;
import za.gov.sars.amakhwezi.common.DatasourceService;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.EmployeeDetails;

/**
 *
 * @author S2026080
 */
@Service
public class ActingManagerInformationService implements ActingManagerInformationServiceLocal {

    @Autowired
    private LightweightDirectoryAccessProtocolService lightweightDirectoryAccessProtocolService;
    @Autowired
    private UserServiceLocal userService;

    @Override
    public Employee getActingManagerByPositionNumber(String positionNumber) {
        Employee employee = null;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
       
        try {
            DatasourceService service = DatasourceFactory.getDatabase(ConnectionType.EMPLOYEE_DATABASE);
            connection = service.getDatasourceConnection();
            stmt = connection.prepareStatement("{call  dbo.GetCascadingManager(?)}");
              stmt.setString(1, positionNumber);
            rs = stmt.executeQuery();
         while (rs.next()) {
                if (rs.getString("SID") != null) {
                    employee = new Employee();
                    employee.setEmpDetails(new EmployeeDetails());
//                     System.out.println("Acting manager Service SID = " + rs.getString("SID"));
                    employee.getEmpDetails().setFullnames(rs.getString("fullName"));
                    employee.setEmployeeSid(rs.getString("SID"));   
            }
         }
        } catch (SQLException e) {
            Logger.getLogger(ActingManagerInformationService.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                 Logger.getLogger(ManagerInformationService.class.getName() + ":" + sid).log(Level.SEVERE, null, e);
            }
        }

        return employee;
    }

}
