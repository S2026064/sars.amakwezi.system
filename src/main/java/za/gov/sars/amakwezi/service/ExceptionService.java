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
import za.gov.sars.amakhwezi.domain.MyException;
import za.gov.sars.amakhwezi.persistence.ExceptionRepository;


/**
 *
 * @author S2026987
 */
@Service
@Transactional
public class ExceptionService implements ExceptionServiceLocal {

    @Autowired
    private ExceptionRepository exceptionRepository;

    @Override
    public MyException save(MyException exception) {
        return exceptionRepository.save(exception);
    }

  
    @Override
    public MyException findById(long id) {
        return exceptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }
    

    @Override
    public MyException update(MyException exception ) {
        return exceptionRepository.save(exception);
    }

    @Override
    public MyException deleteById(long id) {
        MyException exception = findById(id);
        if (exception != null) {
            exceptionRepository.delete(exception);
        }
        return exception;
    }

    @Override
    public List<MyException> listAll() {
        return exceptionRepository.findAll();
    }

  

 

}
