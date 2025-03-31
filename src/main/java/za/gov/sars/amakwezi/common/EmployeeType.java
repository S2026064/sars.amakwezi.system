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
public enum EmployeeType {
    SYSTEM_USER_EMPLOYEE("System User Employee"),
    NON_SYSTEM_USER_EMPLOYEE("Non System User Employee");
    
    private final String name;
    
    EmployeeType(final String name){
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
