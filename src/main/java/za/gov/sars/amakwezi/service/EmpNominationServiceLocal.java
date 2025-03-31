/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Nomination;

/**
 *
 * @author S2026064
 */
public interface EmpNominationServiceLocal {

    EmpNomination save(EmpNomination empNomination);

    EmpNomination findById(Long id);

    EmpNomination update(EmpNomination empNomination);

    EmpNomination deleteById(Long id);

    List<EmpNomination> listAll();

    boolean isExist(EmpNomination empNomination);
    
    boolean isEmployeeNominatedForSameCategory(String category, String sid);
    
    void deleteEmpNomination(Long id);
//    
//    void refresh(EmpNomination empNomination);
//    
//    void clear();
//    
//    void remove(EmpNomination empNomination);
    
    List<EmpNomination> findByNomination(Nomination nomination);
}
