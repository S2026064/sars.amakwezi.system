/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import za.gov.sars.amakhwezi.common.EmployeeType;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "employee")
@Getter
@Setter
public class Employee extends BaseEntity {

    @Column(name = "employee_sid")
    private String employeeSid;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "emp_details_id")
    private EmployeeDetails empDetails;

    @Column(name = "employee_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmployeeType employeeType;

    @Column(name = "numberOfdirectReports")
    private String numberOfDirectReports;

    @Column(name = "division_name")
    private String divisionName;

    @Column(name = "sub_division_name")
    private String subDivision;

    public boolean compareEmployee(Employee employee) {
        if (!Objects.equals(this.employeeSid.trim(), employee.employeeSid.trim())) {
            return false;
        }
        if (!Objects.equals(this.divisionName.trim(), employee.divisionName.trim())) {
            return false;
        }
//        if (!Objects.equals(this.numberOfDirectReports.trim(), employee.numberOfDirectReports.trim())) {
//            return false;
//        }
        if (!Objects.equals(this.subDivision.trim(), employee.subDivision.trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getCostCenterName().trim(), employee.getEmpDetails().getCostCenterName().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getCostCenterNumber().trim(), employee.getEmpDetails().getCostCenterNumber().trim())) {
            return false;
        }
//        if (!Objects.equals(this.getEmpDetails().getCostCentreManagerEmployeeNumber().trim(), employee.getEmpDetails().getCostCentreManagerEmployeeNumber().trim())) {
//            return false;
//        }
        if (!Objects.equals(this.getEmpDetails().getCostCentreManagerFullnames().trim(), employee.getEmpDetails().getCostCentreManagerFullnames().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getCostCentreManagerSid().trim(), employee.getEmpDetails().getCostCentreManagerSid().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getEmailAddress().trim(), employee.getEmpDetails().getEmailAddress().trim())) {
            return false;
        }
//        if (!Objects.equals(this.getEmpDetails().getFinanceManagerEmployeeNumber().trim(), employee.getEmpDetails().getFinanceManagerEmployeeNumber().trim())) {
//            return false;
//        }
        if (!Objects.equals(this.getEmpDetails().getFinanceManagerFullnames().trim(), employee.getEmpDetails().getFinanceManagerFullnames().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getFinanceManagerSid().trim(), employee.getEmpDetails().getFinanceManagerSid().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getFullnames().trim(), employee.getEmpDetails().getFullnames().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getIntials().trim(), employee.getEmpDetails().getIntials().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getLastName().trim(), employee.getEmpDetails().getLastName().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getManagerEmailAddress().trim(), employee.getEmpDetails().getManagerEmailAddress().trim())) {
            return false;
        }
//        if (!Objects.equals(this.getEmpDetails().getManagerEmployeeNumber().trim(), employee.getEmpDetails().getManagerEmployeeNumber().trim())) {
//            return false;
//        }
//        if (!Objects.equals(this.getEmpDetails().getManagerName().trim(), employee.getEmpDetails().getManagerName().trim())) {
//            return false;
//        }
        if (!Objects.equals(this.getEmpDetails().getManagerSid().trim(), employee.getEmpDetails().getManagerSid().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getOrgKey().trim(), employee.getEmpDetails().getOrgKey().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getOrgKeyName().trim(), employee.getEmpDetails().getOrgKeyName().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getOrgUnitId().trim(), employee.getEmpDetails().getOrgUnitId().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getOrgUnitName().trim(), employee.getEmpDetails().getOrgUnitName().trim())) {
            return false;
        }
//        if (!Objects.equals(this.getEmpDetails().getPersonnelArea().trim(), employee.getEmpDetails().getPersonnelArea().trim())) {
//            return false;
//        }
//        if (!Objects.equals(this.getEmpDetails().getPersonnelSubArea().trim(), employee.getEmpDetails().getPersonnelSubArea().trim())) {
//            return false;
//        }
        if (!Objects.equals(this.getEmpDetails().getPersonnelNum().trim(), employee.getEmpDetails().getPersonnelNum().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getPositionNumber().trim(), employee.getEmpDetails().getPositionNumber().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getRegion().trim(), employee.getEmpDetails().getRegion().trim())) {
            return false;
        }
        if (!Objects.equals(this.getEmpDetails().getRegionName().trim(), employee.getEmpDetails().getRegionName().trim())) {
            return false;
        }
        return Objects.equals(this.getEmployeeType().toString().trim(), employee.getEmployeeType().toString().trim());
    }
}
