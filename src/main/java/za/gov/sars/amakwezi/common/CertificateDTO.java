/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.common;

import lombok.Getter;
import lombok.Setter;
import za.gov.sars.amakhwezi.domain.BaseEntity;

/**
 *
 * @author S2026080
 */
@Getter
@Setter
public class CertificateDTO  extends BaseEntity {
    private String sourceEmailAddress;
    private String destinationEmailAddress;
    private String emailSubject;
    private String message;
    private String attachmentFileName;
    private String inputCertificateFileName;
    private String nameAndSurname;
   
}
