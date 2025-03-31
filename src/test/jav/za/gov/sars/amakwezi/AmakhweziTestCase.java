package za.gov.sars.amakhwezi.tests;

import java.util.Date;

import java.util.List;

import org.junit.After;

import org.junit.AfterClass;

import org.junit.Before;

import org.junit.BeforeClass;

import org.junit.FixMethodOrder;

import org.junit.Ignore;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.junit.runners.MethodSorters;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.test.context.ContextConfiguration;

import org.springframework.test.context.junit4.SpringRunner;

import za.gov.sars.amakhwezi.common.CostCentre;

import za.gov.sars.amakhwezi.common.NominationType;

import za.gov.sars.amakhwezi.common.UserStatus;

import za.gov.sars.amakhwezi.config.TestDataSourceConfiguration;

import za.gov.sars.amakhwezi.domain.Category;

import za.gov.sars.amakhwezi.domain.Contribution;

import za.gov.sars.amakhwezi.domain.Employee;

import za.gov.sars.amakhwezi.domain.User;

import za.gov.sars.amakhwezi.domain.UserRole;

import za.gov.sars.amakhwezi.service.CategoryServiceLocal;

import za.gov.sars.amakhwezi.service.ContributionServiceLocal;

import za.gov.sars.amakhwezi.service.CostCentreServiceLocal;

import za.gov.sars.amakhwezi.service.EmployeeInformationServiceLocal;

import za.gov.sars.amakhwezi.service.EmployeeServiceLocal;

import za.gov.sars.amakhwezi.service.ManagerInformationServiceLocal;

import za.gov.sars.amakhwezi.service.RejectionReasonServiceLocal;

import za.gov.sars.amakhwezi.service.UserRoleServiceLocal;

import za.gov.sars.amakhwezi.service.UserServiceLocal;

/**
 *
 *
 *
 * @author S2026987
 *
 */
@EnableJpaAuditing

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

@RunWith(SpringRunner.class)

@ContextConfiguration(classes = TestDataSourceConfiguration.class)

public class AmakhweziTestCase {

    @Autowired

    private EmployeeServiceLocal employeeService;

    @Autowired

    private UserRoleServiceLocal userRoleService;

    @Autowired

    private UserServiceLocal userService;

    @Autowired

    private CategoryServiceLocal categoryService;

    @Autowired

    private ContributionServiceLocal contributionService;

    @Autowired

    private RejectionReasonServiceLocal rejectionReasonService;

    @Autowired

    private ManagerInformationServiceLocal managerInformationService;

    @Autowired

    private EmployeeInformationServiceLocal employeeInformationService;

    @Autowired

    private CostCentreServiceLocal costCentreService;

    public AmakhweziTestCase() {

    }

    @BeforeClass

    public static void setUpClass() {

    }

    @AfterClass

    public static void tearDownClass() {

    }

    @Before

    public void setUp() {

    }

    @After

    public void tearDown() {

    }

    @Test

    public void testScenarioA() {

        UserRole adminRole = BootStrapHelper.getUserRole("Admin");

        userRoleService.save(adminRole);

        UserRole lineManager = BootStrapHelper.getUserRole("Line Manager");

        userRoleService.save(lineManager);

        UserRole costCentreManager = BootStrapHelper.getUserRole("Cost Centre Manager");

        userRoleService.save(costCentreManager);

        UserRole costCentreFinanceManager = BootStrapHelper.getUserRole("Line Cost Centre and Finance Manager");

        userRoleService.save(costCentreFinanceManager);

        UserRole financeManager = BootStrapHelper.getUserRole("Finance Manager");

        userRoleService.save(financeManager);

        UserRole lineCostManager = BootStrapHelper.getUserRole("Line and Cost Centre Manager");

        userRoleService.save(lineCostManager);

        UserRole lineFinanceManager = BootStrapHelper.getUserRole("Line and Finance manager");

        userRoleService.save(lineFinanceManager);

        UserRole lineSuperUser = BootStrapHelper.getUserRole("Line and Super User");

        userRoleService.save(lineSuperUser);

    }

    @Test

    public void testScenarioB() {

        Category category = BootStrapHelper.getCategory("Initiative and Innovation");

        categoryService.save(category);

    }

    //  @Ignore
    @Test

    public void testScenarioC() {

        Contribution contribution = BootStrapHelper.getContribution("Phenomenal Contribution", 1000.00, NominationType.INDIVIDUAL);

        contributionService.save(contribution);

        Contribution contribution1 = BootStrapHelper.getContribution("Phenomenal Contribution", 1000.00, NominationType.TEAM);
        contributionService.save(contribution1);

    }

    @Test

    public void testScenarioD() {

        List<Employee> employees = managerInformationService.getAmakhweziManagers();

        for (Employee employee : employees) {

//            if (!(employeeService.isExist(employee))) {
            employeeService.save(employee);

            User user = new User();

            user.setEmployee(employee);

            user.setUserStatus(UserStatus.ACTIVE);

            user.setCreatedBy("Vongani");

            user.setCreatedDate(new Date());

//                if (employee.getNumberOfDirectReports() != null) {
            user.setUserRole(userRoleService.findByDescription("Line Manager"));

//                }
            userService.save(user);

//            }
        }

        List<CostCentre> costCentres = costCentreService.findAllCostCentres();

        for (CostCentre costCentre : costCentres) {

            processEmployeeRoles(costCentre);

        }

    }

