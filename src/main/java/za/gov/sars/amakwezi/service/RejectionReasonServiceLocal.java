/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.domain.RejectionReason;

/**
 *
 * @author S2028389
 */
public interface RejectionReasonServiceLocal {

    RejectionReason save(RejectionReason rejectionReason);

    RejectionReason findById(Long id);

    RejectionReason update(RejectionReason rejectionReason);

    RejectionReason deleteById(Long id);

    List<RejectionReason> listAll();

    boolean isExist(RejectionReason rejectionReason);
}
