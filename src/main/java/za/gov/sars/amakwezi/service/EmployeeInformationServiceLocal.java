/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026987
 */
public interface EmployeeInformationServiceLocal {

    public Employee getEmployeeBySid(String sid, String userSid);
    public String getEmployeeEmailAddress(String sid);

}
