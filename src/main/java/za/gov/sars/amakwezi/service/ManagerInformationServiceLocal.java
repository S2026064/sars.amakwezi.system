/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026987
 */
public interface ManagerInformationServiceLocal {
    public List<Employee> getAmakhweziManagers();
   
}
