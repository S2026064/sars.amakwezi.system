/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.Date;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import za.gov.sars.amakhwezi.common.CostCentre;
import za.gov.sars.amakhwezi.common.UserStatus;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.Tracker;
import za.gov.sars.amakhwezi.domain.User;

/**
 *
 * @author S2026987
 */
@Configuration
@EnableScheduling
public class DailyManagerInformationUpdateSchedularService {

    @Autowired
    private EmployeeServiceLocal employeeService;
    @Autowired
    private ManagerInformationServiceLocal managerInformationService;
    @Autowired
    private UserRoleServiceLocal userRoleService;
    @Autowired
    private UserServiceLocal userService;

    @Autowired
    private TrackerServiceLocal trackerService;

    @Autowired
    private CostCentreServiceLocal costCentreService;

    @Scheduled(cron = "0 30 0 * * SUN-SAT")
    public void updateManagerInformation() {
        int employeeCounter = 0;

        List<Employee> retrievedEmployees = managerInformationService.getAmakhweziManagers();

        trackerService.save(new Tracker(new Date(), null, retrievedEmployees.size(), employeeCounter));

        for (Employee retrievedEmployee : retrievedEmployees) {

            Employee persistentEmployee = employeeService.findByEmployeeSid(retrievedEmployee.getEmployeeSid());
            if (persistentEmployee == null) {
                retrievedEmployee.setCreatedBy("Manager Schedular on SAP");
                retrievedEmployee.setCreatedDate(new Date());
                employeeService.save(retrievedEmployee);
                User user = new User();
                user.setEmployee(retrievedEmployee);
                user.setUserStatus(UserStatus.ACTIVE);
                user.setCreatedBy("Manager Schedular on SAP");
                user.setUserRole(userRoleService.findByDescription("Line Manager"));
                user.setCreatedDate(new Date());
                userService.save(user);
            } else if (persistentEmployee != null) {
                User persistentUser = userService.findByEmployeeSid(persistentEmployee.getEmployeeSid());
                if (persistentUser != null) {

                    persistentUser.setEmployee(persistentEmployee);
                    persistentUser.setUserStatus(UserStatus.ACTIVE);
                    persistentUser.setUpdatedBy("Manager Schedular on SAP");
                    persistentUser.setUserRole(userRoleService.findByDescription("Line Manager"));
                    persistentUser.setUpdatedDate(new Date());
                    userService.update(persistentUser);
                }
            }
//                else {
//
//                    User user = new User();
//                    user.setEmployee(retrievedEmployee);
//                    user.setUserStatus(UserStatus.ACTIVE);
//                    user.setCreatedBy("Manager Schedular on SAP");
//                    user.setUserRole(userRoleService.findByDescription("Line Manager"));
//                    user.setCreatedDate(new Date());
//                    userService.save(user);
//                }
//            } else {
//
//                retrievedEmployee.setCreatedBy("Manager Schedular on SAP");
//                retrievedEmployee.setCreatedDate(new Date());
//                employeeService.save(retrievedEmployee);
//                User user = new User();
//                user.setEmployee(retrievedEmployee);
//                user.setUserStatus(UserStatus.ACTIVE);
//                user.setCreatedBy("Manager Schedular on SAP");
//                user.setUserRole(userRoleService.findByDescription("Line Manager"));
//                user.setCreatedDate(new Date());
//                userService.save(user);
//            }
            employeeCounter++;
        }

        List<CostCentre> costCentres = costCentreService.findAllCostCentres();
        for (CostCentre costCentre : costCentres) {
            processEmployeeRoles(costCentre);
        }
//        

        trackerService.save(new Tracker(null, new Date(), retrievedEmployees.size(), employeeCounter));

    }

    private void processEmployeeRoles(CostCentre costCentre) {

        if (costCentre.getCostCentreMgrSID() != null) {
            User lineAndCostCentreManager = userService.findByEmployeeSid(costCentre.getCostCentreMgrSID());
            if (lineAndCostCentreManager != null) {

                lineAndCostCentreManager.setUserStatus(UserStatus.ACTIVE);
                lineAndCostCentreManager.setUserRole(userRoleService.findByDescription("Line and Cost Centre Manager"));
                lineAndCostCentreManager.setUpdatedBy("Manager Schedular on SAP");
                lineAndCostCentreManager.setUpdatedDate(new Date());
                userService.update(lineAndCostCentreManager);
            }
            if (costCentre.getCostCentreFinMngSID() != null) {
                User lineAndFinanceManager = userService.findByEmployeeSid(costCentre.getCostCentreFinMngSID());
                if (lineAndFinanceManager != null) {

                    lineAndFinanceManager.setUserStatus(UserStatus.ACTIVE);
                    lineAndFinanceManager.setUserRole(userRoleService.findByDescription("Line and Finance Manager"));
                    lineAndFinanceManager.setUpdatedBy("Manager Schedular on SAP");
                    lineAndFinanceManager.setUpdatedDate(new Date());
                    userService.update(lineAndFinanceManager);
                }
            }
            if (costCentre.getCostCentreFinMngSID().equalsIgnoreCase(costCentre.getCostCentreMgrSID())) {
                User lineCostCentreFinanceManager = userService.findByEmployeeSid(costCentre.getCostCentreFinMngSID());

                if (lineCostCentreFinanceManager != null) {

                    lineCostCentreFinanceManager.setUserStatus(UserStatus.ACTIVE);
                    lineCostCentreFinanceManager.setUserRole(userRoleService.findByDescription("Line Cost Centre and Finance Manager"));
                    lineCostCentreFinanceManager.setUpdatedBy("Manager Schedular on SAP");
                    lineCostCentreFinanceManager.setUpdatedDate(new Date());
                    userService.update(lineCostCentreFinanceManager);
                }
            }
        }
    }

}
