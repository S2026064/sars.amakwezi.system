/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.domain.Contribution;

/**
 *
 * @author S2028389
 */
public interface ContributionServiceLocal {

    Contribution save(Contribution contribution);

    Contribution findById(Long id);

    Contribution update(Contribution contribution);

    Contribution deleteById(Long id);

    List<Contribution> listAll();

    boolean isExist(Contribution contribution);

    List<Contribution> findByNominationType(NominationType nominationType);
}
