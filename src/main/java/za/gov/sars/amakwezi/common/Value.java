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
public enum Value {
    ACCOUNTABILITY("Accountability"),
    FAIRNESS("Fairness"),
    HONESTY("Honesty"),
    INTEGRITY("Integrity"),
    RESPECT("Respect"),
    TRANSPARENCY("Transparency"),
    TRUST("Trust");
    
    private final String name;
    
    Value(final String name){
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
