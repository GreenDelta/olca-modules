package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CurrencyDescriptor;

/**
 * Searches for the use of currencies in other entities. Currencies can be used
 * in processes.
 */
public class SocialIndicatorUseSearch extends BaseUseSearch<CurrencyDescriptor> {

	public SocialIndicatorUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		return queryFor(ModelType.PROCESS, "f_process", "tbl_social_aspects",
				ids, "f_indicator");
	}
}
