/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import za.gov.sars.amakhwezi.domain.EmpNomination;

/**
 *
 * @author S2026987
 */
public interface EmpNominationRepositoryCustom {
    
    void refresh(EmpNomination empNomination);
    
    void clear();
    
    void remove(EmpNomination empNomination);
}
