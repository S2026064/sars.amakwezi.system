/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "employee_details")
@Getter
@Setter
public class EmployeeDetails extends BaseEntity {

    @Column(name = "intials")
    private String intials;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "personnel_num")
    private String personnelNum;

    @Column(name = "org_key")
    private String orgKey;

    @Column(name = "org_key_name")
    private String orgKeyName;

    @Column(name = "org_unit_id")
    private String orgUnitId;

    @Column(name = "org_unit_name")
    private String orgUnitName;

    @Column(name = "cost_center_number")
    private String costCenterNumber;

    @Column(name = "cost_center_name")
    private String costCenterName;

    @Column(name = "position_number")
    private String positionNumber;

    @Column(name = "fullnames")
    private String fullnames;
    
    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "manager_employee_num")
    private String managerEmployeeNumber;

    @Column(name = "cost_centre_manager_employee_num")
    private String costCentreManagerEmployeeNumber;

    @Column(name = "finance_manager_employee_num")
    private String financeManagerEmployeeNumber;

    @Column(name = "manager_sid")
    private String managerSid;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_email_address")
    private String managerEmailAddress;


    @Column(name = "region")
    private String region;

    @Column(name = "region_name")
    private String regionName;


    @Column(name = "cost_centre_manager_sid")
    private String costCentreManagerSid;

    @Column(name = "cost_centre_manager_fullnames")
    private String costCentreManagerFullnames;

    @Column(name = "finance_manager_sid")
    private String financeManagerSid;

    @Column(name = "finance_manager_fullnames")
    private String financeManagerFullnames;
    
    
    @Column(name = "personnel_area")
    private String personnelArea;
    
     @Column(name = "personnel_sub_area")
    private String personnelSubArea;


}
