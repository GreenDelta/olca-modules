package org.openlca.core.database;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactCategoryDao extends
		RootEntityDao<ImpactCategory, ImpactCategoryDescriptor> {

	public ImpactCategoryDao(IDatabase database) {
		super(ImpactCategory.class, ImpactCategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name",
				"description", "version", "last_change",
				"tags", "reference_unit" };
	}

	@Override
	protected ImpactCategoryDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ImpactCategoryDescriptor d = super.createDescriptor(queryResult);
		d.tags = (String) queryResult[6];
		d.referenceUnit = (String) queryResult[7];
		return d;
	}

	/**
	 * Get the descriptors of the LCIA categories of the LCIA method with the
	 * given ID.
	 */
	public List<ImpactCategoryDescriptor> getMethodImpacts(long methodID) {
		String sql = "SELECT id, ref_id, name, description, "
				+ "version, last_change, reference_unit FROM "
				+ "tbl_impact_categories where f_impact_method = "
				+ methodID;
		ArrayList<ImpactCategoryDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(database).query(sql, r -> {
				ImpactCategoryDescriptor d = new ImpactCategoryDescriptor();
				d.id = r.getLong(1);
				d.refId = r.getString(2);
				d.name = r.getString(3);
				d.description = r.getString(4);
				d.version = r.getLong(5);
				d.lastChange = r.getLong(6);
				d.referenceUnit = r.getString(7);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("Failed to query LCIA categories"
					+ " for methodID=" + methodID, e);
		}
		return list;
	}
}
