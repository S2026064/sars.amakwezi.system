/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.mb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import za.gov.sars.amakhwezi.domain.Employee;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.RejectionReason;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.service.EmployeeInformationServiceLocal;
import za.gov.sars.amakhwezi.service.EmployeeServiceLocal;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;
import za.gov.sars.amakhwezi.service.RejectionReasonServiceLocal;
import za.gov.sars.amakhwezi.service.UserServiceLocal;
import za.gov.sars.amakhwezi.service.mail.LDAPService;
import za.gov.sars.amakhwezi.service.mail.MailService;

/**
 *
 * @author S1017275
 */
@ManagedBean
@ViewScoped
public class CcManagerBean extends BaseBean<Nomination> {

    @Autowired
    private EmployeeServiceLocal employeeService;
    @Autowired
    private UserServiceLocal userService;
    @Autowired
    private NominationServiceLocal nominationService;
    @Autowired
    private EmployeeInformationServiceLocal employeeInformationService;
    @Autowired
    private RejectionReasonServiceLocal rejectionReasonService;
    private boolean indexing;
    private Integer activeIndex;
    private List<Nomination> nominations = new ArrayList<>();
    private List<RejectionReason> rejectionReasons = new ArrayList<>();
    private List<NominationStatus> statuses = new ArrayList<>();

    private final String SOURCE_ADDRESS = "noreplyamakhwezi@sars.gov.za";
    private MailService mailService;
    private List<String> emailNotificationRecipients = new ArrayList<>();
    private LDAPService lDAPService = new LDAPService();

    private User administrator;
    private Slice<Nomination> slices;
    private Slice reroutedSlices;
    private Slice reroutedApprovedAndRejected;
    private int totalPage;
    private int tot;

    @PostConstruct
    public void init() {
        this.reset().setList(true);
        mailService = new MailService();

        setPanelTitleName("Cost Centre Review Nomination");
        statuses.add(NominationStatus.APPROVED);
        statuses.add(NominationStatus.REJECTED);
        rejectionReasons.addAll(rejectionReasonService.listAll());
        Pageable pageable = PageRequest.of(0, 15, Sort.by("referenceId").descending());
        slices = nominationService.findSubmittedNominationsOnCostCentre(getActiveUser().getSid(), pageable);
        reroutedSlices = nominationService.findReroutedSubmittedNominationsOnCostCentre(getActiveUser().getSid(), pageable);
        reroutedApprovedAndRejected = nominationService.findReroutedCostCenterApprovedAndRejectedNominations(statuses, getActiveUser().getSid(), pageable);
        addCollections(slices.toList());
        updateCollections(reroutedSlices.toList());
        updateCollections(reroutedApprovedAndRejected.toList());

    }

    public void approve(Nomination nomination) {

        nomination.setUpdatedBy(getActiveUser().getSid());
        nomination.setUpdatedDate(new Date());

        if (!(nomination.getNominator().getEmployee().getEmployeeSid().equals(nomination.getNominator().getEmployee().getEmpDetails().getFinanceManagerSid()))) {
            //Cost Centre Approval in the event where by the Nominator is not a Finance Manager
            nomination.setCostCenterManagerSid(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            nomination.setCostCentreManagerStatus(CostCentreManagerStatus.APPROVED);
            nomination.setFinanceManagerStatus(FinanceManagerStatus.PENDING);

            nominationService.update(nomination);
            // send notification to the finance manager
            sendToFinanceManager(nomination);
        } else {
            // in event where nominator is a finance manager,Finance Manager Status is set to APPROVED upon  the  Cost Centre Manager Approval
            nomination.setCostCenterManagerSid(nomination.getNominator().getEmployee().getEmpDetails().getCostCentreManagerSid());
            nomination.setCostCentreManagerStatus(CostCentreManagerStatus.APPROVED);
            nomination.setFinanceManagerStatus(FinanceManagerStatus.APPROVED);
            nomination.setNominationStatus(NominationStatus.APPROVED);
            nominationService.update(nomination);
            // send notification to the Nominator
            sendEmailNotification(nomination);

            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                if (!empNomination.getEmployee().getEmpDetails().getManagerSid().equals(nomination.getNominator().getEmployee().getEmployeeSid())) {
                    sendDifferentLineManagerMail(empNomination.getEmployee(), nomination.getNominator().getEmployee().getEmpDetails().getFullnames(), nomination);
                }
            }

        }

        synchronizes(nomination);
        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            addInformationMessage("Nomination of ", empNomination.getEmployee().getEmpDetails().getFullnames(), " of SID ", empNomination.getEmployee().getEmployeeSid(), " was successfully approved");
        }