    private void processEmployeeRoles(CostCentre costCentre) {

        if (costCentre.getCostCentreMgrSID() != null) {

            User lineAndCostCentreManager = userService.findByEmployeeSid(costCentre.getCostCentreMgrSID());

            if (lineAndCostCentreManager != null) {

                lineAndCostCentreManager.setUserStatus(UserStatus.ACTIVE);

                lineAndCostCentreManager.setUserRole(userRoleService.findByDescription("Line and Cost Centre Manager"));

                lineAndCostCentreManager.setUpdatedBy("Vongani");

                lineAndCostCentreManager.setUpdatedDate(new Date());

                userService.update(lineAndCostCentreManager);

            }

            if (costCentre.getCostCentreFinMngSID() != null) {

                User lineAndFinanceManager = userService.findByEmployeeSid(costCentre.getCostCentreFinMngSID());

                if (lineAndFinanceManager != null) {

                    lineAndFinanceManager.setUserStatus(UserStatus.ACTIVE);

                    lineAndFinanceManager.setUserRole(userRoleService.findByDescription("Line and Finance Manager"));

                    lineAndFinanceManager.setUpdatedBy("Vongani");

                    lineAndFinanceManager.setUpdatedDate(new Date());

                    userService.update(lineAndFinanceManager);

                }

            }

            if (costCentre.getCostCentreFinMngSID().equalsIgnoreCase(costCentre.getCostCentreMgrSID())) {

                User lineCostCentreFinanceManager = userService.findByEmployeeSid(costCentre.getCostCentreFinMngSID());

                if (lineCostCentreFinanceManager != null) {

                    lineCostCentreFinanceManager.setUserStatus(UserStatus.ACTIVE);

                    lineCostCentreFinanceManager.setUserRole(userRoleService.findByDescription("Line Cost Centre and Finance Manager"));

                    lineCostCentreFinanceManager.setUpdatedBy("Vongani");

                    lineCostCentreFinanceManager.setUpdatedDate(new Date());

                    userService.update(lineCostCentreFinanceManager);

                }

            }

        }

    }

//    @Ignore
    @Test

    public void testScenarioL() {

        List<Employee> persistedemployees = employeeService.listAll();

        for (Employee employee1 : persistedemployees) {

            Employee costCentreM = employeeService.findByEmployeeSid(employee1.getEmpDetails().getCostCentreManagerSid());

            if (costCentreM != null) {

                if (!(employeeService.isExist(costCentreM))) {

                    employeeService.save(costCentreM);

                    User user = new User();

                    user.setEmployee(costCentreM);

                    user.setUserStatus(UserStatus.ACTIVE);

                    user.setCreatedBy("Vongani");

                    user.setCreatedDate(new Date());

//                if (employee.getNumberOfDirectReports() != null) {
                    user.setUserRole(userRoleService.findByDescription("Cost Centre Manager"));

//                }
                    userService.save(user);

                }

            }

        }

        for (Employee employee2 : persistedemployees) {

            Employee financeM = employeeService.findByEmployeeSid(employee2.getEmpDetails().getFinanceManagerSid());

            if (financeM != null) {

                if (!(employeeService.isExist(financeM))) {

                    employeeService.save(financeM);

                    User user = new User();

                    user.setEmployee(financeM);

                    user.setUserStatus(UserStatus.ACTIVE);

                    user.setCreatedBy("Vongani");

                    user.setCreatedDate(new Date());

//                if (employee.getNumberOfDirectReports() != null) {
                    user.setUserRole(userRoleService.findByDescription("Finance Manager"));

//                }
                    userService.save(user);

                }

            }

        }

//        List<CostCentre> costCentres = costCentreService.findAllCostCentres();
//
//        for (CostCentre costCentre : costCentres) {
//            if (costCentre.getCostCentreMgrSID() != null) {
//                User user = userService.findByEmployeeSid(costCentre.getCostCentreMgrSID());
//
//                if (user != null) {
//
//                    user.setUserStatus(UserStatus.ACTIVE);
//                    user.setUserRole(userRoleService.findByDescription("Line and Cost Centre Manager"));
//                    user.setUpdatedBy("Vongani");
//                    user.setUpdatedDate(new Date());
//                    userService.update(user);
//                }
//            }
//        }
//        for (CostCentre costCentre1 : costCentres) {
//            if (costCentre1.getCostCentreFinMngSID() != null) {
//                User user2 = userService.findByEmployeeSid(costCentre1.getCostCentreFinMngSID());
//                // Employee employee2 = employeeService.findBySid(costCentre1.getCostCentreFinMngSID());
//                if (user2 != null) {
//
//                    user2.setUserStatus(UserStatus.ACTIVE);
//                    user2.setUserRole(userRoleService.findByDescription("Line and Finance Manager"));
//                    user2.setUpdatedBy("Vongani");
//                    user2.setUpdatedDate(new Date());
//                    userService.update(user2);
//                }
//            }
//        }
//        for (CostCentre costCentre2 : costCentres) {
//            if (costCentre2.getCostCentreFinMngSID().equalsIgnoreCase(costCentre2.getCostCentreMgrSID())) {
//                User user3 = userService.findByEmployeeSid(costCentre2.getCostCentreFinMngSID());
//
//                if (user3 != null) {
//
//                    user3.setUserStatus(UserStatus.ACTIVE);
//                    user3.setUserRole(userRoleService.findByDescription("Line Cost Centre and Finance Manager"));
//                    user3.setUpdatedBy("Vongani");
//                    user3.setUpdatedDate(new Date());
//                    userService.update(user3);
//                }
//            }
//        }
    }

}
