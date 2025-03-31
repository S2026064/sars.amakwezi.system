/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.domain.UserRole;
import za.gov.sars.amakhwezi.persistence.UserRoleRepository;

/**
 *
 * @author S2026987
 */
@Service
@Transactional
public class UserRoleService implements UserRoleServiceLocal {
    
    @Autowired
    private UserRoleRepository userRepository;

    @Override
    public UserRole save(UserRole userRole) {
        return userRepository.save(userRole);
    }
    @Override
    public UserRole findById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                "The requested id [" + id
                + "] does not exist."));
    }
    @Override
    public UserRole update(UserRole userRole) {
        return userRepository.save(userRole);
    }
    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }
    @Override
    public UserRole deleteById(Long id) {
        UserRole userRole = findById(id);
        if (userRole != null) {
            userRepository.delete(userRole);
        }
        return userRole;
    }
    @Override
    public List<UserRole> listAll() {
        return userRepository.findAll();
    }
    @Override
    public boolean isExist(UserRole userRole) {
        return userRepository.findByDescription(userRole.getDescription()) != null;
    }
    @Override
    public UserRole findByDescription(String descripiton) {
        return userRepository.findByDescription(descripiton);
    }
    
}
