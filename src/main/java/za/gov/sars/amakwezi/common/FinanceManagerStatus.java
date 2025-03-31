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
public enum FinanceManagerStatus {
    APPROVED("Approved"),
    REJECTED("Rejected"),
    PENDING("Pending");
    
    private final String name;
    
    FinanceManagerStatus(final String name){
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
