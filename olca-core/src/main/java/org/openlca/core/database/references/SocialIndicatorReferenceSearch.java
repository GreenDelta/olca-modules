package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;

public class SocialIndicatorReferenceSearch extends
		BaseReferenceSearch<SocialIndicatorDescriptor> {

	private final static Ref[] references = {
		new Ref(Category.class, "category", "f_category", true),
		new Ref(FlowProperty.class, "activityQuantity", "f_activity_quantity", true),
		new Ref(Unit.class, "activityUnit", "f_activity_unit", true) 
	};

	public SocialIndicatorReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, SocialIndicator.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		return findReferences("tbl_social_indicators", "id", ids, references);
	}

}
