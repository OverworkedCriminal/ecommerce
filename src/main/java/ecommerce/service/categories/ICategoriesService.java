package ecommerce.service.categories;

import ecommerce.dto.categories.InCategory;
import ecommerce.dto.categories.InCategoryPatch;
import ecommerce.dto.categories.OutCategory;

public interface ICategoriesService {

    OutCategory postCategory(InCategory category);

    void patchCategory(long id, InCategoryPatch category);

    void deleteCategory(long id);

}
