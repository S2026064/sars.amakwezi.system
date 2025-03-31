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
public enum ReportType {
  //  RECIEVED_NOMINATIONS("Nominations Received"), 
    CAPTURED_NOMINATIONS("Captured Nominations"),
    COST_CENTRE_REPORT("Cost centre report "),
    REROUTED_NOMINATIONS_REPORT("Rerouted Nomination report "),
  //  NOMINATION_VALUES("Nomination Values"),
   // NOMINATION_CATEGORIES("Nominations Categories"),
    PAYROLL_REPORT("Payroll Report");
    
    private final String name;
    
    ReportType(final String name){
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
}
