/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import za.gov.sars.amakhwezi.common.CostCentre;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026064
 */
public interface CostCentreServiceLocal {
    
    List<CostCentre> findAllCostCentres();
  
    
}
