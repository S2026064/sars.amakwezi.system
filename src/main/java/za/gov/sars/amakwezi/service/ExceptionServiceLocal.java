/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.domain.MyException;

/**
 *
 * @author S2026987
 */
public interface ExceptionServiceLocal {

    MyException save(MyException exception);

 
    MyException findById(long id);

    MyException update(MyException exception);

    MyException deleteById(long id);

    List<MyException> listAll();

  
}
