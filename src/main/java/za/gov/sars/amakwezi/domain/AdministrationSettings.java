/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "admin_sett")
@Getter
@Setter
public class AdministrationSettings extends BaseEntity{
    
    @Column(name = "sys_user")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean users;

    @Column(name = "sys_user_role")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean userRole;
    
    @Column(name = "categories")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean categories;
    
    @Column(name = "contributions")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean contributions;
    
    @Column(name = "rejectionReasons")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean rejectionReasons;
    
    public AdministrationSettings(){
        this.users = Boolean.FALSE;
        this.userRole = Boolean.FALSE;
        this.categories = Boolean.FALSE;
        this.contributions = Boolean.FALSE;
        this.rejectionReasons=Boolean.FALSE;
    }
    
    public boolean isAdministrator(){
        return this.users || this.userRole || this.categories || this.contributions || this.rejectionReasons;
    }
}
