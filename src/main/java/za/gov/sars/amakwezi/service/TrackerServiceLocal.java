/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.domain.Tracker;

/**
 *
 * @author S2026987
 */
public interface TrackerServiceLocal {

    Tracker save(Tracker tracker);

 
    Tracker findById(long id);

    Tracker update(Tracker tracker);

    Tracker deleteById(long id);

    List<Tracker> listAll();

  
}
