/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author S2026987
 */
@ManagedBean
@RequestScoped
public class MenuBean extends BaseBean {
    @PostConstruct
    public void init() {

    }
    public String route(String page) {
        System.out.println("selected page =" + page);
        return defaultRouter(page);
    }
    public String routing(String page) {
        if (page.equalsIgnoreCase("/administrations")) {
            getActiveUser().getRouter().reset().setAdministrator(true);
        } else if (page.equalsIgnoreCase("/nominations")) {
            getActiveUser().getRouter().reset().setNomination(true);
        }else if (page.equalsIgnoreCase("/reports")) {
            getActiveUser().getRouter().reset().setReports(true);
        }
        return defaultRouter(page);
    }
}
