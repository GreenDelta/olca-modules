package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

/** The DAO class for impact assessment methods. */
public class ImpactMethodDao extends
        RootEntityDao<ImpactMethod, ImpactMethodDescriptor> {

	public ImpactMethodDao(IDatabase database) {
		super(ImpactMethod.class, ImpactMethodDescriptor.class, database);
	}

	public List<ImpactDescriptor> getCategoryDescriptors(long methodId) {
		return getImpactCategories("m.id = ?", methodId);
	}

	public List<ImpactDescriptor> getCategoryDescriptors(String methodId) {
		return methodId != null
				? getImpactCategories("m.ref_id = '?'", methodId)
				: Collections.emptyList();
	}

	private List<ImpactDescriptor> getImpactCategories(
			String condition, Object param) {
		var con = "inner join tbl_impact_links link  " +
				"on d.id = link.f_impact_category " +
				"inner join tbl_impact_methods m   " +
				"on m.id = link.f_impact_method    " +
				"where " + condition;
		return new ImpactCategoryDao(db)
				.queryDescriptors(con, param);
	}
}
