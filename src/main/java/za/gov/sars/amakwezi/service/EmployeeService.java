/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.persistence.EmployeeRepository;

/**
 *
 * @author S2026987
 */
@Service
@Transactional
public class EmployeeService implements EmployeeServiceLocal {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Employee findByEmployeeSid(String sid) {
        return employeeRepository.findByEmployeeSid(sid);
    }

    @Override
    public Employee findById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }

    @Override
    public Employee update(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Employee deleteById(String id) {
        Employee employee = findById(id);
        if (employee != null) {
            employeeRepository.delete(employee);
        }
        return employee;
    }

    @Override
    public List<Employee> listAll() {
        return employeeRepository.findAll();
    }

    @Override
    public boolean isExist(Employee employee) {
        return employeeRepository.findByEmployeeSid(employee.getEmployeeSid()) != null;
    }

    @Override
    public List<Employee> searchManagersBySidOrFirstnameOrLastname(String searchParam) {
        return employeeRepository.searchManagersBySidOrFirstnameOrLastname(searchParam, searchParam);
    }

   

    @Override
    public Employee findByEmployeeSidOrPersonnelNum(String searchParameter) {
        return employeeRepository.findByEmployeeSidOrPersonnelNum(searchParameter);
    }

    @Override
    public List<Employee> findCostCentreManagers(String employeeSid) {
         return employeeRepository.findCostCentreManagers(employeeSid);
    }

    @Override
    public  List<Employee> findFinanceManagers(String employeeSid) {
         return employeeRepository.findFinanceManagers(employeeSid);
    }

    @Override
    public  List<Employee> findCostAndFinanceManagers(String employeeSid) {
       return employeeRepository.findCostAndFinanceManagers(employeeSid);
    }

    @Override
    public Employee findEmployeeByManagerSid(String employeeSid) {
        return employeeRepository.findEmployeeByManagerSid(employeeSid);
    }

    @Override
    public Slice<Employee> findAllEmployees(Pageable pageable) {
      return employeeRepository.findAllEmployees(pageable);
    }

    @Override
    public Long totalNumberOfEmployees() {
       return employeeRepository.totalNumberOfEmployees();
    }

}
