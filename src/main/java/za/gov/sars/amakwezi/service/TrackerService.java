/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.gov.sars.amakhwezi.domain.Tracker;
import za.gov.sars.amakhwezi.persistence.TrackerRepository;

/**
 *
 * @author S2026987
 */
@Service
@Transactional
public class TrackerService implements TrackerServiceLocal {

    @Autowired
    private TrackerRepository trackerRepository;

    @Override
    public Tracker save(Tracker tracker) {
        return trackerRepository.save(tracker);
    }

  
    @Override
    public Tracker findById(long id) {
        return trackerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }
    

    @Override
    public Tracker update(Tracker tracker ) {
        return trackerRepository.save(tracker);
    }

    @Override
    public Tracker deleteById(long id) {
        Tracker tracker = findById(id);
        if (tracker != null) {
            trackerRepository.delete(tracker);
        }
        return tracker;
    }

    @Override
    public List<Tracker> listAll() {
        return trackerRepository.findAll();
    }

  

 

}
