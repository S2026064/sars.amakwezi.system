/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.persistence.ContributionRepository;

/**
 *
 * @author S2028389
 */
@Service
@Transactional
public class ContributionService implements ContributionServiceLocal {

    @Autowired
    private ContributionRepository contributionRepository;

    @Override
    public Contribution save(Contribution contribution) {
        return contributionRepository.save(contribution);
    }

    @Override
    public Contribution findById(Long id) {
        return contributionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }

    @Override
    public Contribution update(Contribution contribution) {
        return contributionRepository.save(contribution);
    }

    @Override
    public Contribution deleteById(Long id) {
        Contribution contribution = findById(id);
        if (contribution != null) {
            contributionRepository.delete(contribution);
        }
        return contribution;
    }

    @Override
    public List<Contribution> listAll() {
        return contributionRepository.findAll();
    }

    @Override
    public boolean isExist(Contribution contribution) {
        return contributionRepository.findById(contribution.getId()) != null;
    }

    @Override
    public List<Contribution> findByNominationType(NominationType nominationType) {
        return contributionRepository.findByNominationType(nominationType);
    }

}
