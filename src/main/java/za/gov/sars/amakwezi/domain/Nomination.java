/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import za.gov.sars.amakhwezi.common.CostCentreManagerStatus;
import za.gov.sars.amakhwezi.common.FinanceManagerStatus;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.common.SarsValue;
import za.gov.sars.amakhwezi.common.Value;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "nomination")
@Getter
@Setter
public class Nomination extends BaseEntity {

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "nomination", targetEntity = EmpNomination.class, orphanRemoval = true)
    private List<EmpNomination> employeeNominations = new ArrayList();

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Value.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "nomination_values")
    @OrderColumn
    @Column(name = "sars_values")
    private List<Value> values = new ArrayList();

    @ElementCollection(fetch = FetchType.EAGER, targetClass = SarsValue.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "nomination__sars_values")
    @OrderColumn
    @Column(name = "new_sars_values")
    private List<SarsValue> sarsValues = new ArrayList();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, targetEntity = Contribution.class)
    @JoinColumn(name = "constribution_id")
    private Contribution contribution;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, targetEntity = Category.class)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "motivation", length = 1500)
    private String motivation;

    @Column(name = "reference_id", unique = true, updatable = false, nullable = false)
    private Long referenceId;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "nominator_id")
    private User nominator;

    @Column(name = "nomination_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NominationType nominationType;

    @Column(name = "nomination_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NominationStatus nominationStatus;

    @Column(name = "cost_center_status", nullable = true)
    @Enumerated(EnumType.STRING)
    private CostCentreManagerStatus costCentreManagerStatus;

    @Column(name = "finance_status", nullable = true)
    @Enumerated(EnumType.STRING)
    private FinanceManagerStatus financeManagerStatus;

    @Column(name = "cost_centre_num")
    private String CostCentreNumber;

    @Column(name = "cost_centre_name")
    private String CostCentreName;

    @Column(name = "cost_centre_manager_employee_num")
    private String CostCentreEmployeeNumber;

    @Column(name = "finance_manager_employee_num")
    private String financeManagerEmployeeNumber;

    @Column(name = "approver_fullnames")
    private String approverFullNames;

    @Column(name = "cost_center_manager_sid")
    private String costCenterManagerSid;

    @Column(name = "cost_center_manager_fullnames")
    private String costCenterManagerFullnames;

    @Column(name = "finance_manager_sid")
    private String financeManagerSid;

    @Column(name = "finance_manager_fullnames")
    private String financeManagerFullnames;

    @Column(name = "reroute_finance_manager_sid")
    private String reroutefinanceManagerSid;

    @Column(name = "reroute_finance_manager_fullnames")
    private String reroutefinanceManagerFullnames;

    @Column(name = "reroute_cost_center_manager_sid")
    private String reroutecostCenterManagerSid;

    @Column(name = "reroute_cost_center_manager_fullnames")
    private String reroutecostCenterManagerFullnames;
    
    @Column(name = "reroute_cost_centre_manager_employee_num")
    private String rerouteCostCentreEmployeeNumber;

    @Column(name = "reroute_finance_manager_employee_num")
    private String reroutefinanceManagerEmployeeNumber;

    @Column(name = "rerouter_sid")
    private String rerouterSid;

    @Column(name = "rerouted_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reroutedDate;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, targetEntity = RejectionReason.class)
    @JoinColumn(name = "rejectionReason_id", nullable = true)
    private RejectionReason rejectionReason;

    public void addValue(Value value) {
        values.add(value);
    }

    public void removeValue(Value value) {
        values.remove(value);
    }

    public void addSarsValue(SarsValue sValue) {
        sarsValues.add(sValue);
    }

    public void removeSarsValue(SarsValue sValue) {
        sarsValues.remove(sValue);
    }

    public void addEmployeeNomination(EmpNomination empNomination) {
        empNomination.setNomination(this);
        employeeNominations.add(empNomination);
    }

    public void addEmployeeNominations(List<EmpNomination> empNominations) {
        employeeNominations.clear();
        employeeNominations.addAll(empNominations);
    }

    public void removeEmployeeNomination(EmpNomination empNomination) {
        if (employeeNominations.contains(empNomination)) {
            employeeNominations.remove(empNomination);
            //empNomination.setNomination(null);
        }
    }

    public void delinkEmployeeNomination(EmpNomination empNomination) {
        if (employeeNominations.contains(empNomination)) {
            empNomination.setNomination(null);
        }
    }

}
