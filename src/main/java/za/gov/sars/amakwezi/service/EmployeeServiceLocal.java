/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026987
 */
public interface EmployeeServiceLocal {

    Employee save(Employee employee);

    Employee findByEmployeeSid(String sid);

    Employee findById(String id);

    Employee update(Employee employee);

    Employee deleteById(String id);

    List<Employee> listAll();

    boolean isExist(Employee employee);

    List<Employee> searchManagersBySidOrFirstnameOrLastname(String searchParam);

    List<Employee> findCostCentreManagers(String employeeSid);

    List<Employee> findFinanceManagers(String employeeSid);

    Employee findEmployeeByManagerSid(String employeeSid);

    List<Employee> findCostAndFinanceManagers(String employeeSid);

    Employee findByEmployeeSidOrPersonnelNum(String searchParameter);

    Slice<Employee> findAllEmployees(Pageable pageable);
    
    Long totalNumberOfEmployees();
}
