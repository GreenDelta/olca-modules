package org.openlca.core.database;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.ProductSystem;

public class ProductSystemDao extends CategorizedEnitityDao<ProductSystem> {

	public ProductSystemDao(EntityManagerFactory emf) {
		super(ProductSystem.class, emf);
	}

}
