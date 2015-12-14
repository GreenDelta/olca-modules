package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class SocialIndicatorReferenceSearch extends
		BaseReferenceSearch<SocialIndicatorDescriptor> {

	private final static Reference[] references = {
		new Reference(ModelType.CATEGORY, "f_category", true),
		new Reference(ModelType.FLOW_PROPERTY, "f_activity_quantity"),
		new Reference(ModelType.UNIT, "f_activity_unit") 
	};

	public SocialIndicatorReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<BaseDescriptor> mixed = findMixedReferences(
				"tbl_social_indicators", "id", ids, references);
		List<CategorizedDescriptor> results = filterCategorized(mixed);
		List<UnitDescriptor> units = filterUnits(mixed);
		results.addAll(findUnitGroups(units));
		return results;
	}

}
