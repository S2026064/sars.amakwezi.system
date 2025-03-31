/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.mb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import za.gov.sars.amakhwezi.common.CertificateDTO;
import za.gov.sars.amakhwezi.common.CostCentreManagerStatus;
import za.gov.sars.amakhwezi.common.FinanceManagerStatus;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.common.SarsValue;
import za.gov.sars.amakhwezi.common.Value;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Contribution;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.mb.util.UniqueReferenceGenerator;
import za.gov.sars.amakhwezi.service.CategoryServiceLocal;
import za.gov.sars.amakhwezi.service.ContributionServiceLocal;
import za.gov.sars.amakhwezi.service.EmployeeInformationServiceLocal;
import za.gov.sars.amakhwezi.service.EmployeeServiceLocal;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;
import za.gov.sars.amakhwezi.service.UploadMultipleNomineesService;
import za.gov.sars.amakhwezi.service.UserServiceLocal;
import za.gov.sars.amakhwezi.service.mail.LDAPService;
import za.gov.sars.amakhwezi.service.mail.MailService;

/**
 *
 * @author S1017275
 */
@ManagedBean
@ViewScoped
public class RevisedNominationBean extends BaseBean<Nomination> {

    @Autowired
    private EmployeeServiceLocal employeeService;
    @Autowired
    private NominationServiceLocal nominationService;
    @Autowired
    private ContributionServiceLocal contributionService;
    @Autowired
    private CategoryServiceLocal categoryService;
    @Autowired
    private UserServiceLocal userService;
    @Autowired
    private EmployeeInformationServiceLocal employeeInformationService;
    @Autowired
    private UploadMultipleNomineesService uploadMultipleNomineesService;

    private List<NominationType> nominationTypes = new ArrayList<>();
    private List<Contribution> contributions = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Value> values = new ArrayList<>();
    private List<SarsValue> sarsValues = new ArrayList<>();
    private Integer activeIndex;
    private boolean indexing;
    private String empSid;
    private String searchParameter;
    private String costCentreNumber;
    private User nominator;
    private User administrator;
    private User costCenterManager;
    private User financeManager;
    private final String SOURCE_ADDRESS = "noreplyamakhwezi@sars.gov.za";
    private List<Nomination> nominations = new ArrayList<>();
    private MailService mailService;
    private List<String> emailNotificationRecipients = new ArrayList<>();
    private LDAPService lDAPService = new LDAPService();

    private Slice<Nomination> slices;

    @PostConstruct
    public void init() {
        mailService = new MailService();

        this.reset().setList(true);
        setPanelTitleName("Employee Nominations");
        nominationTypes.addAll(Arrays.asList(NominationType.values()));
        categories.addAll(categoryService.listAll());

        sarsValues.addAll(Arrays.asList(SarsValue.values()));
        values.addAll(Arrays.asList(Value.values()));

        Pageable pageable = PageRequest.of(0, 15, Sort.by("referenceId").descending());
        slices = nominationService.findNominationsByUserSid(getActiveUser().getSid(), pageable);

        addCollections(slices.toList());

    }

    public void firstNominationPanel() {

        if (getActiveUser().getNumberOfDirectReports() == null) {
            addWarningMessage("You do not have the ability to add a nomination");
            return;
        }

        this.reset().setFirstPanel(true);
        setPanelTitleName("Nomination Type");

        indexing = true;
        setActiveIndex(0);

        Nomination nomination = new Nomination();
        nomination.setCreatedBy(getActiveUser().getSid());
        nomination.setCreatedDate(new Date());
        nomination.setCostCentreNumber(getActiveUser().getCostCentreNumber());
        nomination.setCostCentreName(getActiveUser().getCostCentreName());
        nomination.setNominator(userService.findByEmployeeSid(getActiveUser().getSid()));
        nomination.setCostCenterManagerSid(getActiveUser().getCostCentreManagerSid());
        nomination.setCostCenterManagerFullnames(getActiveUser().getCostCentreManagerFullnames());
        nomination.setFinanceManagerSid(getActiveUser().getFinanceManagerSid());
        nomination.setFinanceManagerFullnames(getActiveUser().getFinanceManagerFullnames());
        nomination.setCostCentreEmployeeNumber(getActiveUser().getCostCentreManagerEmployeeNumber());
        nomination.setFinanceManagerEmployeeNumber(getActiveUser().getFinanceManagerEmployeeNumber());

        addEntity(nomination);
        addCollection(nomination);

    }

    public void nominationTypeListner() {

        if (getEntity().getNominationType() == null) {
            addWarningMessage("Please select the nomination type first");
            return;
        }

        this.reset().setSecondPanel(true);
        setPanelTitleName("Nomination Details");
        contributions.clear();
        contributions.addAll(contributionService.findByNominationType(getEntity().getNominationType()));
        setActiveIndex(1);
    }

    public void thirdNominationPanel() {

        this.reset().setThirdPanel(true);
        setPanelTitleName("Nominee Details");
    }

