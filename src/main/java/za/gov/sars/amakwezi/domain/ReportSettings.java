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
@Table(name = "report_sett")
@Getter
@Setter
public class ReportSettings extends BaseEntity{
    
    @Column(name = "report")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean report;
    
    public ReportSettings(){
        this.report = Boolean.FALSE;
    }
    
    public boolean isReporter(){
        return this.report;
    }
    
}
