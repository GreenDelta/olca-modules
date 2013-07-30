package org.openlca.core.database;

import org.openlca.core.model.ProductSystem;

public class ProductSystemDao extends CategorizedEnitityDao<ProductSystem> {

	public ProductSystemDao(IDatabase database) {
		super(ProductSystem.class, database);
	}

}
