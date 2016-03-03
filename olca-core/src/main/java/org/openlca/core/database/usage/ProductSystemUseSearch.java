package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemUseSearch extends
		BaseUseSearch<ProductSystemDescriptor> {

	public ProductSystemUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		return queryFor(ModelType.PROJECT, "f_project", "tbl_project_variants",
				ids, "f_product_system");
	}

}