    public void nomineesUploadFile(FileUploadEvent event) {
        try {
            if (event != null) {
                for (Employee employee : uploadMultipleNomineesService.findAllEmployeesByEmployeeSid(event.getFile().getInputStream(), getActiveUser().getSid())) {
                    employee.setCreatedBy(getActiveUser().getSid());
                    employee.setCreatedDate(new Date());
                    employeeService.save(employee);

                    // check if we have added employee with sid and employee number that belong to one employee
                    for (EmpNomination empNomination : getEntity().getEmployeeNominations()) {
                        if (empNomination.getEmployee().getEmployeeSid().compareToIgnoreCase(employee.getEmployeeSid()) == 0) {
                            addWarningMessage("The employee with " + employee.getEmployeeSid() + " already exist as part of the team.");
                            return;
                        } else if (empNomination.getEmployee().getEmpDetails().getPersonnelNum().contains(employee.getEmpDetails().getPersonnelNum())) {
                            addWarningMessage("The employee with " + employee.getEmpDetails().getPersonnelNum() + " already exist as part of the team.");
                            return;
                        }
                    }
                    //check nominator include him/her self in the list as the nominator is not allowed to nominate themselves
                    if (employee.getEmployeeSid().trim().equalsIgnoreCase(getActiveUser().getSid()) || employee.getEmpDetails().getPersonnelNum().equals(getActiveUser().getPersonalNum())) {
                        addWarningMessage("Please note that you cannot nominate yourself");
                        return;
                    }
                    EmpNomination employeeNomination = new EmpNomination();
                    employeeNomination.setCreatedBy(getActiveUser().getSid());
                    employeeNomination.setCreatedDate(new Date());
                    employeeNomination.setEmployee(employee);

                    getEntity().addEmployeeNomination(employeeNomination);
                }
                addInformationMessage("Template named", event.getFile().getFileName(), "of multiple employees successfully uploaded");
            } else {
                addInformationMessage("There was a problem uploading the Template");
            }
        } catch (IOException ex) {
            Logger.getLogger(RevisedNominationBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addNominee() {

        if (this.searchParameter == null || this.searchParameter.isEmpty()) {
            addWarningMessage("Please enter employee sid or employee number");
            return;
        }

        if (!sidCorrectPattern()) {
            addWarningMessage(searchParameter + " is not a valid employee sid or employee number");
            return;
        }

        if (this.searchParameter.trim().equalsIgnoreCase(getActiveUser().getSid()) || this.searchParameter.trim().equals(getActiveUser().getPersonalNum())) {
            addWarningMessage("Please note that you cannot nominate yourself");
            return;
        }
        if (getEntity().getNominationType().equals(NominationType.TEAM)) {
            for (EmpNomination empNomination : getEntity().getEmployeeNominations()) {
                if (empNomination.getEmployee().getEmployeeSid().compareToIgnoreCase(this.searchParameter.trim()) == 0) {

                    addWarningMessage("The employee with " + searchParameter + " already exist as part of the team.");
                    return;
                } else if (empNomination.getEmployee().getEmpDetails().getPersonnelNum().contains(this.searchParameter.trim())) {
                    addWarningMessage("The employee with " + searchParameter + " already exist as part of the team.");
                    return;
                }
            }
        }
//        Seraching employee from Amakhwezi DB Employee Table
        Employee emp = employeeService.findByEmployeeSidOrPersonnelNum(searchParameter);
        if (emp == null) {
//       Seraching employee from Assets DB which contain Employee Information from SAP System
            emp = employeeInformationService.getEmployeeBySid(this.searchParameter, getActiveUser().getSid());

            if (emp != null) {
                emp.setCreatedBy(getActiveUser().getSid());
                emp.setCreatedDate(new Date());
                emp = employeeService.save(emp);
            } else {
                addWarningMessage("The employee with " + this.searchParameter + " does not exist");
                return;
            }
        }

//        if (emp == null) {
//            addWarningMessage("The employee with " + this.searchParameter + " does not exist");
//            return;
//        }
        if (getEntity().getNominationType().equals(NominationType.INDIVIDUAL) && !getEntity().getEmployeeNominations().isEmpty()) {
            addWarningMessage("You cannot add more than one employee for an individual nomination");
            return;
        }

        EmpNomination employeeNomination = new EmpNomination();
        employeeNomination.setCreatedBy(getActiveUser().getSid());
        employeeNomination.setCreatedDate(new Date());
        employeeNomination.setEmployee(emp);

        getEntity().addEmployeeNomination(employeeNomination);

        this.searchParameter = "";
    }

    public void save(Nomination nomination) {
        nomination.setNominationStatus(NominationStatus.SAVED);
        if (nomination.getId() != null) {

            nomination.setUpdatedBy(getActiveUser().getSid());
            nomination.setUpdatedDate(new Date());
            nominationService.update(nomination);
        } else {
            //Ensure this native query is using the correct Database for reference query
            UniqueReferenceGenerator uniqueReferenceGenerator = new UniqueReferenceGenerator();
            nomination.setReferenceId(uniqueReferenceGenerator.generateNumber());
            nominationService.save(nomination);
        }
        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            addInformationMessage("Nomination of ", empNomination.getEmployee().getEmpDetails().getFullnames(), " of SID ", empNomination.getEmployee().getEmployeeSid(), " was successfully saved");
        }

        setPanelTitleName("Employee Nominations");
        indexing = false;
        this.reset().setList(true);
    }

    public void submit(Nomination nomination) {

        getErrorCollectionMsg().clear();
        List<String> sids = new ArrayList<>();
        if (nomination.getNominationType().equals(NominationType.INDIVIDUAL)) {
            if (nomination.getEmployeeNominations().size() > 1) {
                addErrorCollectionMsg("You can only add one employee as a nominee");
            }
        }
        if (nomination.getNominationType().equals(NominationType.TEAM)) {
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                sids.add(empNomination.getEmployee().getEmployeeSid());
            }
            Set<String> set = new HashSet<>();
            for (String existingSid : sids) {
                if (set.contains(existingSid)) {
                    addErrorCollectionMsg("Employee with Sid " + existingSid + " already exist as a nominee");
                } else {
                    set.add(existingSid);
                }
            }
        }

        if (!(nomination.getNominator().getEmployee().getEmployeeSid().equals(getActiveUser().getCostCentreManagerSid()))) {

            if (nomination.getEmployeeNominations().isEmpty()) {
                addErrorCollectionMsg("Please add atleast one employee as a nominee");
//                addWarningMessage("Please add atleast one employee as a nominee");
//                return;
            }

            if (nomination.getNominationType().equals(NominationType.TEAM) && getEntity().getEmployeeNominations().size() <= 1) {
                addErrorCollectionMsg("You need more than 1 nominee for this nomination as it is a team nomination.");
                //return;
            }

            if (nomination.getContribution() == null) {
                addErrorCollectionMsg("Please select the contribution for this nomination");
                //return;
            }

            if (nomination.getCategory() == null) {
                addErrorCollectionMsg("Please select the category for this nomination");
                // return;
            }

            if (nomination.getSarsValues().isEmpty()) {
                addErrorCollectionMsg("Please select atleast one value for this nomination");
                // return;
            }

            if (nomination.getMotivation().isEmpty()) {
                addErrorCollectionMsg("Please enter the motivation for this nomination");
                // return;
            }
            if (getErrorCollectionMsg().isEmpty()) {
                nomination.setNominationStatus(NominationStatus.SUBMITTED);
                nomination.setCostCentreManagerStatus(CostCentreManagerStatus.PENDING);
                nomination.setFinanceManagerStatus(FinanceManagerStatus.PENDING);
//                This code is working pefectly waiting for client to authorise the auto approval if the Cost Centre is amongst nominees
//                for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
//                    if (empNomination.getEmployee().getEmployeeSid().equals(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid())) {
//                        nomination.setCostCentreManagerStatus(CostCentreManagerStatus.APPROVED);
//                        nomination.setFinanceManagerStatus(FinanceManagerStatus.PENDING);
//                        break;
//                    }
//                }

                if (nomination.getId() != null) {
                    remove(nomination);
                    nomination.setUpdatedBy(getActiveUser().getSid());
                    nomination.setUpdatedDate(new Date());
                    nominationService.update(nomination);
                    addCollection(getEntity());
                } else {
                    //Ensure this native query is using the correct Database for reference query
                    UniqueReferenceGenerator uniqueReferenceGenerator = new UniqueReferenceGenerator();
                    nomination.setReferenceId(uniqueReferenceGenerator.generateNumber());
                    nominationService.save(nomination);
                }

                for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                    addInformationMessage("Nomination of ", empNomination.getEmployee().getEmpDetails().getFullnames(), " of SID ", empNomination.getEmployee().getEmployeeSid(), " was successfully submitted");
                }

                //send email notifications
                sendEmailCostCenterNotification(nomination);
                setPanelTitleName("Employee Nominations");
                indexing = false;
                this.reset().setList(true);
            } else {
                for (String erroMessage : getErrorCollectionMsg()) {
                    addErrorMessage(erroMessage);
//                       addErrorCollectionMsg(erroMessage);
                }
            }

        } else {
            if (nomination.getEmployeeNominations().isEmpty()) {
                addErrorCollectionMsg("Please add atleast one employee as a nominee");
                // return;
            }

            if (nomination.getNominationType().equals(NominationType.TEAM) && getEntity().getEmployeeNominations().size() <= 1) {
                addErrorCollectionMsg("You need more than 1 nominee for this nomination as it is a team nomination.");
                // return;
            }

            if (nomination.getContribution() == null) {
                addErrorCollectionMsg("Please select the contribution for this nomination");
                // return;
            }

            if (nomination.getCategory() == null) {
                addErrorCollectionMsg("Please select the category for this nomination");
                // return;
            }

            if (nomination.getSarsValues().isEmpty()) {
                addErrorCollectionMsg("Please select atleast one value for this nomination");
                // return;
            }

            if (nomination.getMotivation().isEmpty()) {
                addErrorCollectionMsg("Please enter the motivation for this nomination");
                //return;
            }
            if (getErrorCollectionMsg().isEmpty()) {
                nomination.setNominationStatus(NominationStatus.SUBMITTED);
                nomination.setCostCentreManagerStatus(CostCentreManagerStatus.APPROVED);
                nomination.setFinanceManagerStatus(FinanceManagerStatus.PENDING);

                if (nomination.getId() != null) {
                    remove(nomination);
                    nomination.setUpdatedBy(getActiveUser().getSid());
                    nomination.setUpdatedDate(new Date());
                    nominationService.update(nomination);
                    addCollection(getEntity());
                } else {
                    //Ensure this native query is using the correct Database for reference query
                    UniqueReferenceGenerator uniqueReferenceGenerator = new UniqueReferenceGenerator();
                    nomination.setReferenceId(uniqueReferenceGenerator.generateNumber());
                    nominationService.save(nomination);
                }

                addInformationMessage("Nomination was successfully submitted");

                //send email notifications
                sendToFinanceManager(nomination);
                setPanelTitleName("Employee Nominations");
                indexing = false;
                this.reset().setList(true);
            } else {
                for (String errorMessage : getErrorCollectionMsg()) {
                    addErrorMessage(errorMessage);
                }
            }

        }
    }

