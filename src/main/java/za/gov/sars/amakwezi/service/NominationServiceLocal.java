/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.RejectionReason;

/**
 *
 * @author S2026064
 */
public interface NominationServiceLocal {

    Nomination save(Nomination nomination);

    Nomination findById(Long id);

    Nomination update(Nomination nomination);

    Nomination deleteById(Long id);

    Slice< Nomination> listAll(Pageable pageable);

    boolean isExist(Nomination nomination);

    boolean isCategoryUsed(Category category);

    boolean isContributionUsed(Contribution contribution);

    boolean isRejectionReasonUsed(RejectionReason rejectionReason);

    List<Nomination> findByCategory(Category category);

    Slice<Nomination> findNominationsByUserSid(String employeeSid, Pageable pageable);

    List<Nomination> findSubmittedNominations(String managerSid, NominationStatus nominationStatus);

    Slice<Nomination> findSubmittedNominationsOnCostCentre(String managerSid, Pageable pageable);

    Slice<Nomination> findEscalatedNominations(Pageable pageable);

    Page<Nomination> findByNominationStatus(List<NominationStatus> nominationStatuses, Pageable pageable);

    List<Nomination> findByNominationType(String nominationType);

    List<Nomination> findRecievedNominations(NominationStatus nominationStatus, Date startDate, Date endDate);

    List<Nomination> findReroutedNominations(Date startDatete, Date endDate);

    List<Nomination> findCapturedNominations(Date startDate, Date endDate);

    //  List<Nomination> findCupturedCostCentreNomination(Date startDate, Date endDate);
    List<Nomination> findNominationsStartDateEndDate(Date startDate, Date endDate);

    Nomination findLastInsertedNominationRecord();

    Slice<Nomination> findCostCenterApprovedNominations(String managerSid, Pageable pageable);

    Slice<Nomination> findReroutedSubmittedNominationsOnCostCentre(String managerSid, Pageable pageable);

    Slice<Nomination> findReroutedCostCenterApprovedNominations(String managerSid, Pageable pageable);

    Slice<Nomination> findReroutedFinanceManagerApprovedAndRejectedNominations(List<NominationStatus> statuses, String managerSid, Pageable pageable);

    Slice<Nomination> findReroutedCostCenterApprovedAndRejectedNominations(List<NominationStatus> statuses, String managerSid, Pageable pageable);

}
