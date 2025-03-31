package za.gov.sars.amakhwezi.mb;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.domain.UserRole;
import za.gov.sars.amakhwezi.common.Router;

@ManagedBean
@SessionScoped
@Getter
@Setter
public class ActiveUser implements Serializable {

    private String sid;
    private String lineManagerSid;
    private String lineManagerFullNames;
    private boolean userLoginIndicator;
    private UserRole userRole;
    private String moduleWelcomeMessage;
    private String loggedOnUserFullName;
    private String fullName;
    private String numberOfDirectReports;
    private Router router = new Router();

    private String costCentreManagerSid;
    private String costCentreManagerEmployeeNumber;
    private String costCentreManagerFullnames;
    private String financeManagerSid;
    private String financeManagerEmployeeNumber;
    private String financeManagerFullnames;
    private String costCentreNumber;
    private String costCentreName;
    private String personalNum;

    public ActiveUser() {
        userLoginIndicator = Boolean.FALSE;
    }

    public void setLogonUserSession(User user) {
        if (user.getEmployee() != null) {
            this.setFullName(user.getEmployee().getEmpDetails().getFullnames());
            this.setSid(user.getEmployee().getEmployeeSid());
            this.setLineManagerSid(user.getEmployee().getEmpDetails().getManagerSid());
            this.setLineManagerFullNames(user.getEmployee().getEmpDetails().getManagerName());
            this.setNumberOfDirectReports(user.getEmployee().getNumberOfDirectReports());
            this.setCostCentreManagerSid(user.getEmployee().getEmpDetails().getCostCentreManagerSid());
            this.setCostCentreManagerFullnames(user.getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            this.setFinanceManagerSid(user.getEmployee().getEmpDetails().getFinanceManagerSid());
            this.setFinanceManagerFullnames(user.getEmployee().getEmpDetails().getFinanceManagerFullnames());
            this.setCostCentreNumber(user.getEmployee().getEmpDetails().getCostCenterNumber());
            this.setCostCentreName(user.getEmployee().getEmpDetails().getCostCenterName());
            this.setPersonalNum(user.getEmployee().getEmpDetails().getPersonnelNum());
            this.setCostCentreManagerEmployeeNumber(user.getEmployee().getEmpDetails().getCostCentreManagerEmployeeNumber());
            this.setFinanceManagerEmployeeNumber(user.getEmployee().getEmpDetails().getFinanceManagerEmployeeNumber());
        }
        this.setUserRole(user.getUserRole());
        this.setLoggedOnUserFullName(getFullName());
        this.setUserLoginIndicator(true);

    }
}
