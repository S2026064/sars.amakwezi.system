/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.mb.util;

import java.util.Date;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.Nomination;

/**
 *
 * @author S2026080
 */
public class NominationHelper {

    private String id;
    private Employee nominee;
    private Date approvedDate;
    private Employee financeManager;
    private String referenceId;
    private Date createdDate;
    private String orgKey;
    private Category category;
    private Contribution contribution;
    private Nomination nomination;
    private String motivation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Employee getNominee() {
        return nominee;
    }

    public void setNominee(Employee nominee) {
        this.nominee = nominee;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getOrgKey() {
        return orgKey;
    }

    public void setOrgKey(String orgKey) {
        this.orgKey = orgKey;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    public Nomination getNomination() {
        return nomination;
    }

    public void setNomination(Nomination nomination) {
        this.nomination = nomination;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public Employee getFinanceManager() {
        return financeManager;
    }

    public void setFinanceManager(Employee financeManager) {
        this.financeManager = financeManager;
    }
    
    

}
