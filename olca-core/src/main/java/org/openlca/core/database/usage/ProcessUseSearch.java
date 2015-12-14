package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Searches for the use of processes in other entities. Processes can be used in
 * product systems.
 */
public class ProcessUseSearch extends BaseUseSearch<ProcessDescriptor> {

	public ProcessUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		return queryFor(ModelType.PRODUCT_SYSTEM, "f_product_system",
				"tbl_product_system_processes", ids, "f_process");
	}

}
