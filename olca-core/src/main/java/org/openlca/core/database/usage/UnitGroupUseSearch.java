package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitGroupUseSearch implements IUseSearch<UnitGroupDescriptor> {

	private IDatabase database;
	private Logger log = LoggerFactory.getLogger(getClass());

	UnitGroupUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(UnitGroupDescriptor group) {
		if (group == null)
			return Collections.emptyList();
		String jpql = "select fp.id, fp.name, fp.description from FlowProperty fp "
				+ "where fp.unitGroup.id = :unitGroupId";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql,
					Collections.singletonMap("unitGroupId", group.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				FlowPropertyDescriptor d = new FlowPropertyDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search for unit group usages", e);
			return Collections.emptyList();
		}
	}
}
