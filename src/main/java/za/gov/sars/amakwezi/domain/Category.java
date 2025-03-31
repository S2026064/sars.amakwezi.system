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

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "category")
@Getter
@Setter
public class Category extends BaseEntity {
    
    @Column(name = "description")
    private String description;
    
}