    public boolean sidCorrectPattern() {
        return searchParameter.length() == 8;
    }

    public void reviewNomination(Nomination nomination) {
        setPanelTitleName("Employee Nominations Details");

        addEntity(nomination);
        reset().setReviewReport(true);
    }

    public void viewNominationDetails(Nomination nomination) {
        setPanelTitleName("Employee Nominations Details");
        reset().setViewReport(true);
        addEntity(nomination);
    }

    public void cancel(Nomination nomination) {
        if (nomination.getId() == null) {
            if (getCollections().contains(nomination)) {
                getCollections().remove(nomination);
            }
        }

        indexing = false;

        setPanelTitleName("Employee Nominations");
        reset().setList(true);
    }

    public void back() {
        reset().setList(true);
    }

    public void removeNominee(EmpNomination empNomination) {
        synchronizes(getEntity(), empNomination);
        if (empNomination.getId() != null) {
            addEntity(nominationService.update(getEntity()));
        }
        addCollection(getEntity());
        addInformationMessage("Nominee has been removed successfully");
    }

    public void updateNomination(Nomination nomination) {
        setPanelTitleName("Nomination Details");
        contributions.clear();
        contributions.addAll(contributionService.findByNominationType(nomination.getNominationType()));
        addEntity(nomination);
        reset().setSecondPanel(true);
    }

