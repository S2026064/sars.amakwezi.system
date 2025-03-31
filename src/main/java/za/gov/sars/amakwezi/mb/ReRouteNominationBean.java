/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.mb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import za.gov.sars.amakhwezi.common.CostCentreManagerStatus;
import za.gov.sars.amakhwezi.common.FinanceManagerStatus;
import za.gov.sars.amakhwezi.common.NominationStatus;
import za.gov.sars.amakhwezi.common.SarsValue;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.service.EmployeeInformationServiceLocal;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;
import za.gov.sars.amakhwezi.service.UserRoleServiceLocal;
import za.gov.sars.amakhwezi.service.UserServiceLocal;
import za.gov.sars.amakhwezi.service.mail.LDAPService;
import za.gov.sars.amakhwezi.service.mail.MailService;

/**
 *
 * @author S2026064
 */
@ManagedBean
@ViewScoped
public class ReRouteNominationBean extends BaseBean<Nomination> {

    @Autowired
    private NominationServiceLocal nominationService;
    @Autowired
    private UserServiceLocal userService;
    @Autowired
    private UserRoleServiceLocal userRoleService;
    @Autowired
    private EmployeeInformationServiceLocal employeeInformationService;
//    @Autowired
//    private MailService mailService;

    private final MailService mailService = new MailService();

    private List<User> managers = new ArrayList<>();
    private List<NominationStatus> statuses = new ArrayList<>();
    private User selectedManager;

    private List<String> emailNotificationRecipients = new ArrayList<>();
    private LDAPService lDAPService = new LDAPService();

    private Slice<Nomination> slices;

    @PostConstruct
    public void init() {
        this.reset().setList(true);

        setPanelTitleName("Reroute Nominations");
        Pageable pageable = PageRequest.of(0, 15, Sort.by("referenceId").ascending());
        statuses.add(NominationStatus.SUBMITTED);
        statuses.add(NominationStatus.REROUTED);
        statuses.add(NominationStatus.FINANCE_REROUTED);

        slices = nominationService.findByNominationStatus(statuses, pageable);
        addCollections(slices.toList());

    }

