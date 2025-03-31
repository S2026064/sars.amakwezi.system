/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.domain.UserRole;

/**
 *
 * @author S2026987
 */
public interface UserRoleServiceLocal {
    
    UserRole findByDescription(String descripiton);

    UserRole save(UserRole userRole);

    UserRole findById(Long id);

    public void deleteAll();

    UserRole update(UserRole userRole);

    UserRole deleteById(Long id);

    List<UserRole> listAll();

    boolean isExist(UserRole userRole);
}
