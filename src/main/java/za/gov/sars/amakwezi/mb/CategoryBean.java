/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import za.gov.sars.amakhwezi.domain.Category;
import za.gov.sars.amakhwezi.domain.Nomination;
import za.gov.sars.amakhwezi.service.CategoryServiceLocal;
import za.gov.sars.amakhwezi.service.NominationServiceLocal;

/**
 *
 * @author S2028389
 */
@ManagedBean
@ViewScoped
public class CategoryBean extends BaseBean<Category> {

    @Autowired
    private CategoryServiceLocal categoryService;
    @Autowired
    private NominationServiceLocal nominationService;

    List<Category> categories = new ArrayList<>();

    private Category category;

    private static final Logger LOG = Logger.getLogger(CategoryBean.class.getName());

    @PostConstruct
    public void init() {
        setPanelTitleName("Categories");
        this.reset().setList(true);
        addCollections(categoryService.listAll());
    }

    public void addOrUpdate(Category category) {
        this.reset().setAdd(true);
        if (category != null) {
            setPanelTitleName("Update Category");
            category.setUpdatedBy(getActiveUser().getFullName());
            category.setUpdatedDate(new Date());
        } else {
            setPanelTitleName("Add Category");
            category = new Category();
            category.setCreatedBy(getActiveUser().getLoggedOnUserFullName());
            category.setCreatedDate(new Date());
            addCollection(category);
        }
        addEntity(category);
    }

    public void save(Category cat) {
        try {
            if (cat.getId() != null) {
                categoryService.update(cat);
            } else {
                categoryService.save(cat);
            }
            reset().setList(true);
            addInformationMessage("Category saved successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Categories");
    }

    public void delete(Category cat) {

        if (nominationService.isCategoryUsed(cat)) {
            addWarningMessage("This Category cannot be deleted because it is being used by other Nominations");
            return;
        }
        
        try {
            categoryService.deleteById(cat.getId());
            synchronize(cat);
            addInformationMessage("Category successfully deleted");
            reset().setList(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        setPanelTitleName("Categories");
    }

    public void synchronize(Category cat) {
        Iterator<Category> categoryList = getCollections().iterator();
        while (categoryList.hasNext()) {
            if (categoryList.next().getId().equals(cat.getId())) {
                categoryList.remove();
            }
        }
    }

    public void cancel(Category cat) {
        if (cat.getId() == null) {
            if (getCollections().contains(cat)) {
                getCollections().remove(cat);
            }
        }
        this.reset().setList(true);
        setPanelTitleName("Categories");
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
