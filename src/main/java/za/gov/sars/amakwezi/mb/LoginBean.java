/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import za.gov.sars.amakhwezi.domain.User;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.common.UserStatus;
import za.gov.sars.amakhwezi.service.UserServiceLocal;

/**
 *
 * @author S2026987
 */
@ManagedBean
@RequestScoped
public class LoginBean extends BaseBean<User> {

    @Autowired
    private UserServiceLocal userService;
    
    private String sidParam;

    public void signIn() {
        User user = userService.findByEmployeeSid(sidParam);
        if (user != null) {
            if (user.getUserStatus().equals(UserStatus.ACTIVE)) {
                getActiveUser().setLogonUserSession(user);
                redirect("home");
            } else {
                addErrorMessage("System user with SID number", sidParam, " is not active");
            }
        } else {
            addErrorMessage("System user with SID number", sidParam, "does not exist");
        }
    }

    public String getSidParam() {
        return sidParam;
    }

    public void setSidParam(String sidParam) {
        this.sidParam = sidParam;
    }
}
