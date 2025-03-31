/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.persistence.EmpNominationRepository;

/**
 *
 * @author S2026064
 */
@Service
@Transactional
public class EmpNominationService implements EmpNominationServiceLocal {

    @Autowired
    private EmpNominationRepository empNominationRepository;

    @Override
    public boolean isEmployeeNominatedForSameCategory(String category, String sid) {
        return !empNominationRepository.findByCategoryAndEmployeeSid(category, sid, NominationStatus.SUBMITTED, NominationStatus.APPROVED).isEmpty();
    }

    @Override
    public EmpNomination save(EmpNomination empNomination) {
        return empNominationRepository.save(empNomination);
    }

    @Override
    public EmpNomination findById(Long id) {
        return empNominationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }

    @Override
    public EmpNomination update(EmpNomination empNomination) {
        return empNominationRepository.save(empNomination);
    }

    @Override
    public EmpNomination deleteById(Long id) {
        EmpNomination empNomination = findById(id);
        if (empNomination != null) {
            empNominationRepository.delete(empNomination);
        }
        return empNomination;
    }

    @Override
    public List<EmpNomination> listAll() {
        return empNominationRepository.findAll();
    }

    @Override
    public boolean isExist(EmpNomination empNomination) {
        return empNominationRepository.findById(empNomination.getId()) != null;
    }

    @Override
    public void deleteEmpNomination(Long id) {
        empNominationRepository.deleteEmpNomination(id);
    }

//    @Override
//    public void refresh(EmpNomination empNomination) {
//        empNominationRepository.refresh(empNomination);
//    }
//
//    @Override
//    public void clear() {
//        empNominationRepository.clear();
//    }
//
//    @Override
//    public void remove(EmpNomination empNomination) {
//        empNominationRepository.remove(empNomination);
//    }

    @Override
    public List<EmpNomination> findByNomination(Nomination nomination) {
        return empNominationRepository.findByNomination(nomination);
    }

}
