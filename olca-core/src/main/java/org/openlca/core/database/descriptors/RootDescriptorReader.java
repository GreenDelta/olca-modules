package org.openlca.core.database.descriptors;

import org.openlca.core.database.RootEntityDao;

public record RootDescriptorReader(RootEntityDao<?, ?> dao)
		implements DescriptorReader {

	@Override
	public String query() {
		var table = dao.getEntityTable();
		return  """
					select
						d.id,
				   	d.ref_id,
				   	d.name,
				   	d.version,
				   	d.last_change,
				   	d.f_category,
				   	d.library,
				   	d.tags from
				""" + table + " d";
	}
}
