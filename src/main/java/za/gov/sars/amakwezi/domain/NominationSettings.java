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
@Table(name = "nomination_sett")
@Getter
@Setter
public class NominationSettings extends BaseEntity {

    @Column(name = "nominate")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean nominate;

    @Column(name = "ccreview_nomination")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean ccReviewNomination;

    @Column(name = "finreview_nomination")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean finReviewNomination;

    @Column(name = "escalated_nominations")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean escalatedNominations;
    
     @Column(name = "reroute_nominations")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean reRouteNominations;

    public NominationSettings() {
        this.nominate = Boolean.FALSE;
        this.ccReviewNomination = Boolean.FALSE;
        this.finReviewNomination = Boolean.FALSE;
        this.escalatedNominations = Boolean.FALSE;
        this.reRouteNominations = Boolean.FALSE;
    }

    public boolean isNomination() {
        return this.nominate || this.ccReviewNomination || this.finReviewNomination || this.escalatedNominations || this.reRouteNominations;
    }

}
