/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.EmployeeDetails;

/**
 *
 * @author S2026987
 */
@Repository
public interface EmployeeDetailsRepository  extends JpaRepository<EmployeeDetails, Long> {
     
    
    EmployeeDetails findByPersonnelNum(String personnelNum);
    @Query("SELECT e FROM EmployeeDetails e WHERE e.costCentreManagerSid=:searchParameter")
    List<EmployeeDetails> findCostCentreManager(@Param("searchParameter") String searchParameter);
    @Query("SELECT e FROM EmployeeDetails e WHERE e.financeManagerSid=:searchParameter")
    List<EmployeeDetails> findFinanceManager(@Param("searchParameter") String searchParameter);
    @Query("SELECT e FROM EmployeeDetails e WHERE e.managerSid=:searchParameter")
    EmployeeDetails findEmployeeByManagerSid(@Param("searchParameter") String managerSid);
}
