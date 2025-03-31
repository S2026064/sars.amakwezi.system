/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.common;

import java.sql.Connection;

/**
 *
 * @author S2026987
 */
public interface DatasourceService {
    
    public Connection getDatasourceConnection();
}
