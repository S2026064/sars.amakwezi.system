/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package za.gov.sars.amakhwezi.service;

import za.gov.sars.amakhwezi.domain.Employee;

/**
 *
 * @author S2026080
 */
public interface ActingManagerInformationServiceLocal {
    public Employee getActingManagerByPositionNumber(String positionNumber);
    
    
}
