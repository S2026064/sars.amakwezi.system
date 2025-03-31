/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.domain.RejectionReason;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;
import za.gov.sars.amakhwezi.service.RejectionReasonServiceLocal;

/**
 *
 * @author S2028389
 */
@ManagedBean
@ViewScoped
public class RejectionReasonBean extends BaseBean<RejectionReason> {

    @Autowired
    private RejectionReasonServiceLocal rejectionReasonService;
    @Autowired
    private NominationServiceLocal nominationService;

    private static final Logger LOG = Logger.getLogger(CategoryBean.class.getName());

    @PostConstruct
    public void init() {
        setPanelTitleName("Rejection Reason");

        this.reset().setList(true);
        addCollections(rejectionReasonService.listAll());
    }

    public void addOrUpdate(RejectionReason rejectionReason) {
        this.reset().setAdd(true);

        if (rejectionReason != null) {
            setPanelTitleName("Update Rejection Reason");
            rejectionReason.setUpdatedBy(getActiveUser().getFullName());
            rejectionReason.setUpdatedDate(new Date());
        } else {
            setPanelTitleName("Add Rejection Reason");
            rejectionReason = new RejectionReason();
            rejectionReason.setCreatedBy(getActiveUser().getLoggedOnUserFullName());
            rejectionReason.setCreatedDate(new Date());
            addCollection(rejectionReason);
        }
        addEntity(rejectionReason);
    }

    public void save(RejectionReason rej) {
        try {
            if (rej.getId() != null) {
                rejectionReasonService.update(rej);
            } else {
                rejectionReasonService.save(rej);
            }
            reset().setList(true);
            addInformationMessage("Rejection Reason saved successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Rejection Reasons");
    }

    public void delete(RejectionReason rej) {
        if (nominationService.isRejectionReasonUsed(rej)) {
            addWarningMessage("This Rejection Reason cannot be deleted because it is being used by other Nominations");
            return;
        }
        try {
            rejectionReasonService.deleteById(rej.getId());
            synchronize(rej);
            addInformationMessage("Rejection Reason successfully deleted");
            reset().setList(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Rejection Reasons");
    }

    public void synchronize(RejectionReason rej) {
        Iterator<RejectionReason> rejectionReasonList = getCollections().iterator();
        while (rejectionReasonList.hasNext()) {
            if (rejectionReasonList.next().getId().equals(rej.getId())) {
                rejectionReasonList.remove();
            }
        }
    }

    public void cancel(RejectionReason rej) {
        if (rej.getId() == null) {
            if (getCollections().contains(rej)) {
                getCollections().remove(rej);
            }
        }
        this.reset().setList(true);
        setPanelTitleName("Rejection Reasons");
    }

}
