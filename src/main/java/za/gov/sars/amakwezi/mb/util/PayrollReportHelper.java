/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb.util;

import java.util.Date;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026987
 */
public class PayrollReportHelper {
    
    private String id;
    private Long referenceId;
    private Employee nominee;    
    private Date approvedDate;
    private String wageType;
    private Double amount;
    private Date refferenceNo;   

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

    public String getWageType() {
        return wageType;
    }

    public void setWageType(String wageType) {
        this.wageType = wageType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getRefferenceNo() {
        return refferenceNo;
    }

    public void setRefferenceNo(Date refferenceNo) {
        this.refferenceNo = refferenceNo;
    }
    
    
}
