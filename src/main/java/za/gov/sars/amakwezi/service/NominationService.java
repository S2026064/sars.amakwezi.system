/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.RejectionReason;
import za.gov.sars.amakhwezi.persistence.NominationRepository;

/**
 *
 * @author S2026064
 */
@Service
@Transactional
public class NominationService implements NominationServiceLocal {

    @Autowired
    private NominationRepository nominationRepository;

    @Override
    public Nomination save(Nomination nomination) {
        return nominationRepository.save(nomination);
    }

    @Override
    public Nomination findById(Long id) {
        return nominationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                "The requested id [" + id
                + "] does not exist."));
    }

    @Override
    public Nomination update(Nomination nomination) {
        return nominationRepository.save(nomination);
    }

    @Override
    public Nomination deleteById(Long id) {
        Nomination nomination = findById(id);
        if (nomination != null) {
            nominationRepository.delete(nomination);
        }
        return nomination;
    }

    @Override
    public Slice<Nomination> listAll(Pageable pageable) {
        return nominationRepository.findAll(pageable);
    }

    @Override
    public boolean isExist(Nomination nomination) {
        return nominationRepository.findById(nomination.getId()) != null;
    }

    @Override
    public boolean isCategoryUsed(Category category) {
        return !nominationRepository.findByCategory(category).isEmpty();
    }

    @Override
    public boolean isContributionUsed(Contribution contribution) {

        return !nominationRepository.findByContribution(contribution).isEmpty();
    }

    @Override
    public Slice<Nomination> findNominationsByUserSid(String employeeSid, Pageable pageable) {
        return nominationRepository.findNominationsByUserSid(employeeSid, pageable);
    }

    @Override
    public List<Nomination> findSubmittedNominations(String managerSid, NominationStatus nominationStatus) {
        return nominationRepository.findSubmittedNominations(managerSid, nominationStatus);
    }

    @Override
    public Slice<Nomination> findEscalatedNominations(Pageable pageable) {
        return nominationRepository.findEscalatedNominations(pageable);
    }

    @Override
    public List<Nomination> findByNominationType(String nominationType) {
        return nominationRepository.findByNominationType(nominationType);
    }

    @Override
    public List<Nomination> findRecievedNominations(NominationStatus nominationStatus, Date startDate, Date endDate) {
        return nominationRepository.findRecievedNominations(nominationStatus, startDate, endDate);
    }

    @Override
    public List<Nomination> findCapturedNominations(Date startDate, Date endDate) {
        return nominationRepository.findCapturedNominations(startDate, endDate);
    }

    @Override
    public List<Nomination> findNominationsStartDateEndDate(Date startDate, Date endDate) {
        return nominationRepository.findNominationsStartDateEndDate(startDate, endDate);
    }

    @Override
    public boolean isRejectionReasonUsed(RejectionReason rejectionReason) {
        return !nominationRepository.findByRejectionReason(rejectionReason).isEmpty();
    }

    @Override
    public List<Nomination> findByCategory(Category category) {
        return nominationRepository.findByCategory(category);
    }

    @Override
    public Nomination findLastInsertedNominationRecord() {
        return nominationRepository.findLastInsertedNominationRecord();
    }

    @Override
    public Slice<Nomination> findCostCenterApprovedNominations(String managerSid, Pageable pageable) {
        return nominationRepository.findCostCenterApprovedNominations(managerSid, pageable);
    }

    @Override
    public Slice<Nomination> findSubmittedNominationsOnCostCentre(String managerSid, Pageable pageable) {
        return nominationRepository.findSubmittedNominationsOnCostCentre(managerSid, pageable);
    }

    @Override
    public List<Nomination> findReroutedNominations(Date startDatete, Date endDate) {
        return nominationRepository.findReroutedNominations(startDatete, endDate);
    }

    @Override
    public Slice<Nomination> findReroutedSubmittedNominationsOnCostCentre(String managerSid, Pageable pageable) {
        return nominationRepository.findReroutedSubmittedNominationsOnCostCentre(managerSid, pageable);
    }

    @Override
    public Slice<Nomination> findReroutedCostCenterApprovedNominations(String managerSid, Pageable pageable) {
        return nominationRepository.findReroutedCostCenterApprovedNominations(managerSid, pageable);
    }

    @Override
    public Page<Nomination> findByNominationStatus(List<NominationStatus> nominationStatuses, Pageable pageable) {
        return nominationRepository.findByNominationStatus(nominationStatuses, pageable);
    }

    @Override
    public Slice<Nomination> findReroutedFinanceManagerApprovedAndRejectedNominations(List<NominationStatus> statuses, String managerSid, Pageable pageable) {
        return nominationRepository.findReroutedFinanceManagerApprovedAndRejectedNominations(statuses, managerSid, pageable);
    }

    @Override
    public Slice<Nomination> findReroutedCostCenterApprovedAndRejectedNominations(List<NominationStatus> statuses, String managerSid, Pageable pageable) {
        return nominationRepository.findReroutedCostCenterApprovedAndRejectedNominations(statuses, managerSid, pageable);
    }

}
