/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package za.gov.sars.amakhwezi.service.mail;

import java.util.List;
import javax.mail.Message;
import za.gov.sars.amakhwezi.common.CertificateDTO;

/**
 *
 * @author S2026080
 */
public interface MailServiceLocal {

    public boolean send(List<String> destinationAddress, String subject, String message);

    public boolean send(List<String> destinationAddress, String subject, String message, String attachmentPath);

    public boolean sendEmail(CertificateDTO certificateDTO);
}
