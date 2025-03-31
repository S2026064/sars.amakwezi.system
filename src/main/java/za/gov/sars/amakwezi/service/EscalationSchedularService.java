/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import za.gov.sars.amakhwezi.common.NominationType;
import za.gov.sars.amakhwezi.common.SarsValue;
import za.gov.sars.amakhwezi.domain.EmpNomination;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.domain.User;
import za.gov.sars.amakhwezi.service.mail.MailService;

/**
 *
 * @author S2026987
 */
@Configuration
@EnableScheduling
public class EscalationSchedularService {

    @Autowired
    private NominationServiceLocal nominationService;
    @Autowired
    private EmployeeInformationServiceLocal employeeInformationService;
    private User administrator;

    @Scheduled(cron = "0 0 0 * * ?")
    public void escalate() {
        System.out.println("Schedular is begining");
        Pageable pageable = PageRequest.of(0, 15);
        Slice<Nomination> escalatedNominations = nominationService.findEscalatedNominations(pageable);
        System.out.println(escalatedNominations.getNumberOfElements() + " has been collected");
        for (Nomination nomination : escalatedNominations.toList()) {
            if ((DateUtils.addDays(nomination.getCreatedDate(), 1).compareTo(new Date()) == 0) || (DateUtils.addDays(nomination.getUpdatedDate(), 1).compareTo(new Date()) == 0) || (DateUtils.addDays(nomination.getReroutedDate(), 1).compareTo(new Date()) == 0)) {
                sendEscalatedNominationMail(nomination);
            }
        }
        System.out.println("Escalation Schedular has finished");
    }

    public void sendEscalatedNominationMail(Nomination nomination) {
        MailService mailService = new MailService();
        List<String> emailNotificationRecipients = new ArrayList<>();
        // LDAPService lDAPService = new LDAPService();

        String msg = "Please note that the Nomination approval request for Employee ";

        emailNotificationRecipients.clear();
        emailNotificationRecipients.add(employeeInformationService.getEmployeeEmailAddress(getAdministrator().getEmployee().getEmployeeSid()));
//        emailNotificationRecipients.add(lDAPService.getUserEmailAddress("HCDAmakhwezi"));
//        String destinationAddress = employeeInformationService.getEmployeeEmailAddress("s1028785");
//        emailNotificationRecipients.add(destinationAddress);

        StringBuilder builder = new StringBuilder();
        builder.append("Dear Administrator ");
        builder.append("<br /><br />");
        builder.append(msg);
        builder.append(nomination.getNominator().getEmployee().getEmpDetails().getFullnames());
        builder.append(" has been escalated to you for further processing");
        builder.append("<br /><br />");
        builder.append("<strong><u>Nomination information</u></strong>");
        builder.append("<br /><br />");
        if (nomination.getNominationType().equals(NominationType.INDIVIDUAL)) {
            builder.append("Employee ");
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
                builder.append(" from ");
                builder.append(empNomination.getEmployee().getDivisionName());
                builder.append(", ");
                builder.append(empNomination.getEmployee().getSubDivision());
            }
            builder.append(" was nominated for On the spot Rewards and Recognition for the following category ");
        } else {
            builder.append(msg);
            builder.append(convertStringToDate(nomination.getUpdatedDate()));

            builder.append("<table border=1>");
            builder.append("<tr>");
            builder.append("<th>Employee id</th><th>Full Names</th><th>Personnel area</th><th>Personnel sub area</th>");
            builder.append("</tr>");
            for (EmpNomination empNomination : nomination.getEmployeeNominations()) {
                builder.append("<tr>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getPersonnelNum());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getEmpDetails().getFullnames());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getDivisionName());
                builder.append("</td>");
                builder.append("<td>");
                builder.append(empNomination.getEmployee().getSubDivision());
                builder.append("</td>");
                builder.append("</tr>");
            }
            builder.append("</table>");
            builder.append("<br /><br />");
            builder.append(" were nominated for On the spot Rewards and Recognition for the following category ");
        }

        builder.append(nomination.getCategory().getDescription());
        builder.append(" and value(s) ");
        for (SarsValue value : nomination.getSarsValues()) {
            builder.append(value.toString());
            builder.append(", ");
        }
        builder.append(" The nominated award ");
        builder.append(nomination.getContribution().getAmount());
        builder.append("<br /><br />");
        builder.append("Motivation ");
        builder.append(nomination.getMotivation());
        builder.append("<br /><br />");
        builder.append("The above nomination was escalated to you, because no action was taken by the approver or no approver was identified. The nomination was originally submitted on the ");
        builder.append(convertStringToDate(nomination.getUpdatedDate()));
        builder.append(" from Nominator ");
        builder.append(nomination.getNominator().getEmployee().getDivisionName());
        builder.append(", ");
        builder.append(nomination.getNominator().getEmployee().getSubDivision());
        builder.append(".");
        builder.append("<br />Please action this nomination request via your approval page ");
        builder.append("http://intranet/Pages/default.aspx");
        builder.append("<br /><br />");
        builder.append("This is a system generated notification; please contact your local HR Administrator for any inquiries.");
        builder.append("<br /><br />");
        builder.append("Employee Shared Services");

        if (mailService.send(emailNotificationRecipients, "On the spot Rewards and Recognition Escalated Nomination for Approval", builder.toString())) {
            System.out.println("Email notification has been sent to the Administrator");
        }

    }

    public String convertStringToDate(Date inputDateParam) {
        if (inputDateParam != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            return sdf.format(inputDateParam);
        } else {
            return "";
        }
    }

    public User getAdministrator() {
        return administrator;
    }

    public void setAdministrator(User administrator) {
        this.administrator = administrator;
    }

}