    public void reRouteNomination(Nomination nomination) {
        managers.clear();
        List<String> roleDescriptions = new ArrayList<>();

        if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED)) {
            if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED) && !nomination.getFinanceManagerStatus().equals(FinanceManagerStatus.APPROVED)) {
                roleDescriptions.add("Finance Manager");
                roleDescriptions.add("Line and Finance Manager");
                managers.addAll(userService.findUsersByRoleDescription(roleDescriptions));
            }
        } else {
            roleDescriptions.add("Cost Centre Manager");
            roleDescriptions.add("Line and Cost Centre Manager");
            managers.addAll(userService.findUsersByRoleDescription(roleDescriptions));
        }
        addEntity(nomination);
        reset().setReRoutePanel(true);
    }

    public void route(Nomination nomination) {
        nomination.setUpdatedBy(getActiveUser().getSid());
        nomination.setUpdatedDate(new Date());

        if (nomination.getCreatedBy().equals(selectedManager.getEmployee().getEmployeeSid())) {
            addWarningMessage("You cannot reroute a nomination to the nominator");
            return;
        }

        if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.PENDING) && (nomination.getNominationStatus().equals(NominationStatus.SUBMITTED) || nomination.getNominationStatus().equals(NominationStatus.REROUTED))) {
            nomination.setReroutecostCenterManagerFullnames(selectedManager.getEmployee().getEmpDetails().getFullnames());
            nomination.setReroutecostCenterManagerSid(selectedManager.getEmployee().getEmployeeSid());
            nomination.setRerouteCostCentreEmployeeNumber(selectedManager.getEmployee().getEmpDetails().getPersonnelNum());
            nomination.setNominationStatus(NominationStatus.REROUTED);
            nomination.setReroutedDate(new Date());
            nomination.setRerouterSid(getActiveUser().getSid());
            //send email notification to nominator and Cost Center
            sendEmailNotification(nomination);
            sendEmailCostCenterNotification(nomination);

        } else if (nomination.getFinanceManagerStatus().equals(FinanceManagerStatus.PENDING) && nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED) && (nomination.getNominationStatus().equals(NominationStatus.SUBMITTED) || nomination.getNominationStatus().equals(NominationStatus.REROUTED) || nomination.getNominationStatus().equals(NominationStatus.FINANCE_REROUTED))) {
            nomination.setReroutefinanceManagerFullnames(selectedManager.getEmployee().getEmpDetails().getFullnames());
            nomination.setReroutefinanceManagerSid(selectedManager.getEmployee().getEmployeeSid());
            nomination.setReroutefinanceManagerEmployeeNumber(selectedManager.getEmployee().getEmpDetails().getPersonnelNum());
            nomination.setNominationStatus(NominationStatus.FINANCE_REROUTED);
            nomination.setCostCentreManagerStatus(CostCentreManagerStatus.APPROVED);
            nomination.setReroutedDate(new Date());
            nomination.setRerouterSid(getActiveUser().getSid());
            //send email notofication
            sendToFinanceManager(nomination);
        }

        nominationService.update(nomination);
        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            addInformationMessage("Nomination of ", empNomination.getEmployee().getEmpDetails().getFullnames(), " of SID ", empNomination.getEmployee().getEmployeeSid(), " was successfully routed");
        }
        this.reset().setList(true);
    }

    public void deleteSubmittedNomination(Nomination nomination) {
        if (nomination.getNominationStatus().equals(NominationStatus.SUBMITTED) || nomination.getNominationStatus().equals(NominationStatus.REROUTED) || nomination.getNominationStatus().equals(NominationStatus.FINANCE_REROUTED)) {
            if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.PENDING)) {
                nomination.setUpdatedBy(getActiveUser().getSid());
                nomination.setUpdatedDate(new Date());
                nomination.setNominationStatus(NominationStatus.DELETED);

                nominationService.update(nomination);
                addInformationMessage("The nomination was successfully deleted");
                deleteSubmittedNominationCostCentre(nomination);
                deleteSubmittedNominationNominatorMail(nomination);
                synchronize(nomination);
                reset().setList(true);
            } else if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED)) {
                nomination.setUpdatedBy(getActiveUser().getSid());
                nomination.setUpdatedDate(new Date());
                nomination.setNominationStatus(NominationStatus.DELETED);

                nominationService.update(nomination);
                addInformationMessage("The nomination was successfully deleted");
                deleteSubmittedNominationFinanceManager(nomination);
                deleteSubmittedNominationCostCentre(nomination);
                deleteSubmittedNominationNominatorMail(nomination);
                synchronize(nomination);
                reset().setList(true);
            }

        }

    }

    public void deleteSubmittedNominationCostCentre(Nomination nomination) {

        emailNotificationRecipients.clear();
        String destinationAddress;
        if (!(nomination.getReroutecostCenterManagerSid() == null)) {
            //  destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getReroutecostCenterManagerSid());
            destinationAddress = lDAPService.getUserEmailAddress(nomination.getReroutecostCenterManagerSid());
            //   destinationAddress = lDAPService.getUserEmailAddress("s2026064");
        } else {
            //  destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getCostCenterManagerSid());
            destinationAddress = lDAPService.getUserEmailAddress(nomination.getCostCenterManagerSid());
            //     destinationAddress = lDAPService.getUserEmailAddress("s2026064");
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

            //  destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getReroutefinanceManagerSid());
            destinationAddress = lDAPService.getUserEmailAddress(nomination.getReroutefinanceManagerSid());
            //  destinationAddress = lDAPService.getUserEmailAddress("s2026064");
        } else {
            //  destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getFinanceManagerSid());
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

    public void deleteSubmittedNominationNominatorMail(Nomination nomination) {

        emailNotificationRecipients.clear();

        //  String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmployeeSid());
        String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmployeeSid());
        //  String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Nominator's Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);
        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
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
        builder.append("<tr colspana=5>");
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
            addInformationMessage("Email notification has been sent to the nominator");
        }
    }

    //sent to nominator when reroute to cost center or finance
    public void sendEmailNotification(Nomination nomination) {
        emailNotificationRecipients.clear();

        //   String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
        String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
        //   String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Cost Centre Manager Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
        builder.append("<br /><br />");

        builder.append("The below nomination for On-the-Spot Rewards and Recognition was rerouted to an alternative approver, please see the details below.");
        builder.append("<br /><br />");

        builder.append("<table border=1 style=width: 200px;>");
        builder.append("<tr>");
        builder.append("<th>Nomination No</th><th>Nomination Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
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
            builder.append(nomination.getReroutefinanceManagerFullnames());
            builder.append("</td>");
            builder.append("</tr>");
            builder.append("<tr>");
            builder.append("<td>Finance manager's SID</td>");
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

        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the Nominator");
        }
    }

