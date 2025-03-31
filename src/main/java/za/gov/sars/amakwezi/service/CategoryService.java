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
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.persistence.CategoryRepository;

/**
 *
 * @author S2028389
 */
@Service
@Transactional
public class CategoryService implements CategoryServiceLocal {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                                "The requested id [" + id
                                + "] does not exist."));
    }

    @Override
    public Category update(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category deleteById(Long id) {
        Category category = findById(id);

        if (category != null) {
            categoryRepository.delete(category);
        }
        return category;
    }

    @Override
    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    @Override
    public boolean isExist(Category category) {
        return categoryRepository.findById(category.getId()) != null;
    }

}
