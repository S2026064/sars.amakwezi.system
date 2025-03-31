/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import za.gov.sars.amakhwezi.common.ConnectionType;
import za.gov.sars.amakhwezi.common.CostCentre;
import za.gov.sars.amakhwezi.common.DatasourceFactory;
import za.gov.sars.amakhwezi.common.DatasourceService;

/**
 *
 * @author S2026064
 */
@Service
public class CostCentreService implements CostCentreServiceLocal {
 
    @Override
    public List<CostCentre> findAllCostCentres() {

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<CostCentre> costCentres = new ArrayList<>();

        try {
            DatasourceService service = DatasourceFactory.getDatabase(ConnectionType.EMPLOYEE_DATABASE);
            connection = service.getDatasourceConnection();
            stmt = connection.prepareStatement("{call  dbo.spGetAmakhweziManager}");
            rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getString("costCentreNumber") != null) {
                CostCentre costCentre = new CostCentre();
                costCentre.setCostCentreNumber(rs.getString("costCentreNumber"));
             //   costCentre.setDescription(rs.getString("ccstx"));
                costCentre.setCostCentreMgrSID(rs.getString("CostCentreMgrSID"));
                costCentre.setCostCentreFinMngSID(rs.getString("CostCentreFinMngSID"));
                costCentres.add(costCentre);
            }
            }
        } catch (SQLException e) {
            Logger.getLogger(CostCentreService.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                Logger.getLogger(CostCentreService.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return costCentres;
    }


}
