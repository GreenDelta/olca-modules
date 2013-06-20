package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public interface IRootEntityDao<T extends RootEntity> extends IDao<T> {

	List<? extends BaseDescriptor> getDescriptors(Optional<Category> category);

}
