/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Nomination;

/**
 *
 * @author S2026987
 */
@Repository
public interface EmpNominationRepository extends JpaRepository<EmpNomination, Long>{
    
    @Query("SELECT e FROM EmpNomination e WHERE e.nomination.category.description=:category AND e.employee.employeeSid=:sid AND (e.nomination.nominationStatus=:submittedStatus OR e.nomination.nominationStatus=:approvedStatus)")
    List<EmpNomination> findByCategoryAndEmployeeSid(@Param("category") String category, @Param("sid") String sid, @Param("submittedStatus") NominationStatus submittedStatus, @Param("approvedStatus") NominationStatus approvedStatus);
    
    List<EmpNomination> findByNomination(Nomination nomination);
    
    //@Modifying(flushAutomatically = true)
    @Modifying
    @Query("DELETE FROM EmpNomination e WHERE e.id=:id")
    void deleteEmpNomination(@Param("id") Long id);
}
