/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.mb.util;

import java.util.Date;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.Nomination;

/**
 *
 * @author S2026080
 */
public class ReroutedReportHelper {

    private String id;
    private Date approvedDate;
    private Employee financeManager;
    private Employee costCentreManager;
    private Employee nominator;
    private Long referenceId;
    private Date createdDate;
    private Employee nominee;
    private String rerouterSid;
    private Nomination nomination;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Employee getFinanceManager() {
        return financeManager;
    }

    public void setFinanceManager(Employee financeManager) {
        this.financeManager = financeManager;
    }

    public Employee getCostCentreManager() {
        return costCentreManager;
    }

    public void setCostCentreManager(Employee costCentreManager) {
        this.costCentreManager = costCentreManager;
    }

    public Employee getNominator() {
        return nominator;
    }

    public void setNominator(Employee nominator) {
        this.nominator = nominator;
    }

    public Employee getNominee() {
        return nominee;
    }

    public void setNominee(Employee nominee) {
        this.nominee = nominee;
    }

    public Nomination getNomination() {
        return nomination;
    }

    public void setNomination(Nomination nomination) {
        this.nomination = nomination;
    }

    public String getRerouterSid() {
        return rerouterSid;
    }

    public void setRerouterSid(String rerouterSid) {
        this.rerouterSid = rerouterSid;
    }

}
