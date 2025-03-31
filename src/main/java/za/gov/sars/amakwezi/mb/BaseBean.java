/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import za.gov.sars.amakhwezi.domain.User;

/**
 *
 * @author S2026987
 */
public class BaseBean<T> extends SpringBeanAutowiringSupport implements Serializable {

    @ManagedProperty(value = "#{activeUser}")
    private ActiveUser activeUser;
    private List<String> errorCollectionMsg = new ArrayList<>();
    private boolean list;
    private boolean add;
    private boolean update;
    private boolean search;
    private boolean addChild;
    private boolean link;
    private boolean delink;
    private boolean actuals;
    private boolean viewReport;
    private boolean reviewReport;
    private boolean firstPanel;
    private boolean secondPanel;
    private boolean thirdPanel;
    private boolean reRoutePanel;
    private List<T> collections = new ArrayList<>();

    T entity;
    private String confirmationMessage;
    private String panelTitleName;

    private final String OS = System.getProperty("os.name").toLowerCase();

    final Logger LOG = Logger.getLogger(BaseBean.class.getName());

    public BaseBean() {
    }

    public ActiveUser getActiveUser() {
        return activeUser;
    }

    /**
     * @param activeUser the activeUser to set
     */
    public void setActiveUser(ActiveUser activeUser) {
        this.activeUser = activeUser;
    }

    public void redirect(String page) {
        try {
            StringBuilder builder = new StringBuilder(page);
            builder.append(".xhtml");
            FacesContext.getCurrentInstance().getExternalContext().redirect(builder.toString());
        } catch (IOException ex) {
            Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void redirecting(String page) {
        try {
            StringBuilder builder = new StringBuilder(page);
            builder.append(".xhtml?faces-redirect=true");
            FacesContext.getCurrentInstance().getExternalContext().redirect(builder.toString());
        } catch (IOException ex) {
            Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addInformationMessage(String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void addInformationMessage(String... detail) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : detail) {
            stringBuilder.append(message);
            stringBuilder.append(" ");
        }
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", stringBuilder.toString().trim());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void addErrorMessage(String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void addErrorMessage(String... detail) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : detail) {
            stringBuilder.append(message);
            stringBuilder.append(" ");
        }
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", stringBuilder.toString().trim());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void addWarningMessage(String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "", detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void addWarningMessage(String... detail) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : detail) {
            stringBuilder.append(message);
            stringBuilder.append(" ");
        }
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "", stringBuilder.toString().trim());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void invalidateUserSession() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public void addError(String... message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String error : message) {
            stringBuilder.append(error);
            stringBuilder.append(" ");
        }
        this.getErrorCollectionMsg().add(stringBuilder.toString());
    }

    public void addInfomation(String... message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String information : message) {
            stringBuilder.append(information);
            stringBuilder.append(" ");
        }
        this.getErrorCollectionMsg().add(stringBuilder.toString());
    }

    public void addWarning(String... message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String warning : message) {
            stringBuilder.append(warning);
            stringBuilder.append(" ");
        }
        this.getErrorCollectionMsg().add(stringBuilder.toString());
    }

    public String defaultRouter(String page) {
        StringBuilder builder = new StringBuilder(page);
        builder.append(".xhtml");
        return builder.toString();
    }

    public String defaultRouting(String page) {
        StringBuilder builder = new StringBuilder(page);
        builder.append(".xhtml");
        return builder.toString();
    }

    /**
     * @return the errorCollectionMsg
     */
    public List<String> getErrorCollectionMsg() {
        return errorCollectionMsg;
    }

    /**
     * @param errorCollectionMsg the errorCollectionMsg to set
     */
    public void setErrorCollectionMsg(List<String> errorCollectionMsg) {
        this.errorCollectionMsg = errorCollectionMsg;
    }

    public void addErrorCollectionMsg(String message) {
        errorCollectionMsg.add(message);
    }

    public BaseBean reset() {
        setList(false);
        setAdd(false);
        setSearch(false);
        setUpdate(false);
        setAddChild(false);
        setLink(false);
        setDelink(false);
        setActuals(false);
        setViewReport(false);
        setReviewReport(false);
        setFirstPanel(false);
        setSecondPanel(false);
        setThirdPanel(false);
        setReRoutePanel(false);
        return this;
    }

    public boolean isWindows() {
        return (OS.contains("win"));
    }

    public boolean isMac() {
        return (OS.contains("mac"));
    }

    public boolean isLinux() {
        return (OS.contains("nux"));
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

    public boolean isAddChild() {
        return addChild;
    }

    public void setAddChild(boolean addChild) {
        this.addChild = addChild;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public boolean isDelink() {
        return delink;
    }

    public void setDelink(boolean delink) {
        this.delink = delink;
    }

    public List<T> getCollections() {
        return collections;
    }

    public void setCollections(List<T> collections) {
        this.collections = collections;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public void addEntity(T entity) {
        this.entity = entity;
    }

    public void addCollections(List<T> list) {
        collections.clear();
        collections.addAll(list);
    }
    
    public void updateCollections(List<T> list) {
        collections.addAll(list);
    }

    public void addCollection(T entity) {
        collections.add(0, entity);
    }

    public void addCollections(Set<T> list) {
        collections.clear();
        collections.addAll(list);
    }
    
    public void synchronize(T entity){
        if(collections.contains(entity)){
            collections.remove(entity);
        }
    }

    public void refreshTable(T entity) {
        collections.add(0, entity);
    }

    public void remove(T entity) {
        collections.remove(entity);
    }
    public Date formattedDate(String dateString) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder builder = new StringBuilder(dateString.substring(0, 4));
        builder.append("-");
        builder.append(dateString.substring(4, 6));
        builder.append("-");
        builder.append(dateString.substring(6, 8));
        try {
            return sdfSource.parse(builder.toString());
        } catch (ParseException ex) {
            Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String convertStringToDate(Date inputDateParam) {
        if (inputDateParam != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            return sdf.format(inputDateParam);
        } else {
            return "";
        }
    }
    
    public String convertStringToDateMmYyyy(Date inputDateParam) {
        if (inputDateParam != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
            return sdf.format(inputDateParam);
        } else {
            return "";
        }
    }

    public boolean isSearch() {
        return search;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getPanelTitleName() {
        return panelTitleName;
    }

    public void setPanelTitleName(String panelTitleName) {
        this.panelTitleName = panelTitleName;
    }

    public boolean isActuals() {
        return actuals;
    }

    public void setActuals(boolean actuals) {
        this.actuals = actuals;
    }

    public boolean isViewReport() {
        return viewReport;
    }

    public void setViewReport(boolean viewReport) {
        this.viewReport = viewReport;
    }

    public boolean isReviewReport() {
        return reviewReport;
    }

    public void setReviewReport(boolean reviewReport) {
        this.reviewReport = reviewReport;
    }

    public boolean isFirstPanel() {
        return firstPanel;
    }

    public void setFirstPanel(boolean firstPanel) {
        this.firstPanel = firstPanel;
    }

    public boolean isSecondPanel() {
        return secondPanel;
    }

    public void setSecondPanel(boolean secondPanel) {
        this.secondPanel = secondPanel;
    }

    public boolean isThirdPanel() {
        return thirdPanel;
    }

    public void setThirdPanel(boolean thirdPanel) {
        this.thirdPanel = thirdPanel;
    }

    public boolean isReRoutePanel() {
        return reRoutePanel;
    }

    public void setReRoutePanel(boolean reRoutePanel) {
        this.reRoutePanel = reRoutePanel;
    }
    
}
