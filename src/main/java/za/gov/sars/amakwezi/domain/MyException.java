/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author S2026987
 */
@Entity
@Table(name = "exc_tracker")
@Getter
@Setter
public class MyException extends BaseEntity {

    public MyException() {
    }

    public MyException(Date excDate, String userSid, String trace) {
        this.excDate = excDate;
        this.userSid = userSid;
        this.trace = trace;
      
    }

  
    
    @Column(name = "exc_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date excDate;
    
    @Column(name = "user_sid")
    private String userSid;
    
    @Lob
    @Column(name = "trace")
    private String trace;
    
  
}
