package ecommerce.service.categories;

import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.OutCategory;

public interface ICategoriesService {

    OutCategory postCategory(InCategory category);

    void putCategory(long id, InCategory category);

    void deleteCategory(long id);

}
