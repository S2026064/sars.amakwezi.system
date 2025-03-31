/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "user_role")
@Getter
@Setter
public class UserRole extends BaseEntity {
    
    @Column(name = "description")
    private String description;
    
    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "admin_sett_id")
    private AdministrationSettings administrationSettings;
    
    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "nomination_sett_id")
    private NominationSettings nominationSettings;
    
    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "report_sett_id")
    private ReportSettings reportSettings;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Permission.class)
    @JoinColumn(name = "permission_id")
    private Permission permission;
    
}
