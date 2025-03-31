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
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;
import za.gov.sars.amakhwezi.persistence.UserRepository;

/**
 *
 * @author S2026987
 */
@Service
@Transactional
public class UserService implements UserServiceLocal {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public User deleteById(Long id) {
        User user = findById(id);
        if (user != null) {
            userRepository.delete(user);
        }
        return user;
    }

    @Override
    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Override
    public boolean isExist(User user) {
        return userRepository.findById(user.getId()) != null;
    }
    
    @Override
    public User findByEmployeeSid(String employeeSid){
        return userRepository.findByEmployeeSid(employeeSid);
    }

    @Override
    public List<User> searchForSystemUsers(String searchParam) {
        return  userRepository.searchForSystemUsers(searchParam);
    }

    @Override
    public List<User> findByUserRole(UserRole userRole) {
        return userRepository.findByUserRole(userRole);
    }

    @Override
    public List<User> findBySidOrFirstNameOrLastName(String searchParam) {
        return userRepository.findBySidOrFirstNameOrLastName(searchParam, searchParam);
    }

    @Override
    public List<User> findAllAdministratorUsers(){
        return userRepository.findAllAdministratorUsers();
    }
    
    public boolean isUserRoleUsed(UserRole userRole){
        return !userRepository.findByUserRole(userRole).isEmpty();
    }
    
     @Override
    public List<User> search(String searchParam) {
        return userRepository.search(searchParam);
    }

    @Override
    public List<User> findUsersByRoleDescription(List<String> descriptions) {
        return userRepository.findUsersByRoleDescription(descriptions);
    }
}