//send reroute email to cost center
    public void sendEmailCostCenterNotification(Nomination nomination) {
        emailNotificationRecipients.clear();

        //   String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
        String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
        //  String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Cost Centre Manager Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        builder.append(nomination.getReroutecostCenterManagerFullnames());
        builder.append("<br /><br />");

        builder.append("The below nomination for On-the-Spot Rewards and Recognition was rerouted to yourself as an alternative approver, please see the details below.");
        builder.append("<br /><br />");

        builder.append("<table border=1 style=width: 200px;>");
        builder.append("<tr>");
        builder.append("<th>Nomination No</th><th>Nomination Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
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
            builder.append("<td>Re- Routed Cost Center manager's SID</td>");
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
            builder.append("<tr>");
            builder.append("<td>Re- Routed Finance manager Name and Surname</td>");
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

        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the Cost Center Manager");
        }
    }

    //send reroute email to finance Manager
    public void sendToFinanceManager(Nomination nomination) {
        emailNotificationRecipients.clear();

        // String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
        String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
        //   String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Finance Manager's Destination Email Address is null");
            return;
        }
        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ");
        builder.append(nomination.getReroutefinanceManagerFullnames());
        builder.append("<br /><br />");

        builder.append("The below nomination for On-the-Spot Rewards and Recognition was rerouted to yourself as an alternative approver, please see the details below.");
        builder.append("<br /><br />");
        builder.append("Nomination information");
        builder.append("<br />");
        builder.append("<table border=1>");
        builder.append("<tr>");
        builder.append("<th>Nomination No</th><th>Nomination Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
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

        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the Finance Manager");
        }
    }

    //send Notitification to Line manager after from finanace manager
    public void sendApprovalEmailNotification(Nomination nomination) {
        if (nomination.getFinanceManagerStatus().equals(FinanceManagerStatus.APPROVED)) {

            emailNotificationRecipients.clear();

            //   String   destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid());
            //    String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

            if (StringUtils.isEmpty(destinationAddress)) {
                addInformationMessage("Nominator's Destination Email Address is null");
                return;
            }
            emailNotificationRecipients.add(destinationAddress);

            StringBuilder builder = new StringBuilder();
            builder.append("Dear ");
            builder.append(nomination.getCostCenterManagerFullnames());
            builder.append("<br /><br />");

            builder.append("The below nomination for On-the-Spot Rewards and Recognition was approved on ");
            builder.append(convertStringToDate(nomination.getUpdatedDate()));
            builder.append("<br /><br />");

            builder.append("<table border=1 style=width: 200px;>");
            builder.append("<tr>");
            builder.append("<th>Nomination No</th><th>Nomination Date</th><th>Category</th><th>Value</th><th>On the spot award</th>");
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

            if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
                addInformationMessage("Email notification has been sent to the Cost Center Manager");
            }
        }
    }

    public void nextNominations() {
        if (slices.hasNext()) {
            //  slices = nominationService.findEscalatedNominations(slices.nextPageable());
            slices = nominationService.findByNominationStatus(statuses, slices.nextPageable());
            addCollections(slices.toList());
        }
    }

    public void prevNominations() {
        if (slices.hasPrevious()) {
            //   slices = nominationService.findEscalatedNominations(slices.previousPageable());
            slices = nominationService.findByNominationStatus(statuses, slices.previousPageable());
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

    public List<Nomination> getNominations() {
        return this.getCollections();
    }

    public void cancel() {
        reset().setList(true);
    }

    public List<User> getManagers() {
        return managers;
    }

    public void setManagers(List<User> managers) {
        this.managers = managers;
    }

    public User getSelectedManager() {
        return selectedManager;
    }

    public void setSelectedManager(User selectedManager) {
        this.selectedManager = selectedManager;
    }

    public List<NominationStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<NominationStatus> statuses) {
        this.statuses = statuses;
    }

}
