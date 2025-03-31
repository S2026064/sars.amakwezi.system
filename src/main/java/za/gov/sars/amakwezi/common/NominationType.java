/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.common;

/**
 *
 * @author S2026987
 */
public enum NominationType {
    INDIVIDUAL("Individual"),
    TEAM("Team");
    
    private final String name;
    
    NominationType(final String name){
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
