package org.openlca.core.database;

import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemDao extends
		CategorizedEntityDao<ProductSystem, ProductSystemDescriptor> {

	public ProductSystemDao(IDatabase database) {
		super(ProductSystem.class, ProductSystemDescriptor.class, database);
	}

}
