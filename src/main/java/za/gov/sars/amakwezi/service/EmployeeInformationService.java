/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.gov.sars.amakhwezi.common.ConnectionType;
import za.gov.sars.amakhwezi.common.DatasourceFactory;
import za.gov.sars.amakhwezi.common.DatasourceService;
import za.gov.sars.amakhwezi.common.EmployeeType;

import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.EmployeeDetails;

/**
 *
 * @author S2026987
 */
@Service
public class EmployeeInformationService implements EmployeeInformationServiceLocal {

    @Autowired
    private LightweightDirectoryAccessProtocolService lightweightDirectoryAccessProtocolService;

    @Autowired
    private ActingManagerInformationServiceLocal actingManagerInformationService;

    @Autowired
    private UserServiceLocal userService;

    @Override
    public Employee getEmployeeBySid(String sid, String userSid) {
        Employee employee = null;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DatasourceService service = DatasourceFactory.getDatabase(ConnectionType.EMPLOYEE_DATABASE);
            connection = service.getDatasourceConnection();
            stmt = connection.prepareStatement("{call  dbo.GetEmployeeBySID_Amakhwezi(?)}");
            stmt.setString(1, sid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("costCentreNumber") != null) {
                    employee = new Employee();
                    employee.setCreatedBy(userSid);
                    employee.setCreatedDate(new Date());
                    employee.setEmployeeSid(rs.getString("SID"));
                    employee.setNumberOfDirectReports(rs.getString("NumberOfDirectReports"));

                    employee.setEmpDetails(new EmployeeDetails());

                    employee.getEmpDetails().setCreatedBy(userSid);
                    employee.getEmpDetails().setCreatedDate(new Date());
                    employee.getEmpDetails().setIntials(rs.getString("Initials"));
                    employee.getEmpDetails().setLastName(rs.getString("lastName"));
                    employee.getEmpDetails().setPersonnelNum(rs.getString("employeeNumber"));
                    employee.getEmpDetails().setCostCenterNumber(rs.getString("costCentreNumber"));
                    employee.getEmpDetails().setCostCenterName(rs.getString("CostCentreName"));
                    employee.getEmpDetails().setFullnames(rs.getString("fullName"));

                    employee.getEmpDetails().setOrgKey(rs.getString("Province"));
                    employee.getEmpDetails().setOrgKeyName(rs.getString("OrgKeyName"));
                    employee.getEmpDetails().setPositionNumber(rs.getString("positionNumber"));
                    employee.getEmpDetails().setEmailAddress(lightweightDirectoryAccessProtocolService.getUserEmailAddress(employee.getEmployeeSid()));
                  //  employee.getEmpDetails().setEmailAddress(rs.getString("emailAddress"));
                    employee.setEmployeeType(EmployeeType.NON_SYSTEM_USER_EMPLOYEE);
                    employee.getEmpDetails().setManagerEmployeeNumber(rs.getString("managerID"));
                    employee.getEmpDetails().setManagerName(rs.getString("managerName"));
                    employee.getEmpDetails().setManagerSid(rs.getString("managerSID"));

                    employee.getEmpDetails().setManagerEmailAddress(lightweightDirectoryAccessProtocolService.getUserEmailAddress(employee.getEmpDetails().getManagerSid()));
                    employee.getEmpDetails().setPositionNumber(rs.getString("positionNumber"));
                    employee.getEmpDetails().setRegionName(rs.getString("RegionName"));
                    employee.getEmpDetails().setRegion(rs.getString("Region"));
                    employee.getEmpDetails().setOrgUnitId(rs.getString("OrgUnit"));
                    employee.getEmpDetails().setOrgUnitName(rs.getString("OrgUnitName"));
                    employee.getEmpDetails().setCostCentreManagerSid(rs.getString("CostCentreMngSID"));
                    employee.getEmpDetails().setCostCentreManagerFullnames(rs.getString("CostCentreMng"));
                    employee.getEmpDetails().setFinanceManagerSid(rs.getString("CostCentreFinMngSID"));
                    employee.getEmpDetails().setFinanceManagerFullnames(rs.getString("CostCentreFinMng"));
                    employee.getEmpDetails().setCostCentreManagerEmployeeNumber(rs.getString("CostCentreMgrEmpNum"));
                    employee.getEmpDetails().setFinanceManagerEmployeeNumber(rs.getString("CostCentreFinMgrEmpNum"));

                    employee.setSubDivision(rs.getString("SubDivision"));
                    employee.setDivisionName(rs.getString("devisionName"));

                    if (employee.getEmpDetails().getManagerSid() == null) {
                        Employee actingManager = actingManagerInformationService.getActingManagerByPositionNumber(employee.getEmpDetails().getPositionNumber());
                        if (actingManager != null) {
                            employee.getEmpDetails().setManagerSid(actingManager.getEmployeeSid());
                            employee.getEmpDetails().setManagerName(actingManager.getEmpDetails().getFullnames());
                        }
                    }

                }
            }
        } catch (SQLException e) {
            Logger.getLogger(EmployeeInformationService.class.getName() + ":" + sid).log(Level.SEVERE, null, e);
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
                Logger.getLogger(EmployeeInformationService.class.getName() + ":" + sid).log(Level.SEVERE, null, e);
            }
        }

        return employee;
    }

    @Override
    public String getEmployeeEmailAddress(String sid) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DatasourceService service = DatasourceFactory.getDatabase(ConnectionType.EMPLOYEE_DATABASE);
            connection = service.getDatasourceConnection();
            stmt = connection.prepareStatement("SELECT mail FROM [Assets].[mysap].[empaddata] WHERE empsid = ?");
            stmt.setString(1, sid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("mail") != null) {
                    return rs.getString("mail");
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(EmployeeInformationService.class.getName() + ":" + sid).log(Level.SEVERE, null, e);
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
                Logger.getLogger(EmployeeInformationService.class.getName() + ":" + sid).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }
}
