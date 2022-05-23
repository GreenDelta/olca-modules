package org.openlca.core.database;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
		return getImpactCategories("m.id = " + methodId);
	}

	public List<ImpactDescriptor> getCategoryDescriptors(String methodId) {
		if (methodId == null)
			return Collections.emptyList();
		return getImpactCategories("m.ref_id = '" + methodId + "'");
	}

	private List<ImpactDescriptor> getImpactCategories(
			String whereClause) {
		var dao = new ImpactCategoryDao(db);
		var fields = Arrays.stream(dao.getDescriptorFields())
				.map(field -> "ic." + field)
				.reduce((field1, field2) -> field1 + ", " + field2)
				.get();
		String sql = "select "
				+ fields + " " +
				"from tbl_impact_categories ic     " +
				"inner join tbl_impact_links link  " +
				"on ic.id = link.f_impact_category " +
				"inner join tbl_impact_methods m   " +
				"on m.id = link.f_impact_method    " +
				"where " + whereClause;
		List<Object[]> records = selectAll(sql,
				dao.getDescriptorFields(),
				Collections.emptyList());
		return records.stream()
				.map(dao::createDescriptor)
				.collect(Collectors.toList());
	}

}