        setPanelTitleName("Review Nomination");
        indexing = false;
        this.reset().setList(true);
        // reset().setList(true);

    }

    public void reject(Nomination nomination) {
        if (nomination.getRejectionReason() == null) {
            addWarningMessage("Please select rejection reason");
            return;
        }
        nomination.setUpdatedBy(getActiveUser().getSid());
        nomination.setUpdatedDate(new Date());

        nomination.setCostCentreManagerStatus(CostCentreManagerStatus.REJECTED);
        nomination.setFinanceManagerStatus(FinanceManagerStatus.REJECTED);

        nomination.setNominationStatus(NominationStatus.REJECTED);
        nominationService.update(nomination);
        //send reject  email notifications to the line manager
        sendRejectEmailNotification(nomination);
        synchronizes(nomination);

        for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
            addInformationMessage("Nomination of ", empNomination.getEmployee().getEmpDetails().getFullnames(), " of SID ", empNomination.getEmployee().getEmployeeSid(), " was successfully rejected");
        }

        setPanelTitleName("Review Nomination");
        indexing = false;
        this.reset().setList(true);
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

// send to line manager
    public void sendEmailNotification(Nomination nomination) {
        if (nomination.getFinanceManagerStatus().equals(FinanceManagerStatus.APPROVED)) {

            emailNotificationRecipients.clear();
            //      String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmployeeSid());

             String destinationAddress = lDAPService.getUserEmailAddress(nomination.getNominator().getEmployee().getEmployeeSid());
        //    String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

            if (StringUtils.isEmpty(destinationAddress)) {
                addInformationMessage("Nominator's Destination Email Address is null");
                return;
            }
            emailNotificationRecipients.add(destinationAddress);

            StringBuilder builder = new StringBuilder();
            builder.append("Dear ");
            builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
            builder.append("<br /><br />");
            builder.append("The below nomination for an On-the-Spot Rewards and Recognition was approved on ");
            builder.append(convertStringToDate(nomination.getUpdatedDate()));
            builder.append("<br /><br />");
            builder.append("Once you receive this notification as confirmation of the finance manager's approval, you must navigate to the nomination and click on send to send a congratulations message to the nominee.");
            builder.append("<br /><br />");
            builder.append("You are also encouraged to announce this achievement in a public forum (for example during meetings or any suitable platform) to congratulate the nominee stating the reason/s for the achievement.");
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
            builder.append("This is a system generated notification; please contact your local HR Administrator for any inquiries.");
            builder.append("<br /><br />");
            builder.append("Employee Shared Services");

            if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
                addInformationMessage("Email notification has been sent to the nominator");
            }

        }
    }

    public void sendToFinanceManager(Nomination nomination) {
        if (nomination.getCostCentreManagerStatus().equals(CostCentreManagerStatus.APPROVED)) {

            emailNotificationRecipients.clear();
            //    String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getFinanceManagerSid());
                   String destinationAddress = lDAPService.getUserEmailAddress(nomination.getFinanceManagerSid());
          //  String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

            if (StringUtils.isEmpty(destinationAddress)) {
                addInformationMessage("Finance Manager Destination Email Address is null");
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
                addInformationMessage("Email notification has been sent to the Finance Manager");
            }
        }
    }

    public void sendDifferentLineManagerMail(Employee nominee, String nominatorFullNames, Nomination nomination) {

        emailNotificationRecipients.clear();

        //      String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nominee.getEmpDetails().getManagerSid());
          String destinationAddress = lDAPService.getUserEmailAddress(nominee.getEmpDetails().getManagerSid());
     //   String destinationAddress = lDAPService.getUserEmailAddress("s2026064");

        if (StringUtils.isEmpty(destinationAddress)) {
            addInformationMessage("Nominee's Manager Destination Email Address is null");
            return;
        }

        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear Manager ");
        builder.append(nominee.getEmpDetails().getManagerName());
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
        builder.append(nominee.getEmpDetails().getFullnames());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nominee.getEmpDetails().getPersonnelNum());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nominee.getEmployeeSid());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nominee.getDivisionName());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nominee.getSubDivision());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nominee.getEmpDetails().getCostCenterNumber());
        builder.append("</td>");
        builder.append("<td>");
        builder.append(nominee.getEmpDetails().getCostCenterName());
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
        builder.append("<td>Please action this nomination request, by clicking on the URL</td>");
        builder.append("<td>");
        builder.append("http://ecentral.sars.gov.za/onthespotrewards");
        builder.append("</td>");
        builder.append("</tr>");
        builder.append("</table>");
        builder.append("<br /><br />");
        builder.append("This is a system generated notification; please contact your local HR Administrator for any inquiries.");
        builder.append("<br /><br />");
        builder.append("Employee Shared Services");

        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Nomination Approval: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the nominee's manager");
        }
    }

    public void sendRejectEmailNotification(Nomination nomination) {

        emailNotificationRecipients.clear();

        // String destinationAddress = employeeInformationService.getEmployeeEmailAddress(nomination.getNominator().getEmployee().getEmployeeSid());
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

        builder.append("The below nomination for an On-the-Spot Rewards and Recognition was reject on ");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append(" with");
        builder.append("<br />");
        builder.append("rejection reason: ");
        builder.append(nomination.getRejectionReason().getDescription());
        builder.append("<br /><br />");
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
        builder.append("This is a system generated notification; please contact your local HR Administrator for any inquiries.");
        builder.append("<br /><br />");
        builder.append("Employee Shared Services");

        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition from: " + nomination.getNominator().getEmployee().getEmpDetails().getFullnames() + " " + nomination.getNominator().getEmployee().getEmployeeSid(), builder.toString())) {
            addInformationMessage("Email notification has been sent to the nominee's manager");
        }
    }

    public void synchronizes(Nomination nomination) {
        Iterator<Nomination> nomList = getCollections().iterator();
        while (nomList.hasNext()) {
            if (nomList.next().getId().equals(nomination.getId())) {
                nomList.remove();
            }
        }
    }

    public void nextNominations() {
        if (slices.hasNext()) {
            slices = nominationService.findSubmittedNominationsOnCostCentre(getActiveUser().getSid(), slices.nextPageable());
            addCollections(slices.toList());
        }
    }

    public void prevNominations() {
        if (slices.hasPrevious()) {
            slices = nominationService.findSubmittedNominationsOnCostCentre(getActiveUser().getSid(), slices.previousPageable());
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

    public void setNominations(List<Nomination> nominations) {
        this.nominations = nominations;
    }

    public boolean isIndexing() {
        return indexing;
    }

    public void setIndexing(boolean indexing) {
        this.indexing = indexing;
    }

    public List<RejectionReason> getRejectionReasons() {
        return rejectionReasons;
    }

    public void setRejectionReasons(List<RejectionReason> rejectionReasons) {
        this.rejectionReasons = rejectionReasons;
    }

    public Integer getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(Integer activeIndex) {
        this.activeIndex = activeIndex;
    }

    public User getAdministrator() {
        return administrator;
    }

    public void setAdministrator(User administrator) {
        this.administrator = administrator;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTot() {
        return tot;
    }

    public void setTot(int tot) {
        this.tot = tot;
    }

}
