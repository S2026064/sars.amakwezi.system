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
import za.gov.sars.amakhwezi.domain.RejectionReason;
import za.gov.sars.amakhwezi.persistence.RejectionReasonRepository;

/**
 *
 * @author S2028389
 */
@Service
@Transactional
public class RejectionReasonService implements RejectionReasonServiceLocal {

    @Autowired
    private RejectionReasonRepository rejectionReasonRepository;

    @Override
    public RejectionReason save(RejectionReason rejectionReason) {
        return rejectionReasonRepository.save(rejectionReason);
    }

    @Override
    public RejectionReason findById(Long id) {
        return rejectionReasonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }

    @Override
    public RejectionReason update(RejectionReason rejectionReason) {
        return rejectionReasonRepository.save(rejectionReason);
    }

    @Override
    public RejectionReason deleteById(Long id) {
        RejectionReason rejectionReason = findById(id);

        if (rejectionReason != null) {
            rejectionReasonRepository.delete(rejectionReason);
        }
        return rejectionReason;
    }

    @Override
    public List<RejectionReason> listAll() {
        return rejectionReasonRepository.findAll();
    }

    @Override
    public boolean isExist(RejectionReason rejectionReason) {
        return rejectionReasonRepository.findById(rejectionReason.getId()) != null;
    }

}
