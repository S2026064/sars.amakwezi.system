/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "scheduler_tracker")
@Getter
@Setter
public class Tracker extends BaseEntity {

    public Tracker() {
    }

    public Tracker(Date schedulerStartDate, Date schedulerEndDate, int numberOfRecordsRetrieved, int numberOfRecordsUpdated) {
        this.schedulerStartDate = schedulerStartDate;
        this.schedulerEndDate = schedulerEndDate;
        this.numberOfRecordsRetrieved = numberOfRecordsRetrieved;
        this.numberOfRecordsUpdated = numberOfRecordsUpdated;
    }

    
    @Column(name = "scheduler_start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date schedulerStartDate;
    
    @Column(name = "scheduler_end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date schedulerEndDate;
    
    @Column(name = "records_retrieved")
    private int numberOfRecordsRetrieved;
    
    @Column(name = "records_updated")
    private int numberOfRecordsUpdated;
}
