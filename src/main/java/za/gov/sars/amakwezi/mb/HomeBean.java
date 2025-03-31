/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import za.gov.sars.amakhwezi.domain.User;

/**
 *
 * @author S2026987
 */
@ManagedBean
@RequestScoped
public class HomeBean extends BaseBean<User> {
    
    private static final String LANDING_PAGE = "/landing.xhtml";
    private static final String EXPIRY_PAGE = "/expired.xhtml?faces-redirect=true";

    public String routeToAdministration() {
        if (getActiveUser() != null) {
            getActiveUser().setModuleWelcomeMessage("Welcome to Administration Page");
            getActiveUser().getRouter().reset().setAdministrator(true);
            return LANDING_PAGE;
        }
        return EXPIRY_PAGE;
    }
    public String routeToNomination() {
        if (getActiveUser() != null) {
            getActiveUser().setModuleWelcomeMessage("Welcome to Nomination Page");
            getActiveUser().getRouter().reset().setNomination(true);
            return LANDING_PAGE;
        }
        return EXPIRY_PAGE;
    }
    public String routeToReports() {
        if (getActiveUser() != null) {
            getActiveUser().setModuleWelcomeMessage("Welcome to Reports Page");
            getActiveUser().getRouter().reset().setReports(true);
            return LANDING_PAGE;
        }
        return EXPIRY_PAGE;
    }
    
}