    public void deleteNomination(Nomination nomination) {
        nominationService.deleteById(nomination.getId());
        addInformationMessage("Saved nomination was successfully deleted");
        synchronize(nomination);
        reset().setList(true);
    }

    public void deleteSubmittedNomination(Nomination nomination) {
        if (nomination.getNominationStatus().equals(NominationStatus.SUBMITTED) || nomination.getNominationStatus().equals(NominationStatus.REROUTED) || nomination.getNominationStatus().equals(NominationStatus.FINANCE_REROUTED)) {
            if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.PENDING)) {
                nomination.setUpdatedBy(getActiveUser().getSid());
                nomination.setUpdatedDate(new Date());
                nomination.setNominationStatus(NominationStatus.DELETED);

                nominationService.update(nomination);
                addInformationMessage("The escalated nomination was successfully deleted");
                deleteSubmittedNominationCostCentre(nomination);
                deleteSubmittedNominationAdminMail(nomination);
                synchronize(nomination);
                reset().setList(true);
            } else if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED)) {
                nomination.setUpdatedBy(getActiveUser().getSid());
                nomination.setUpdatedDate(new Date());
                nomination.setNominationStatus(NominationStatus.DELETED);

                nominationService.update(nomination);
                addInformationMessage("The escalated nomination was successfully deleted");
                deleteSubmittedNominationFinanceManager(nomination);
                deleteSubmittedNominationAdminMail(nomination);
                synchronize(nomination);
                reset().setList(true);
            }

        }

    }

    public void synchronizes(Nomination nomination) {
        if (this.getCollections().contains(nomination)) {
            getCollections().remove(nomination);
        }
    }

    public void synchronizes(Nomination nomination, EmpNomination empNomination) {
        nomination.removeEmployeeNomination(empNomination);
        if (this.getCollections().contains(nomination)) {
            getCollections().remove(nomination);
        }
    }

    public void sendEmailCostCenterNotification(Nomination nomination) {
        if (nomination.getNominationStatus().equals(NominationStatus.SUBMITTED)) {

            emailNotificationRecipients.clear();

            //   String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            //    String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

            if (StringUtils.isEmpty(destinationAddress)) {
                addInformationMessage("Cost Centre Manager Destination Email Address is null");
                return;
            }
            emailNotificationRecipients.add(destinationAddress);

            StringBuilder builder = new StringBuilder();
            builder.append("Dear ");
            builder.append(nomination.getCostCenterManagerFullnames());
            builder.append("<br /><br />");

            builder.append("The below nomination for On-the-Spot Rewards and Recognition was sent to you for your action.");
            builder.append("<br /><br />");

            builder.append("Nomination information");
            builder.append("<br />");
            builder.append("<table border=1 style=width: 200px;>");
            builder.append("<tr>");
            builder.append("<th>Nomination No</th><th>Submission Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>");
            builder.append(nomination.getReferenceId());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(convertStringToDate(nomination.getCreatedDate()));
            builder.append("</td>");
            builder.append("<td>");
            builder.append(nomination.getCategory().getDescription());
            builder.append("</td>");
            builder.append("<td>");
            for (SarsValue value : nomination.getSarsValues()) {
                builder.append(value.toString());
            }
            builder.append("</td>");
            builder.append("<td>");
            builder.append(nomination.getContribution().getAmount());
            builder.append("0");
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr colspan=5>");
            builder.append("<td colspan=5>");
            builder.append("Motivation");
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr colspan=5>");
            builder.append("<td colspan=5>");
            builder.append(nomination.getMotivation());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("</table>");
            builder.append("<br /><br />");

            builder.append("Nominees information");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<th>Name and Surname</th><th>Employee No</th><th>Sid</th><th>Personnel area</th><th>Personnel sub area</th><th>Cost Centre</th><th>Cost Center Description</th>");
            builder.append("</tr>");
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                builder.append("<tr>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmployeeSid());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getDivisionName());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getSubDivision());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterNumber());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterName());
                builder.append("</td>");
                builder.append("</tr>");
            }
            builder.append("</table>");
            builder.append("<br /><br />");

            builder.append("Nominator information");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<td>Nominator Name and Surname:</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Nominator Employee number</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Nominator's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmployeeSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Nominator's Cost Center</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterNumber());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Nominator's Cost Center Description</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterName());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("</table>");
            builder.append("<br /><br />");

            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Please action this nomination request, by clicking on the URL</td>");
            builder.append("<td>");
            builder.append("http://ecentral.sars.gov.za/onthespotrewards");
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("</table>");
            builder.append("<br /><br />");
            builder.append("This is a system generated workflow that needs to be actioned within 10 working days, to prevent escalation.");
            builder.append("<br /><br />");
            builder.append("Employee Shared Services");

            if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination from: " + getActiveUser().getFullName() + " " + getActiveUser().getSid(), builder.toString())) {
                addInformationMessage("Email notification has been sent to the Cost Center Manager");
            }
        }
    }

    public void sendToFinanceManager(Nomination nomination) {
        if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED)) {

            emailNotificationRecipients.clear();

                 String   destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
           // String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

            if (StringUtils.isEmpty(destinationAddress)) {
                addInformationMessage("Cost Centre Manager Destination Email Address is null");
                return;
            }
            emailNotificationRecipients.add(destinationAddress);
            StringBuilder builder = new StringBuilder();
            builder.append("Dear ");
            builder.append(nomination.getFinanceManagerFullnames());
            builder.append("<br /><br />");

            builder.append("The below nomination for On-the-Spot Rewards and Recognition was sent to you for your action.");
            builder.append("<br /><br />");
            builder.append("Nomination information");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<th>Nomination No</th><th>Submission Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>");
            builder.append(nomination.getReferenceId());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(convertStringToDate(nomination.getCreatedDate()));
            builder.append("</td>");
            builder.append("<td>");
            builder.append(nomination.getCategory().getDescription());
            builder.append("</td>");
            builder.append("<td>");
            for (SarsValue value : nomination.getSarsValues()) {
                builder.append(value.toString());
            }
            builder.append("</td>");
            builder.append("<td>");
            builder.append(nomination.getContribution().getAmount());
            builder.append("0");
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr colspan=5>");
            builder.append("<td colspan=5>");
            builder.append("Motivation");
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr colspan=5>");
            builder.append("<td colspan=5>");
            builder.append(nomination.getMotivation());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("</table>");
            builder.append("<br /><br />");

            builder.append("Nominees information");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<th>Name and Surname</th><th>Employee No</th><th>Sid</th><th>Personnel area</th><th>Personnel sub area</th><th>Cost Centre</th><th>Cost Center Description</th>");
            builder.append("</tr>");
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                builder.append("<tr>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmployeeSid());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getDivisionName());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getSubDivision());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterNumber());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterName());
                builder.append("</td>");
                builder.append("</tr>");
            }
            builder.append("</table>");
            builder.append("<br /><br />");

            builder.append("Nominator information");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<th>Nominator Name and Surname:</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Nominator Employee number</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Nominator's SID</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmployeeSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Nominator's Cost Center</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterNumber());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Nominator's Cost Center Description</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterName());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("</table>");
            builder.append("<br /><br />");

            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<th>Cost Center manager Name and Surname</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Cost Center manager's SID</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Status</th>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Finance manager Name and Surname</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Finance manager's SID</th>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Status</th>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<th>Please action this nomination request, by clicking on the URL</th>");
            builder.append("<td>");
            builder.append("http://ecentral.sars.gov.za/onthespotrewards");
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("</table>");
            builder.append("<br /><br />");
            builder.append("This is a system generated workflow that needs to be actioned within 10 working days, to prevent escalation.");
            builder.append("<br /><br />");
            builder.append("Employee Shared Services");

            if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination from: " + getActiveUser().getFullName() + " " + getActiveUser().getSid(), builder.toString())) {
                addInformationMessage("Email notification has been sent to the Cost Center Manager");
            }
        }
    }

    public void deleteSubmittedNominationCostCentre(Nomination nomination) {

        emailNotificationRecipients.clear();

        String destinationAddress;
        if (!(nomination.getReroutecostCenterManagerSid() == null)) {
//            destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
                destinationAddress = lDAPService.getUserEmailAddress(nomination.getReroutecostCenterManagerSid());
        //    destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        } else {
//            destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getCostCenterManagerSid());
              destinationAddress = lDAPService.getUserEmailAddress(nomination.getCostCenterManagerSid());
          //  destinationAddress = lDAPService.getUserEmailAddress("s2026064");
        }

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Cost Centre Manager Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        if (!(nomination.getReroutecostCenterManagerSid() == null)) {
            builder.append(nomination.getReroutecostCenterManagerFullnames());
        } else {
            builder.append(nomination.getCostCenterManagerFullnames());
        }
        builder.append("<br /><br />");
        builder.append("The below nomination for an On-the-Spot Rewards and Recognition deleted on ");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append("<br />");
        builder.append("Nomination information");
        builder.append("<br />");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Nomination No</th><th>Submission date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
        builder.append("</tr>");
        builder.append("<tr>");
        builder.append("<td>");
        builder.append(nomination.getReferenceId());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getCategory().getDescription());
        builder.append("</td>");
        builder.append("<td>");
        for (SarsValue value : nomination.getSarsValues()) {
            builder.append(value.toString());
            builder.append(", ");
        }
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getContribution().getAmount());
        builder.append("0");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("<tr colspan=5>");
        builder.append("<td colspana=5>");
        builder.append("Motivation");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("<tr colspan=5>");
        builder.append("<td colspan=5>");
        builder.append(nomination.getMotivation());
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append(" Nominees information");
        builder.append("<br/>");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Name and Surname</th><th>Employee id</th><th>Sid </th><th>Personnel area</th><th>Personnel sub area</th><th>Cost Center Description </th>");
        builder.append("</tr>");
        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            builder.append("<tr>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmployeeSid());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getDivisionName());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getSubDivision());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterName());
            builder.append("</td>");
            builder.append("</tr>");
        }
        builder.append("</table>");
        builder.append("<br /><br />");

        builder.append("Nominator information ");
        builder.append("<br />");

        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator Name and Surname");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator Employee number");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's SID");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmployeeSid());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's Cost Center");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterNumber());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's Cost Center Description");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterName());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("</table>");
        builder.append("<br /><br />");

        if (!(nomination.getReroutecostCenterManagerSid() == null)) {
            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");

            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");

            builder.append("<tr>");
            builder.append("<td>Re-Routed Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutecostCenterManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutecostCenterManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
        } else {
            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
        }

        if (!(nomination.getReroutefinanceManagerSid() == null)) {

            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutefinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutefinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
        } else {
            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
        }

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Please action this nomination request, by clicking on the URL");
        builder.append("</td>");
        builder.append("<td>");
        builder.append("http://ecentral.sars.gov.za/onthespotrewards");
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append("This is a system generated workflow that needs to be actioned within 10 working days, to prevent escalation.");
        builder.append("<br />");
        builder.append("Employee Shared Services");
        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the cost centre manager");
        }
    }

    public void deleteSubmittedNominationFinanceManager(Nomination nomination) {

        emailNotificationRecipients.clear();

        String destinationAddress;
        if (!(nomination.getReroutefinanceManagerSid() == null)) {
//            destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getReroutefinanceManagerSid());
              destinationAddress = lDAPService.getUserEmailAddress(nomination.getReroutefinanceManagerSid());
         //   destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        } else {
//            destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getFinanceManagerSid());
              destinationAddress = lDAPService.getUserEmailAddress(nomination.getFinanceManagerSid());
          //  destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        }

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Finance Manager Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        if (!(nomination.getReroutefinanceManagerSid() == null)) {
            builder.append(nomination.getReroutefinanceManagerFullnames());
        } else {
            builder.append(nomination.getFinanceManagerFullnames());
        }
        builder.append("<br /><br />");

        builder.append("The below nomination for an On-the-Spot Rewards and Recognition deleted on ");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append("<br />");
        builder.append("Nomination information");
        builder.append("<br />");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Nomination No</th><th>Submission date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
        builder.append("</tr>");
        builder.append("<tr>");
        builder.append("<td>");
        builder.append(nomination.getReferenceId());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getCategory().getDescription());
        builder.append("</td>");
        builder.append("<td>");
        for (SarsValue value : nomination.getSarsValues()) {
            builder.append(value.toString());

        }
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getContribution().getAmount());
        builder.append("0");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("<tr colspan=5>");
        builder.append("<td colspan=5>");
        builder.append("Motivation");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("<tr colspan=5>");
        builder.append("<td colspan=5>");
        builder.append(nomination.getMotivation());
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append(" Nominees information");
        builder.append("<br/>");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Name and Surname</th><th>Employee id</th><th>Sid </th><th>Personnel area</th><th>Personnel sub area</th><th>Cost Center Description </th>");
        builder.append("</tr>");
        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            builder.append("<tr>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmployeeSid());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getDivisionName());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getSubDivision());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterName());
            builder.append("</td>");
            builder.append("</tr>");
        }
        builder.append("</table>");
        builder.append("<br /><br />");

        builder.append("Nominator information ");
        builder.append("<br />");

        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator Name and Surname");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator Employee number");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's SID");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmployeeSid());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's Cost Center");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterNumber());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's Cost Center Description");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterName());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("</table>");
        builder.append("<br /><br />");

        if (!(nomination.getReroutecostCenterManagerSid() == null)) {
            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");

            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");

            builder.append("<tr>");
            builder.append("<td>Re-Routed Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutecostCenterManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutecostCenterManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
        } else {
            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
        }
        if (!(nomination.getReroutefinanceManagerSid() == null)) {
            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutefinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutefinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
        } else {
            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
        }
        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Please action this nomination request, by clicking on the URL");
        builder.append("</td>");
        builder.append("<td>");
        builder.append("http://ecentral.sars.gov.za/onthespotrewards");
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append("This is a system generated workflow that needs to be actioned within 10 working days, to prevent escalation.");
        builder.append("<br />");
        builder.append("Employee Shared Services");
        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the finance manager");
        }
    }

    public void deleteSubmittedNominationAdminMail(Nomination nomination) {

        emailNotificationRecipients.clear();
//        List<User> admins = userService.findAllAdministratorUsers();
//        
//        for (User admin : admins) {
        //     String destinationAddress = employeeInformationService.getEmployeeEmailAddress(getAdministrator().getEmployee().getEmployeeSid());
           String destinationAddress = lDAPService.getUserEmailAddress(getAdministrator().getEmployee().getEmployeeSid());
     //   String destinationAddress = lDAPService.getUserEmailAddress("s2026064");
//        }
        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Admin's Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");

        builder.append("Administrator");
        builder.append("<br /><br />");

        builder.append("The below nomination for an On-the-Spot Rewards and Recognition deleted on ");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append("<br />");
        builder.append("Nomination information");
        builder.append("<br />");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Nomination No</th><th>Submission date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
        builder.append("</tr>");
        builder.append("<tr>");
        builder.append("<td>");
        builder.append(nomination.getReferenceId());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getCategory().getDescription());
        builder.append("</td>");
        builder.append("<td>");
        for (SarsValue value : nomination.getSarsValues()) {
            builder.append(value.toString());

        }
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getContribution().getAmount());
        builder.append("0");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("<tr colspan=5>");
        builder.append("<td colspan=5>");
        builder.append("Motivation");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("<tr colspan=5>");
        builder.append("<td colspan=5>");
        builder.append(nomination.getMotivation());
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append(" Nominees information");
        builder.append("<br/>");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Name and Surname</th><th>Employee id</th><th>Sid </th><th>Personnel area</th><th>Personnel sub area</th><th>Cost Center Description </th>");
        builder.append("</tr>");
        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            builder.append("<tr>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmployeeSid());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getDivisionName());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getSubDivision());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterName());
            builder.append("</td>");
            builder.append("</tr>");
        }
        builder.append("</table>");
        builder.append("<br /><br />");

        builder.append("Nominator information ");
        builder.append("<br />");

        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator Name and Surname");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator Employee number");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's SID");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmployeeSid());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's Cost Center");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterNumber());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Nominator's Cost Center Description");
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterName());
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("</table>");
        builder.append("<br /><br />");

        if (!(nomination.getReroutecostCenterManagerSid() == null)) {
            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");

            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");

            builder.append("<tr>");
            builder.append("<td>Re-Routed Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutecostCenterManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutecostCenterManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
        } else {
            builder.append("Approver information ");
            builder.append("<br />");
            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Cost Center manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getCostCentreManagerStatus().toString());
            builder.append("</td>");
            builder.append("</tr>");
        }
        if (!(nomination.getReroutefinanceManagerSid() == null)) {
            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutefinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Re-Routed Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getReroutefinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
        } else {
            builder.append("<tr>");
            builder.append("<td>Finance manager Name and Surname</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
            builder.append("<td>");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Status</td>");
            builder.append("<td>");
            builder.append(nomination.getFinanceManagerStatus());
            builder.append("</td>");
            builder.append("</tr>");
        }
        builder.append("<tr>");
        builder.append("<td>");
        builder.append("Please action this nomination request, by clicking on the URL");
        builder.append("</td>");
        builder.append("<td>");
        builder.append("http://ecentral.sars.gov.za/onthespotrewards");
        builder.append("</td>");
        builder.append("</tr>");

        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append("This is a system generated workflow that needs to be actioned within 10 working days, to prevent escalation.");
        builder.append("<br />");
        builder.append("Employee Shared Services");
        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the finance manager");
        }
    }

    //send mail to nominee
    public void sendApproveMailNominees(Nomination nomination) {
        if (nomination.getNominationStatus().equals(NominationStatus.APPROVED)) {

            emailNotificationRecipients.clear();
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                //    String destinationAddress = employeeInformationService.getEmployeeEmailAddress(empNomination.getEmployee().getEmployeeSid());
                    String destinationAddress = lDAPService.getUserEmailAddress(empNomination.getEmployee().getEmployeeSid());
              //  String destinationAddress = lDAPService.getUserEmailAddress("s2026064");
                if (StringUtils.isEmpty(destinationAddress)) {
                    addInformationMessage("Nominee's Destination Email Address is null");
                    return;
                }

                emailNotificationRecipients.add(destinationAddress);

                StringBuilder builder = new StringBuilder();
                builder.append("Dear ");
                builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
                builder.append("<br /><br />");
                builder.append("The below nomination for an On-the-Spot Rewards and Recognition was approved on ");
                builder.append(convertStringToDate(nomination.getUpdatedDate()));
                builder.append("<br /><br />");
                builder.append("Nomination information");
                builder.append("<br />");
                builder.append("<table border=1>");
                builder.append("<tr>");
                builder.append("<th>Nomination No</th><th>Submission Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
                builder.append("</tr>");
                builder.append("<tr>");
                builder.append("<td>");
                builder.append(nomination.getReferenceId());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(convertStringToDate(nomination.getCreatedDate()));
                builder.append("</td>");
                builder.append("<td>");
                builder.append(nomination.getCategory().getDescription());
                builder.append("</td>");
                builder.append("<td>");
                for (SarsValue value : nomination.getSarsValues()) {
                    builder.append(value.toString());
                }
                builder.append("</td>");
                builder.append("<td>");
                builder.append(nomination.getContribution().getAmount());
                builder.append("0");
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("<tr colspan=5>");
                builder.append("<td colspan=5>");
                builder.append("Motivation");
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("<tr colspan=5>");
                builder.append("<td colspan=5>");
                builder.append(nomination.getMotivation());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("</table>");
                builder.append("<br /><br />");

                builder.append("Nominees information");
                builder.append("<br />");
                builder.append("<table border=1>");
                builder.append("<tr>");
                builder.append("<th>Name and Surname</th><th>Employee No</th><th>Sid</th><th>Personnel area</th><th>Personnel sub area</th><th>Cost Centre</th><th>Cost Center Description</th>");
                builder.append("</tr>");
                builder.append("<tr>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmployeeSid());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getDivisionName());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getSubDivision());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterNumber());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getCostCenterName());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("</table>");
                builder.append("<br /><br />");

                builder.append("Nominator information");
                builder.append("<br />");
                builder.append("<table border=1>");
                builder.append("<tr>");
                builder.append("<td>Nominator Name and Surname:</td>");
                builder.append("<td>");
                builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("<tr>");
                builder.append("<td>Nominator Employee number</td>");
                builder.append("<td>");
                builder.append(nomination.getNominator().getEmployee().getEmpDetails().getPersonnelNum());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("<tr>");
                builder.append("<td>Nominator's SID</td>");
                builder.append("<td>");
                builder.append(nomination.getNominator().getEmployee().getEmployeeSid());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("<tr>");
                builder.append("<td>Nominator's Cost Center</td>");
                builder.append("<td>");
                builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterNumber());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("<tr>");
                builder.append("<td>Nominator's Cost Center Description</td>");
                builder.append("<td>");
                builder.append(nomination.getNominator().getEmployee().getEmpDetails().getCostCenterName());
                builder.append("</td>");
                builder.append("</tr>");
                builder.append("</table>");
                builder.append("<br /><br />");

                builder.append("This is a system generated workflow that needs to be actioned within 10 working days, to prevent escalation");
                builder.append("<br /><br />");
                builder.append("Employee Shared Services");

                CertificateDTO certificateDTO = new CertificateDTO();

                certificateDTO.setSourceEmailAddress(SOURCE_ADDRESS);

                certificateDTO.setDestinationEmailAddress(destinationAddress);

                certificateDTO.setEmailSubject("On the spot Rewards and Recognition from: " + getActiveUser().getFullName() + " " + getActiveUser().getSid());
                certificateDTO.setMessage(builder.toString());
                certificateDTO.setAttachmentFileName("On the spot certificate.pdf");
                StringBuilder certificateBuilder = new StringBuilder(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/certificate"));
                certificateBuilder.append("/On the spot certificate.pdf");

                certificateDTO.setInputCertificateFileName(certificateBuilder.toString());

                certificateDTO.setNameAndSurname(empNomination.getEmployee().getEmpDetails().getFullnames());

                if (mailService.sendEmail(certificateDTO)) {
                    addInformationMessage("Email notification has been sent to the nominee");
                }
            }
        }
    }

    public void nextNominations() {
        if (slices.hasNext()) {
            slices = nominationService.findNominationsByUserSid(getActiveUser().getSid(), slices.nextPageable());
            addCollections(slices.toList());
        }
    }

    public void prevNominations() {
        if (slices.hasPrevious()) {
            slices = nominationService.findNominationsByUserSid(getActiveUser().getSid(), slices.previousPageable());
            addCollections(slices.toList());
        }
    }

    public Integer getPageNumber() {
        return slices.getNumber();
    }

    public Boolean getNextPage() {
        return slices.hasNext();
    }

    public Boolean getPrevPage() {
        return slices.hasPrevious();
    }

    public List<NominationType> getNominationTypes() {
        return nominationTypes;
    }

    public void setNominationTypes(List<NominationType> nominationTypes) {
        this.nominationTypes = nominationTypes;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<SarsValue> getSarsValues() {
        return sarsValues;
    }

    public void setSarsValues(List<SarsValue> sarsValues) {
        this.sarsValues = sarsValues;
    }

    public Integer getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(Integer activeIndex) {
        this.activeIndex = activeIndex;
    }

    public boolean isIndexing() {
        return indexing;
    }

    public void setIndexing(boolean indexing) {
        this.indexing = indexing;
    }

    public String getEmpSid() {
        return empSid;
    }

    public void setEmpSid(String empSid) {
        this.empSid = empSid;
    }

    public String getSearchParameter() {
        return searchParameter;
    }

    public void setSearchParameter(String searchParameter) {
        this.searchParameter = searchParameter;
    }

    public String getCostCentreNumber() {
        return costCentreNumber;
    }

    public void setCostCentreNumber(String costCentreNumber) {
        this.costCentreNumber = costCentreNumber;
    }

    public User getNominator() {
        return nominator;
    }

    public void setNominator(User nominator) {
        this.nominator = nominator;
    }

    public User getCostCenterManager() {
        return costCenterManager;
    }

    public void setCostCenterManager(User costCenterManager) {
        this.costCenterManager = costCenterManager;
    }

    public User getFinanceManager() {
        return financeManager;
    }

    public void setFinanceManager(User financeManager) {
        this.financeManager = financeManager;
    }

    public List<Nomination> getNominations() {
        return this.getCollections();
    }

    public void setNominations(List<Nomination> nominations) {
        this.nominations = nominations;
    }

    public User getAdministrator() {
        return administrator;
    }

    public void setAdministrator(User administrator) {
        this.administrator = administrator;
    }

}
