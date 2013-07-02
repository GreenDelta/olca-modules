package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Category;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public interface IRootEntityDao<T extends CategorizedEntity> extends IDao<T> {

	List<? extends BaseDescriptor> getDescriptors(Optional<Category> category);

}
