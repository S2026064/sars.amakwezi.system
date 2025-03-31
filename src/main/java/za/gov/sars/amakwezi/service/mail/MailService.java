/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service.mail;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import za.gov.sars.amakhwezi.common.CertificateDTO;

/**
 *
 * @author S2026987
 */
public class MailService {

    private final int MAIL_SERVER_PORT = 25;
    private final String SMTP_SERVER = "smtp.sars.gov.za";
    private final String SOURCE_ADDRESS = "noreplyonthespotrewards@sars.gov.za";

    public MailService() {

    }

    public boolean send(List<String> destinationAddress, String subject, String message) {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props);
        

        try {
            session.setDebug(false);
            Message msg = new MimeMessage(session);
            InternetAddress addressFrom = new InternetAddress(SOURCE_ADDRESS);
            msg.setFrom(addressFrom);
            setReceipients(destinationAddress, msg);
            msg.setSubject(subject);
            msg.setContent(message, "text/html");
            msg.setSentDate(new Date());
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    public boolean send(List<String> destinationAddress, String subject, String message, String attachmentPath) {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props);

        try {
            session.setDebug(false);
            Message msg = new MimeMessage(session);
            InternetAddress addressFrom = new InternetAddress(SOURCE_ADDRESS);
            msg.setFrom(addressFrom);
            setReceipients(destinationAddress, msg);
            msg.setSubject(subject);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Now set the actual message
            messageBodyPart.setText(message);
            // Create a multipar message
            Multipart multipart = new MimeMultipart();
            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Set the attachment file
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentPath);
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);
            msg.setSentDate(new Date());
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    private void setReceipients(List<String> to, Message message)
            throws AddressException, MessagingException {
        InternetAddress[] addressTo = new InternetAddress[to.size()];
        int index = 0;
        for (String toAddress : to) {
            if (toAddress != null && toAddress.length() > 0) {
                addressTo[index] = new InternetAddress(toAddress);
                index++;
            }
        }
        message.setRecipients(Message.RecipientType.TO, addressTo);
    }

    public boolean sendEmail(CertificateDTO certificateDTO) {
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            PdfReader reader = new PdfReader(certificateDTO.getInputCertificateFileName());

            PdfStamper stamper = new PdfStamper(reader, byteArrayOutputStream);

            AcroFields form = stamper.getAcroFields();

            form.setField("Name and Surname", certificateDTO.getNameAndSurname());

         

            form.setGenerateAppearances(true);

            stamper.setFormFlattening(true);

            stamper.close();

            reader.close();

            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_SERVER);
            props.put("mail.debug", "true");
            Session session = Session.getInstance(props);
            session.setDebug(false);

            Message msg = new MimeMessage(session);

            InternetAddress addressFrom = new InternetAddress(certificateDTO.getSourceEmailAddress());

            msg.setFrom(addressFrom);

            InternetAddress addressTo = new InternetAddress(certificateDTO.getDestinationEmailAddress());

            msg.setRecipient(Message.RecipientType.TO, addressTo);

            msg.setSentDate(new Date());

            msg.setSubject(certificateDTO.getEmailSubject());

            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setText(certificateDTO.getMessage());

            messageBodyPart.setContent(certificateDTO.getMessage(), "text/html");

            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(messageBodyPart);

            multipart.addBodyPart(addAttachment(byteArrayOutputStream.toByteArray(), "application/pdf", certificateDTO.getAttachmentFileName()));

            msg.setContent(multipart);

            Transport.send(msg);

//            Transport transport = session.getTransport("smtp");
//
//            transport.connect();
//
//            msg.saveChanges();
//
//            transport.sendMessage(msg, msg.getAllRecipients());
//
//            transport.close();
            return true;

        } catch (AddressException ex) {

            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);

        } catch (MessagingException | IOException | DocumentException ex) {

            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);

        }
        return false;
    }

    private BodyPart addAttachment(byte[] contents, String contentType, String fileName) throws MessagingException {

        BodyPart attachmentBodyPart = new MimeBodyPart();

        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(contents, contentType);

        attachmentBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        attachmentBodyPart.setFileName(fileName);

        return attachmentBodyPart;

    }
}
