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
public enum BudgetStatus {
    ALLOCATED("Allocated"),
    REQUESTED("Requested"),
    REJECTED("Rejected");
    
    private final String name;
    
    BudgetStatus(final String name){
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
