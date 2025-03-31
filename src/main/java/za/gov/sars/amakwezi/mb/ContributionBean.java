/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.service.ContributionServiceLocal;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;

/**
 *
 * @author S2028389
 */
@ManagedBean
@ViewScoped
public class ContributionBean extends BaseBean<Contribution> {

    @Autowired
    private ContributionServiceLocal contributionService;
    @Autowired
    private NominationServiceLocal nominationService;

    private ArrayList<NominationType> nominationTypes = new ArrayList<>();

    private Contribution contribution;

    private static final Logger LOG = Logger.getLogger(ContributionBean.class.getName());

    @PostConstruct
    public void init() {
        setPanelTitleName("Contributions");
        this.reset().setList(true);
        nominationTypes.addAll(Arrays.asList(NominationType.values()));
        addCollections(contributionService.listAll());
    }

    public void addOrUpdate(Contribution contribution) {
        this.reset().setAdd(true);
        if (contribution != null) {
            setPanelTitleName("Update Contribution");
            contribution.setUpdatedBy(getActiveUser().getLoggedOnUserFullName());
            contribution.setUpdatedDate(new Date());
        } else {
            setPanelTitleName("Add Contribution");
            contribution = new Contribution();
            contribution.setCreatedBy(getActiveUser().getLoggedOnUserFullName());
            contribution.setCreatedDate(new Date());
            addCollection(contribution);
        }
        addEntity(contribution);
    }

    public void save(Contribution con) {
        try {
            if (con.getId() != null) {
                contributionService.update(con);
            } else {
                contributionService.save(con);
            }
            reset().setList(true);
            addInformationMessage("Contribution saved successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Contribution");
    }

    public void delete(Contribution con) {

        if (nominationService.isContributionUsed(con)) {
            addWarningMessage("This " + con.getDescription() + " cannot be deleted because it is being used by other Nominations");
            return;
        }
        try {
            contributionService.deleteById(con.getId());
            synchronize(con);
            addInformationMessage("Contribution successfully deleted");
            reset().setList(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Contribution");
    }

    public void synchronize(Contribution con) {
        Iterator<Contribution> conList = getCollections().iterator();
        while (conList.hasNext()) {
            if (conList.next().getId().equals(con.getId())) {
                conList.remove();
            }
        }
    }

    public void cancel(Contribution con) {
        if (con.getId() == null) {
            if (getCollections().contains(con)) {
                getCollections().remove(con);
            }
        }
        this.reset().setList(true);
        setPanelTitleName("Contributions");
    }

    public List<Contribution> getContributions() {
        return this.getCollections();
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    public ArrayList<NominationType> getNominationTypes() {
        return nominationTypes;
    }

    public void setNominationTypes(ArrayList<NominationType> nominationTypes) {
        this.nominationTypes = nominationTypes;
    }

}
