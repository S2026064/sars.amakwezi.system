/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;

/**
 *
 * @author S2026987
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT e FROM User e WHERE e.employee.employeeSid=:employeeSid")
    User findByEmployeeSid(@Param("employeeSid") String employeeSid);

    @Query("SELECT e FROM User e WHERE e.employee.employeeSid LIKE %:sid%")
    List<User> searchForSystemUsers(String sid);

    @Query("SELECT e FROM User e WHERE e.employee IS NOT NULL")
    List<User> findAllAdministratorUsers();

    @Query("SELECT e FROM User e WHERE e.employee.employeeSid LIKE %:employeeSid% OR e.employee.empDetails.fullnames LIKE %:fullnames%")
    List<User> findBySidOrFirstNameOrLastName(@Param("employeeSid") String employeeSid, @Param("fullnames") String fullnames);

    List<User> findByUserRole(UserRole userRole); 
    //add custom query fore user
    @Query("SELECT e From User e WHERE e.userRole.description IN (:descriptions)")
    List<User> findUsersByRoleDescription(@Param("descriptions")List<String> descriptions);
    
    
    @Query("SELECT e FROM User e WHERE (e.employee.employeeSid LIKE %:searchParam%  OR e.employee.empDetails.fullnames LIKE %:searchParam% )")
    List<User> search(@Param("searchParam")String searchParam);

    
}
