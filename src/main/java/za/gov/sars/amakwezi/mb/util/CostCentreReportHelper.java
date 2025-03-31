/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.mb.util;

import java.util.Date;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026064
 */
public class CostCentreReportHelper {

    private String id;
    private Long referenceId;
    private Employee nominee;
    private Employee nominator;
    private Date approvedDate;
    private Date createdDate;
    private Contribution contribution;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
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

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    public Employee getNominator() {
        return nominator;
    }

    public void setNominator(Employee nominator) {
        this.nominator = nominator;
    }
    
    
}
