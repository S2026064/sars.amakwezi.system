/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;

/**
 *
 * @author S2026987
 */
public interface UserServiceLocal {

    User save(User user);

    User findById(Long id);

    User update(User user);

    User deleteById(Long id);

    List<User> findByUserRole(UserRole userRole);

    List<User> listAll();

    User findByEmployeeSid(String employeeSid);

    List<User> searchForSystemUsers(String searchParam);

    List<User> findBySidOrFirstNameOrLastName(String searchParam);

    boolean isExist(User user);

    boolean isUserRoleUsed(UserRole userRole);

    List<User> findAllAdministratorUsers();

    List<User> search(String searchParam);

    List<User> findUsersByRoleDescription(List<String> descriptions);
}
