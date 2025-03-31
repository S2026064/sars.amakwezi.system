/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2028398
 */
@Service
public class UploadMultipleNomineesService implements UploadMultipleNomineesServiceLocal {

    private Map<String, Employee> usersHashMap;
    private Employee employee;

    @Autowired
    private EmployeeInformationService employeeInformationService;
    @Autowired
    private EmployeeServiceLocal employeeRepository;

    @Override
    public List<Employee> findAllEmployeesByEmployeeSid(InputStream inputStream, String employeeSid) {
        usersHashMap = new HashMap<>();
        List<Employee> employees = new ArrayList<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns oe cells
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                    if (cell.getRowIndex() != 0) {

                        if (cell.getColumnIndex() == 0 && cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                            if (StringUtils.isNotBlank(cell.getStringCellValue().trim())) {
                                System.out.println("Employee level column Index :" + cell.getColumnIndex());
                                employee = employeeRepository.findByEmployeeSidOrPersonnelNum(cell.getStringCellValue().trim());
                                if (employee == null) {
                                    if (cell.getStringCellValue().trim().startsWith("s") || cell.getStringCellValue().trim().startsWith("S")) {
                                        employee = employeeInformationService.getEmployeeBySid(cell.getStringCellValue().trim(), employeeSid);
                                    } else if (cell.getStringCellValue().trim().startsWith("0")) {
                                        employee = employeeInformationService.getEmployeeBySid("000"+cell.getStringCellValue().trim(), employeeSid);
                                    } else {
                                        employee = employeeInformationService.getEmployeeBySid(cell.getStringCellValue().trim(), employeeSid);
                                    }
                                    if (employee != null) {
                                        employee.setEmployeeSid(cell.getStringCellValue().trim());
                                        employee.setCreatedDate(new Date());
                                        employee.setCreatedBy(employeeSid);
                                    } else {
                                        System.out.println("The Employee with S-ID of " + cell.getStringCellValue().trim() + " does not exist");
                                    }
                                }
                            }
                        }
                    }
                    //if that employee already exist on the list remove the existing employee and insert new one
                    if (employee != null && StringUtils.isNotEmpty(employee.getEmployeeSid())) {
                        if (usersHashMap.containsKey(employee.getEmployeeSid())) {
                            usersHashMap.replace(employee.getEmployeeSid(), employee);
                            System.out.println("Inside replace part :" + employee.getEmployeeSid());
                        } else {
                            // if the imployee dont exist at all on the list insert that employee
                            usersHashMap.put(employee.getEmployeeSid(), employee);
                            System.out.println("Inside put part :" + employee.getEmployeeSid());
                        }
                    }
                }
            }
            usersHashMap.entrySet().forEach((entry) -> {
                employees.add(entry.getValue());
            });

            usersHashMap.clear();
            return employees;
        } catch (IOException | NumberFormatException e) {
            Logger.getLogger(UploadMultipleNomineesService.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                //file.close();
            } catch (Exception ex) {
                Logger.getLogger(UploadMultipleNomineesService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
