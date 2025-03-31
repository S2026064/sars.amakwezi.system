/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.List;
import za.gov.sars.amakhwezi.domain.Category;

/**
 *
 * @author S2028389
 */
public interface CategoryServiceLocal {

    Category save(Category category);

    Category findById(Long id);

    Category update(Category category);

    Category deleteById(Long id);

    List<Category> listAll();
    
    boolean isExist(Category category);
}

