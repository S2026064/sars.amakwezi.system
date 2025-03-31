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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.Tracker;

/**
 *
 * @author S2026987
 */
@Configuration
@EnableScheduling
public class EmployeInformationUpdateSchedularService {

    @Autowired
    private EmployeeServiceLocal employeeService;

    @Autowired
    private EmployeeInformationServiceLocal employeeInformationService;

    @Autowired
    private TrackerServiceLocal trackerService;

    @Scheduled(cron = "0 0 0 * * SUN-SAT")
    public void updateEmployeeInformation() {

        Integer employeeCounter = 0;

        trackerService.save(new Tracker(new Date(), null, 0, 0));
        List<Employee> retrievedEmployees = employeeService.listAll();

        Pageable pageable = PageRequest.of(0, 100);
        Slice<Employee> slice = employeeService.findAllEmployees(pageable);
        for (Employee retrievedEmployee : slice.toList()) {
            Employee newEmployee = employeeInformationService.getEmployeeBySid(retrievedEmployee.getEmployeeSid(), "Andile check");

            if (newEmployee != null) {
                employeeCounter++;
                newEmployee.setId(retrievedEmployee.getId());
                newEmployee.getEmpDetails().setId(retrievedEmployee.getEmpDetails().getId());
                newEmployee.setUpdatedBy("Scheduler");
                newEmployee.setUpdatedDate(new Date());
                retrievedEmployee.setLoggedOnUserFullName("Scheduler");
                
                employeeService.update(newEmployee);

            }
        }
        while (slice.hasNext()) {
            slice = employeeService.findAllEmployees(slice.nextPageable());
            for (Employee retrievedEmployee : slice.toList()) {
                Employee newEmployee = employeeInformationService.getEmployeeBySid(retrievedEmployee.getEmployeeSid(), "Scheduler");

                if (newEmployee != null) {
                    employeeCounter++;
                    newEmployee.setId(retrievedEmployee.getId());
                    newEmployee.getEmpDetails().setId(retrievedEmployee.getEmpDetails().getId());
                    newEmployee.setUpdatedBy("Scheduler");
                    newEmployee.setUpdatedDate(new Date());
                    employeeService.update(newEmployee);

                }
            }
        }
        trackerService.save(new Tracker(null, new Date(), retrievedEmployees.size(), employeeCounter));
        System.out.println("Starting Manager Schedular");

    }

}
