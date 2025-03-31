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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.poi.hssf.record.DrawingSelectionRecord.sid;
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
public class ManagerInformationService implements ManagerInformationServiceLocal {

    @Autowired
    private LightweightDirectoryAccessProtocolService lightweightDirectoryAccessProtocolService;
    @Autowired
    private ActingManagerInformationServiceLocal actingManagerInformationService;

    @Autowired
    private UserRoleServiceLocal userRoleService;
    private EmployeeDetails empDetails;

    @Override
    public List<Employee> getAmakhweziManagers() {
        Employee employee = null;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList();
        try {
            DatasourceService service = DatasourceFactory.getDatabase(ConnectionType.EMPLOYEE_DATABASE);
            connection = service.getDatasourceConnection();
            stmt = connection.prepareStatement("{call dbo.spGetAmakhweziManager}");

            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("EmployeeNumber") != null) {
                    employee = new Employee();
                    employee.setEmpDetails(new EmployeeDetails());
                    employee.getEmpDetails().setCreatedBy("SAP");
                    employee.getEmpDetails().setCreatedDate(new Date());
                    employee.setEmployeeSid(rs.getString("SID"));
                    employee.getEmpDetails().setIntials(rs.getString("Initials"));
                    employee.getEmpDetails().setLastName(rs.getString("lastName"));
                    employee.getEmpDetails().setPersonnelNum(rs.getString("EmployeeNumber"));
                    employee.getEmpDetails().setCostCenterNumber(rs.getString("costCentreNumber"));
                    employee.getEmpDetails().setCostCenterName(rs.getString("CostCentreName"));
                    employee.getEmpDetails().setFullnames(rs.getString("ENAME"));
                    employee.getEmpDetails().setOrgKey(rs.getString("OrgKey"));
                    employee.getEmpDetails().setPositionNumber(rs.getString("positionNumber"));
                    employee.getEmpDetails().setOrgKeyName(rs.getString("OrgKeyName"));
                    employee.getEmpDetails().setPositionNumber(rs.getString("positionNumber"));

//                    employee.getEmpDetails().setJobTittle(rs.getString("jobTitle"));
//                    employee.getEmpDetails().setEmailAddress(lightweightDirectoryAccessProtocolService.getUserEmailAddress(employee.getEmployeeSid()));
                    employee.setEmployeeType(EmployeeType.SYSTEM_USER_EMPLOYEE);
                    employee.getEmpDetails().setManagerEmployeeNumber(rs.getString("managerID"));
                    employee.getEmpDetails().setManagerName(rs.getString("managerName"));
                    employee.getEmpDetails().setManagerSid(rs.getString("managerSID"));
                    // employee.getEmpDetails().setManagerEmailAddress(lightweightDirectoryAccessProtocolService.getUserEmailAddress(employee.getEmpDetails().getManagerSid()));
                    employee.getEmpDetails().setRegionName(rs.getString("OrgKeyName"));
                    employee.getEmpDetails().setRegion(rs.getString("OrgKey"));
                    employee.getEmpDetails().setOrgUnitId(rs.getString("OrgUnit"));

                    employee.getEmpDetails().setOrgUnitName(rs.getString("OrgUnitName"));
                    employee.getEmpDetails().setCostCentreManagerSid(rs.getString("CostCentreMgrSID"));
                    employee.getEmpDetails().setCostCentreManagerFullnames(rs.getString("CostCentreMgr"));
                    employee.getEmpDetails().setFinanceManagerSid(rs.getString("CostCentreFinMngSID"));
                    employee.getEmpDetails().setFinanceManagerFullnames(rs.getString("CostCentreFinMng"));
                    employee.getEmpDetails().setCostCentreManagerEmployeeNumber(rs.getString("CostCentreMgrEmpNum"));
                    employee.getEmpDetails().setFinanceManagerEmployeeNumber(rs.getString("CostCentreFinMgrEmpNum"));
                    employee.getEmpDetails().setPersonnelArea(rs.getString("PersonnelArea"));
                    employee.getEmpDetails().setPersonnelSubArea(rs.getString("PersonnelSubArea"));

                    employee.setNumberOfDirectReports(rs.getString("NumberOfDirectReports"));
                    employee.setDivisionName(rs.getString("PersonnelArea"));
                    employee.setSubDivision(rs.getString("PersonnelSubArea"));

                    if (employee.getEmpDetails().getManagerSid() == null) {

                        Employee actingManager = actingManagerInformationService.getActingManagerByPositionNumber(employee.getEmpDetails().getPositionNumber());
                        if (actingManager != null) {
                            employee.getEmpDetails().setManagerSid(actingManager.getEmployeeSid());
                            employee.getEmpDetails().setManagerName(actingManager.getEmpDetails().getFullnames());
                        }

                    }

                    employees.add(employee);
                }

            }
        } catch (SQLException e) {
            Logger.getLogger(ManagerInformationService.class.getName()).log(Level.SEVERE, null, e);
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
        return employees;
    }

    

}
