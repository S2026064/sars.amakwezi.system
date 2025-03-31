/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.common.EmployeeType;
import za.gov.sars.amakhwezi.common.UserStatus;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;
import za.gov.sars.amakhwezi.service.EmployeeInformationServiceLocal;
import za.gov.sars.amakhwezi.service.EmployeeServiceLocal;
import za.gov.sars.amakhwezi.service.UserRoleServiceLocal;
import za.gov.sars.amakhwezi.service.UserServiceLocal;

/**
 *
 * @author S2026015
 */
@ManagedBean
@ViewScoped
public class UserBean extends BaseBean<User> {

    @Autowired
    private UserServiceLocal userService;
    @Autowired
    private UserRoleServiceLocal userRoleService;
    @Autowired
    private EmployeeServiceLocal employeeService;
    @Autowired
    private EmployeeInformationServiceLocal employeeInformationService;

    private List<UserRole> userRoles = new ArrayList<>();
    private List<UserStatus> userStatuses = new ArrayList<>();
    private UserStatus userStatus;
    public List<User> users = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger(UserBean.class.getName());
    private String userTitle;
    //private String panelTitle;
    public String searchParameter;
    public String sid;

    public void UserBean() {
    }

    @PostConstruct
    public void init() {
        //LOG.log(Level.INFO, "Initialization - UserBean");
        reset().setList(true);
        setPanelTitleName("Users");
        userRoles.addAll(userRoleService.listAll());
        userStatuses.addAll(Arrays.asList(UserStatus.values()));
    }

    public void addOrUpdate(User usr) {
        reset().setAdd(true);

        if (usr == null) {
            addWarningMessage("The user you have selected is invalid, please check the user and try again");
            return;
        }

        setPanelTitleName("Update User");
        usr.setUpdatedBy(getActiveUser().getSid());
        usr.setUpdatedDate(new Date());

        addEntity(usr);
    }

    public void addUser() {
        reset().setSearch(true);
        setPanelTitleName("Search Employee");
    }

    public void searchEmployee() {
        if (this.sid.equals("") || this.sid == null) {
            addWarningMessage("Please enter the employee S-ID");
            return;
        }
        User existingUser = userService.findByEmployeeSid(sid);

        if (existingUser != null) {
            addWarningMessage("An employee with an sid of " + sid + " already exist as a user");
            return;
        }

        Employee emp = employeeService.findByEmployeeSid(this.sid);
        if (emp == null) {
            emp = employeeInformationService.getEmployeeBySid(this.sid, getActiveUser().getSid());
            if (emp != null) {
                emp.setCreatedBy(getActiveUser().getSid());
                emp.setCreatedDate(new Date());
                emp.setEmployeeType(EmployeeType.NON_SYSTEM_USER_EMPLOYEE);
                emp = employeeService.save(emp);

            }
        }

        if (emp == null) {
            addWarningMessage("The employee with sid of " + sid + " does not exist, please check the sid and try again");
            return;
        }
        User user = new User();
        user.setCreatedBy(getActiveUser().getSid());
        user.setCreatedDate(new Date());
        user.setEmployee(emp);
        addCollection(user);
        addEntity(user);
        reset().setAdd(true);
        setPanelTitleName("Add User");
    }

    public void save(User user) {
        try {
            user.getEmployee().setEmployeeType(EmployeeType.SYSTEM_USER_EMPLOYEE);
            if (user.getId() != null) {
                userService.update(user);
            } else {
                userService.save(user);
            }
            reset().setList(true);
            addInformationMessage("User successfully saved");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Users");
    }

    public void cancel(User usr) {
        if (usr.getId() == null && getUsers().contains(usr)) {
            remove(usr);
        }
        reset().setList(true);
        setPanelTitleName("Users");
    }

    public void delete(User usr) {
        try {
            userService.deleteById(usr.getId());
            remove(usr);
            addInformationMessage("The content of Sid with the name", usr.getEmployee().getEmployeeSid(), "was successfully deleted");
            reset().setList(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
    }

    public void onSystemUserSearchListener() {
        getCollections().clear();
        if (searchParameter.isEmpty()) {
            addInformationMessage("Enter search criteria");
            return;
        }

        addCollections(userService.findBySidOrFirstNameOrLastName(searchParameter));

        if (getCollections().isEmpty()) {
            addInformationMessage("The employee you are searching for is not registered as a user in this system");
            return;
        }
        setUserTitle("Users");
    }

    public boolean manageUserRoleSelection(User user) {
        User foundUser = userService.findByEmployeeSid(user.getEmployee().getEmployeeSid());
        if (foundUser != null) {
            if (user.getUserRole().getDescription().equals("Line Manager")) {
                if (Integer.parseInt(foundUser.getEmployee().getNumberOfDirectReports()) < 1) {
                    addWarningMessage("User does not have subordinates within SAP and therefore can not assume the role of a Line Manager");
                    return true;
                }
            } else if (user.getUserRole().getDescription().equals("Line and Super User")) {
                if (!foundUser.getUserRole().getDescription().equals("Line Manager")) {
                    addWarningMessage("User does not exist as Line Manager within SAP and therefore can not assume the role of a Line and Super User");
                    return true;
                } else if (Integer.parseInt(foundUser.getEmployee().getNumberOfDirectReports()) < 1) {
                    addWarningMessage("User does not have subordinates within SAP and therefore can not assume the role of a Line and Super User");
                    return true;
                }
            } else if (user.getUserRole().getDescription().equals("Line and Cost Manager")) {
                if (!foundUser.getUserRole().getDescription().equals("Line Manager")) {
                    addWarningMessage("User does not exist as Line Manager within SAP and therefore can not assume the role of a Line and Cost Manager");
                    return true;
                } else if (!foundUser.getUserRole().getDescription().equals("Cost Centre Manager")) {
                    addWarningMessage("User does not exist as Cost Centre Manager within SAP and therefore can not assume the role of a Line and Cost Manager");
                    return true;
                }
            } else if (user.getUserRole().getDescription().equals("Line and Finance Manager")) {
                if (!foundUser.getUserRole().getDescription().equals("Line Manager")) {
                    addWarningMessage("User does not exist as Line Manager within SAP and therefore can not assume the role of a Line and Finance Manager");
                    return true;
                } else if (!foundUser.getUserRole().getDescription().equals("Finance Manager")) {
                    addWarningMessage("User does not exist as Finance Manager within SAP and therefore can not assume the role of a Line and Finance Manager");
                    return true;
                }
            }
        }
        return false;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public List<User> getUsers() {
        return this.getCollections();
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    public String getSearchParameter() {
        return searchParameter;
    }

    public void setSearchParameter(String searchParameter) {
        this.searchParameter = searchParameter;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public List<UserStatus> getUserStatuses() {
        return userStatuses;
    }

    public void setUserStatuses(List<UserStatus> userStatuses) {
        this.userStatuses = userStatuses;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

}
