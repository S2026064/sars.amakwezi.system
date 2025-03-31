/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.domain.EmpNomination;

/**
 *
 * @author S2026987
 */
public class EmpNominationRepositoryImpl implements EmpNominationRepositoryCustom {

    @PersistenceContext(unitName = "server-persistence-unit")
    private EntityManager entityManager;

    @Override
    @Transactional
    public void refresh(EmpNomination empNomination) {
        entityManager.refresh(entityManager.merge(empNomination));
    }

    @Override
    @Transactional
    public void clear() {
        entityManager.clear();
    }

    @Override
    @Transactional
    public void remove(EmpNomination empNomination) {
        EmpNomination empNom = entityManager.merge(empNomination);
        if (entityManager.contains(empNom)) {
            entityManager.remove(empNom);
            entityManager.flush();
        }
    }

}
