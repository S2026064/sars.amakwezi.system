/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.io.InputStream;
import java.util.List;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2028398
 */
public interface UploadMultipleNomineesServiceLocal {
   List<Employee> findAllEmployeesByEmployeeSid(InputStream inputStream, String employeeSid); 
}
