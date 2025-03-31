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
public class DatasourceFactory {
    
    
    public static DatasourceService getDatabase(ConnectionType connectionType){
        if(connectionType.equals(ConnectionType.EMPLOYEE_DATABASE)){
            return new EmployeeDatasourceService();
        }
        return null;
    }
}
