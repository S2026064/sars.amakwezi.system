/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.common.UserStatus;
import za.gov.sars.amakhwezi.domain.AdministrationSettings;
import za.gov.sars.amakhwezi.domain.NominationSettings;
import za.gov.sars.amakhwezi.domain.Permission;
import za.gov.sars.amakhwezi.domain.ReportSettings;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;
import za.gov.sars.amakhwezi.service.UserRoleServiceLocal;
import za.gov.sars.amakhwezi.service.UserServiceLocal;

/**
 *
 * @author S2026015
 */
@ManagedBean
@ViewScoped
public class UserRoleBean extends BaseBean<UserRole>{
    
    @Autowired
    private UserRoleServiceLocal userRoleService;
    @Autowired
    private  UserServiceLocal userService;
    
    private UserStatus userStatus;

   @PostConstruct
    public void init() {
        reset().setList(true);
        setPanelTitleName("User Roles");
        addCollections(userRoleService.listAll());
    }
    public void addOrUpdate(UserRole userRole) {
        reset().setAdd(true);
        if (userRole != null) {
            setPanelTitleName("Update User Role");
            userRole.setUpdatedBy(getActiveUser().getSid());
            userRole.setUpdatedDate(new Date());
        } else {
            userRole = new UserRole();
            setPanelTitleName("Add User Role");
            userRole.setCreatedBy(getActiveUser().getSid());
            userRole.setCreatedDate(new Date());

            AdministrationSettings administrationSettings = new AdministrationSettings();
            administrationSettings.setCreatedBy(getActiveUser().getSid());
            administrationSettings.setCreatedDate(new Date());
            userRole.setAdministrationSettings(administrationSettings);

            NominationSettings nominationSettings = new NominationSettings();
            nominationSettings.setCreatedBy(getActiveUser().getSid());
            nominationSettings.setCreatedDate(new Date());
            userRole.setNominationSettings(nominationSettings);

            ReportSettings reportSettings = new ReportSettings();
            reportSettings.setCreatedBy(getActiveUser().getSid());
            reportSettings.setCreatedDate(new Date());
            userRole.setReportSettings(reportSettings);

            Permission permission = new Permission();
            permission.setCreatedBy(getActiveUser().getSid());
            permission.setCreatedDate(new Date());
            userRole.setPermission(permission);

            addCollection(userRole);
        }
        addEntity(userRole);
    }

    public void save(UserRole userRole) {
        if (userRole.getId() != null) {
            userRoleService.update(userRole);
            addInformationMessage("User Role was successfully updated.");
        } else {
            userRoleService.save(userRole);
            addInformationMessage("User Role was successfully created.");
        }
        reset().setList(true);
        setPanelTitleName("User Roles");
    }

    public void cancel(UserRole userRole) {
        if (userRole.getId() == null && getUserRoles().contains(userRole)) {
            remove(userRole);
        }
        reset().setList(true);
        setPanelTitleName("User Roles");
    }

    public void delete(UserRole userRole) {
        if (userService.isUserRoleUsed(userRole)) {
            addWarningMessage("This Role cannot be deleted because it is being used by other Users");
            return;
        }
        userRoleService.deleteById(userRole.getId());
        remove(userRole);
        addInformationMessage("User Role was successfully deleted");
        reset().setList(true);
        setPanelTitleName("User Roles");
    }
    
     public List<UserRole> getUserRoles() {
        return this.getCollections();
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
      
}
