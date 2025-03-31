/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.RejectionReason;

/**
 *
 * @author S2026987
 */
@Repository
public interface NominationRepository extends PagingAndSortingRepository<Nomination, Long> {

    List<Nomination> findByCategory(Category category);

    List<Nomination> findByContribution(Contribution contribution);

    List<Nomination> findByRejectionReason(RejectionReason rejectionReason);

    @Query("SELECT e FROM Nomination e WHERE e.nominator.employee.employeeSid=:employeeSid")
    Slice<Nomination> findNominationsByUserSid(@Param("employeeSid") String employeeSid, Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus <> 'DELETED' AND e.nominationStatus <> 'SAVED' AND e.nominationStatus <> 'REROUTED' AND e.costCenterManagerSid=:managerSid")
    Slice<Nomination> findSubmittedNominationsOnCostCentre(@Param("managerSid") String managerSid, Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus=:nominationStatus AND e.costCenterManagerSid=:managerSid")
    List<Nomination> findSubmittedNominations(@Param("managerSid") String managerSid, @Param("nominationStatus") NominationStatus nominationStatus);
//    

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus IN (:nominationStatus)")
    Page<Nomination> findByNominationStatus(@Param("nominationStatus") List<NominationStatus> nominationStatuses, Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.costCentreManagerStatus='APPROVED' AND e.financeManagerSid=:managerSid  AND e.nominationStatus <> 'DELETED'")
    Slice<Nomination> findCostCenterApprovedNominations(@Param("managerSid") String managerSid, Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus = 'REROUTED' AND e.reroutecostCenterManagerSid=:managerSid")
    Slice<Nomination> findReroutedSubmittedNominationsOnCostCentre(@Param("managerSid") String managerSid, Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus='FINANCE_REROUTED' AND  e.reroutefinanceManagerSid=:managerSid")
    Slice<Nomination> findReroutedCostCenterApprovedNominations(@Param("managerSid") String managerSid, Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus IN (:nominationStatus) AND  e.reroutefinanceManagerSid=:managerSid")
    Slice<Nomination> findReroutedFinanceManagerApprovedAndRejectedNominations(@Param("nominationStatus") List<NominationStatus> statuses, @Param("managerSid") String managerSid, Pageable pageable);

     @Query("SELECT e FROM Nomination e WHERE e.nominationStatus IN (:nominationStatus) AND  e.reroutecostCenterManagerSid=:managerSid")
    Slice<Nomination> findReroutedCostCenterApprovedAndRejectedNominations(@Param("nominationStatus") List<NominationStatus> statuses, @Param("managerSid") String managerSid, Pageable pageable);

    
    @Query("SELECT e FROM Nomination e WHERE (( e.nominationStatus<>'APPROVED' AND e.nominationStatus <> 'DELETED' AND e.nominationStatus <> 'REJECTED' AND e.nominationStatus<>'FINANCE_REROUTED')AND(e.costCentreManagerStatus= 'APPROVED') AND ((GETDATE()  > DATEADD(DAY, 1,e.updatedDate))) OR ( e.nominationStatus<>'APPROVED' AND e.nominationStatus <> 'DELETED' AND e.nominationStatus <> 'REJECTED' AND e.nominationStatus='FINANCE_REROUTED')AND(e.costCentreManagerStatus= 'APPROVED') AND ((GETDATE()  > DATEADD(DAY, 1,e.reroutedDate)))) OR (( e.nominationStatus<>'APPROVED' AND e.nominationStatus <> 'DELETED' AND e.nominationStatus <> 'REJECTED' AND e.nominationStatus<>'REROUTED') AND ( e.costCentreManagerStatus= 'PENDING')  AND ((GETDATE()  > DATEADD(DAY,1,e.createdDate))) OR ( e.nominationStatus<>'APPROVED' AND e.nominationStatus <> 'DELETED' AND e.nominationStatus <> 'REJECTED' AND e.nominationStatus='REROUTED') AND (e.costCentreManagerStatus= 'PENDING')  AND ((GETDATE()  > DATEADD(DAY,1,e.reroutedDate))))")
    Slice<Nomination> findEscalatedNominations(Pageable pageable);

    @Query("SELECT e FROM Nomination e WHERE e.nominationType=:nominationType")
    List<Nomination> findByNominationType(@Param("nominationType") String nominationType);

    @Query("SELECT e FROM Nomination e WHERE e.nominationStatus=:nominationStatus AND (e.updatedDate>=:startDate AND e.updatedDate<=:endDate)")
    List<Nomination> findRecievedNominations(@Param("nominationStatus") NominationStatus nominationStatus, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT e FROM Nomination e WHERE e.createdDate>=:startDate AND e.createdDate<=:endDate")
    List<Nomination> findCapturedNominations(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT e FROM Nomination e WHERE (e.createdDate>=:startDate AND e.createdDate<=:endDate)")
    List<Nomination> findNominationsStartDateEndDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT TOP (1) * FROM nomination  ORDER BY [reference_id]  DESC", nativeQuery = true)
    Nomination findLastInsertedNominationRecord();

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Nomination e SET e=:nomination WHERE e.id=:id")
    void updateNomination(@Param("nomination") Nomination nomination, @Param("id") Long id);

    @Query("SELECT e FROM Nomination e WHERE (e.reroutefinanceManagerSid IS NOT NULL OR e.reroutecostCenterManagerSid IS NOT NULL) AND (e.createdDate>=:startDate AND e.createdDate<=:endDate)")
    List<Nomination> findReroutedNominations(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
