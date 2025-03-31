/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb.util;

import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.mb.BaseBean;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;

/**
 *
 * @author S2026987
 */
@ManagedBean
@RequestScoped
public class UniqueReferenceGenerator extends BaseBean{

    @Autowired
    private NominationServiceLocal nominationService;

    private Long referenceNumber;

    public Long generateNumber() {
        Nomination nomination = nominationService.findLastInsertedNominationRecord();
        if(nomination != null){
            referenceNumber = nomination.getReferenceId() + 1L;
        }else {
            referenceNumber = 100000001L;
        }
        return referenceNumber;
    }

    public Long getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(Long referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

}
