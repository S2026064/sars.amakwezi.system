/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.domain.Nomination;

/**
 *
 * @author S2026987
 */

public class NominationRepositoryImpl implements NominationRepositoryCustom{

    @PersistenceContext(unitName = "server-persistence-unit")
    private EntityManager entityManager;

    @Override
    @Transactional
    public void refresh(Nomination nomination) {
        entityManager.refresh(entityManager.merge(nomination));
    }

    @Override
    @Transactional
    public void flush() {
        entityManager.flush();
    }

    @Override
    @Transactional
    public void updateEntity(Nomination nomination) {
        entityManager.merge(nomination);
    }

    @Override
    public void remove(Nomination nomination) {
        entityManager.remove(entityManager.merge(nomination));
    }
    
}
