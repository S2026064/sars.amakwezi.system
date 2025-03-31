/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.tests;

import java.util.Date;
import java.util.List;
import za.gov.sars.amakhwezi.common.EmployeeType;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.common.SarsValue;
import za.gov.sars.amakhwezi.common.UserStatus;
import za.gov.sars.amakhwezi.domain.AdministrationSettings;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.EmployeeDetails;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.NominationSettings;
import za.gov.sars.amakhwezi.domain.Permission;
import za.gov.sars.amakhwezi.domain.ReportSettings;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;

/**
 *
 * @author S2026987
 */
public class BootStrapHelper {

   
    
    public static Employee getEmployee(String employeeSid, String personnelNum, Date endDate, Date startDate, EmployeeType employeeType, String orgKey, String costCenterNumber, String positionID, String code, String sEmployeeName, String eEmployeeName){
                
        Employee employee = new Employee();
        //employee.setCreatedBy("s2026987");
        //employee.setCreatedDate(new Date());
        //employee.setLoggedOnUserFullName("Matome Moagi");
        employee.setEmployeeSid(employeeSid);

        EmployeeDetails employeeDetails = new EmployeeDetails();
        /*employeeDetails.setCreatedBy("s2026987");
        employeeDetails.setCreatedDate(new Date());
        employeeDetails.setLoggedOnUserFullName("Matome Moagi");*/
        //employeeDetails.setCode(code);
        employeeDetails.setCostCenterNumber(costCenterNumber);
        employeeDetails.setFullnames(eEmployeeName);
        employee.setEmployeeType(employeeType);
      
        employeeDetails.setOrgKey(orgKey);
        employeeDetails.setPersonnelNum(personnelNum);
        employeeDetails.setPositionNumber(positionID);
      
        employee.setEmpDetails(employeeDetails);
        
        return employee;
    }

    public static UserRole getUserRole(String description) {
        UserRole userRole = new UserRole();
        userRole.setCreatedBy("s2026987");
        userRole.setCreatedDate(new Date());
        userRole.setLoggedOnUserFullName("Matome Moagi");
        userRole.setDescription(description);

        AdministrationSettings administrationSettings = new AdministrationSettings();
        administrationSettings.setCreatedBy("s2026987");
        administrationSettings.setCreatedDate(new Date());
        administrationSettings.setLoggedOnUserFullName("Matome Moagi");
        administrationSettings.setUserRole(true);
        administrationSettings.setUsers(true);
        administrationSettings.setCategories(true);
        administrationSettings.setContributions(true);
        administrationSettings.setRejectionReasons(true);
        userRole.setAdministrationSettings(administrationSettings);

        NominationSettings nominationSettings = new NominationSettings();
        nominationSettings.setCreatedBy("s2026987");
        nominationSettings.setCreatedDate(new Date());
        nominationSettings.setLoggedOnUserFullName("Matome Moagi");
        nominationSettings.setNominate(true);
        nominationSettings.setCcReviewNomination(true);
         nominationSettings.setFinReviewNomination(true);
        nominationSettings.setEscalatedNominations(true);
        userRole.setNominationSettings(nominationSettings);

        ReportSettings reportSettings = new ReportSettings();
        reportSettings.setCreatedBy("s2026987");
        reportSettings.setCreatedDate(new Date());
        reportSettings.setLoggedOnUserFullName("Matome Moagi");
        reportSettings.setReport(true);
        userRole.setReportSettings(reportSettings);

        Permission permission = new Permission();
        permission.setCreatedBy("s2026987");
        permission.setCreatedDate(new Date());
        permission.setLoggedOnUserFullName("Matome Moagi");
        permission.setAdd(true);
        permission.setDelete(true);
        permission.setRead(true);
        permission.setUpdate(true);
        permission.setWrite(true);
        userRole.setPermission(permission);

        return userRole;
    }

    public static User getUser(UserRole userRole, UserStatus userStatus, Employee employee) {
        User user = new User();
        user.setCreatedBy("s2026987");
        user.setCreatedDate(new Date());
        user.setLoggedOnUserFullName("Matome Moagi");
        user.setUserRole(userRole);
        user.setUserStatus(userStatus);
        user.setEmployee(employee);
        return user;
    }

  
    
    
    public static Category getCategory(String description) {
        Category category = new Category();
        category.setCreatedBy("doctor");
        category.setCreatedDate(new Date());
        category.setDescription(description);
        return category;
    }

    public static Contribution getContribution(String description, double amount,NominationType nominationType) {
        Contribution contribution = new Contribution();
        contribution.setCreatedBy("doctor");
        contribution.setCreatedDate(new Date());
        contribution.setDescription(description);
        contribution.setAmount(amount);
        contribution.setNominationType(nominationType);
        return contribution;
    }

    
    public static Nomination getNomination(String motivation, List<SarsValue> values, Contribution contribution, Category category){
        Nomination nomination = new Nomination();
        nomination.setCreatedBy("s2026064");
        nomination.setCreatedDate(new Date());
        nomination.setMotivation(motivation);
        nomination.setContribution(contribution);
        nomination.setCategory(category);
        
        for(SarsValue value: values){
            nomination.addSarsValue(value);
        }
        return nomination;        
    }
    
//    public static EmployeeNomination getEmployeeNomination(NominationType nominationType, NominationStatus nominationStatus, User nominator){
//        EmployeeNomination employeeNomination = new EmployeeNomination();
//        employeeNomination.setCreatedBy("s2026064");
//        employeeNomination.setCreatedDate(new Date());
//        employeeNomination.setNominationStatus(nominationStatus);
//        employeeNomination.setNominationType(nominationType);
//        employeeNomination.setNominator(nominator);  ;
//       // employeeNomination.setNomination(nomination);
//        
//        return employeeNomination;
//        
//    }
    
//     public static RejectionReason getRejectionReason(String description) {
//        RejectionReason rejectionReason = new RejectionReason();
//        rejectionReason.setCreatedBy("doctor");
//        rejectionReason.setCreatedDate(new Date());
//        rejectionReason.setDescription(description);
//        return rejectionReason;
//    }
}
