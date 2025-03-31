/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import za.gov.sars.amakhwezi.common.NominationType;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "constribution")
@Getter
@Setter
public class Contribution extends BaseEntity {

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private double amount;

    @Column(name = "nomination_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NominationType nominationType;

}
