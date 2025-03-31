/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.service.UserServiceLocal;

/**
 *
 * @author S2026987
 */
@ManagedBean
@RequestScoped
public class SsoBean extends BaseBean<User> {

    @Autowired
    private UserServiceLocal userService;

    public void initialization() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String remoteUserSid = externalContext.getRemoteUser() != null ? externalContext.getUserPrincipal().getName() : "S2024727";
        User persistentUser = userService.findByEmployeeSid(remoteUserSid);

        if (persistentUser != null) {
            getActiveUser().setLoggedOnUserFullName(persistentUser.getEmployee().getEmpDetails().getFullnames());
            getActiveUser().setUserLoginIndicator(true);
            getActiveUser().setUserRole(persistentUser.getUserRole());

            getActiveUser().setFullName(persistentUser.getEmployee().getEmpDetails().getFullnames());
            getActiveUser().setSid(remoteUserSid);
            getActiveUser().setLineManagerSid(persistentUser.getEmployee().getEmpDetails().getManagerSid());
            getActiveUser().setLineManagerFullNames(persistentUser.getEmployee().getEmpDetails().getManagerName());
            getActiveUser().setNumberOfDirectReports(persistentUser.getEmployee().getNumberOfDirectReports());
            getActiveUser().setCostCentreManagerSid(persistentUser.getEmployee().getEmpDetails().getCostCentreManagerSid());
            getActiveUser().setCostCentreManagerFullnames(persistentUser.getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            getActiveUser().setFinanceManagerSid(persistentUser.getEmployee().getEmpDetails().getFinanceManagerSid());
            getActiveUser().setFinanceManagerFullnames(persistentUser.getEmployee().getEmpDetails().getFinanceManagerFullnames());
            getActiveUser().setCostCentreNumber(persistentUser.getEmployee().getEmpDetails().getCostCenterNumber());
            getActiveUser().setCostCentreName(persistentUser.getEmployee().getEmpDetails().getCostCenterName());
            getActiveUser().setPersonalNum(persistentUser.getEmployee().getEmpDetails().getPersonnelNum());
            getActiveUser().setCostCentreManagerEmployeeNumber(persistentUser.getEmployee().getEmpDetails().getCostCentreManagerEmployeeNumber());
            getActiveUser().setFinanceManagerEmployeeNumber(persistentUser.getEmployee().getEmpDetails().getFinanceManagerEmployeeNumber());

            redirect("home");
        } else {
            getActiveUser().setUserLoginIndicator(false);
            redirect("accessdenied");
        }
    }
}
