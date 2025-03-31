/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026987
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Employee findByEmployeeSid(String employeeSid);

    @Query("SELECT e FROM Employee e WHERE e.employeeSid=:searchParameter OR e.empDetails.personnelNum=:searchParameter")
    Employee findByEmployeeSidOrPersonnelNum(@Param("searchParameter") String searchParameter);

    @Query("SELECT e FROM Employee e WHERE e.numberOfDirectReports IS NOT null AND (e.employeeSid LIKE %:employeeSid% OR e.empDetails.fullnames LIKE %:fullname%)")
    List<Employee> searchManagersBySidOrFirstnameOrLastname(@Param("employeeSid") String employeeSid, @Param("fullname") String fullname);

    @Query("SELECT a FROM Employee a LEFT JOIN EmployeeDetails b on a.employeeSid=b.costCentreManagerSid")
    List<Employee> findCostCentreManagers(String employeeSid);

    @Query("SELECT a FROM Employee a LEFT JOIN EmployeeDetails b on a.employeeSid=b.managerSid")
    Employee findEmployeeByManagerSid(String employeeSid);

    @Query("SELECT a FROM Employee a LEFT JOIN EmployeeDetails b on a.employeeSid=b.financeManagerSid")
    List<Employee> findFinanceManagers(String employeeSid);

    @Query("SELECT a FROM Employee a LEFT JOIN EmployeeDetails b on a.employeeSid=b.financeManagerSid AND a.employeeSid=b.costCentreManagerSid")
    List<Employee> findCostAndFinanceManagers(String employeeSid);

    @Query("SELECT a FROM Employee a")
    Slice<Employee> findAllEmployees(Pageable pageable);

    @Query("SELECT COUNT(*) FROM Employee a ")
    Long totalNumberOfEmployees();
}
