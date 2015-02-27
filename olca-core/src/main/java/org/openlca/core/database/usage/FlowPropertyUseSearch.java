package org.openlca.core.database.usage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.Query;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for the use of flow properties in other entities. Flow properties
 * can be used in flows (in flow property factors) and unit groups (as default
 * flow property).
 */
public class FlowPropertyUseSearch implements
		IUseSearch<FlowPropertyDescriptor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public FlowPropertyUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<BaseDescriptor> findUses(FlowProperty flowProperty) {
		return findUses(Descriptors.toDescriptor(flowProperty));
	}

	@Override
	public List<BaseDescriptor> findUses(FlowPropertyDescriptor prop) {
		if (prop == null)
			return Collections.emptyList();
		List<FlowDescriptor> flows = findInFlows(prop);
		List<BaseDescriptor> unitGroups = findInUnitGroups(prop);
		List<BaseDescriptor> results = new ArrayList<>(flows.size()
				+ unitGroups.size() + 2);
		results.addAll(flows);
		results.addAll(unitGroups);
		return results;
	}

	private List<FlowDescriptor> findInFlows(FlowPropertyDescriptor prop) {
		String sql = "select f_flow from tbl_flow_property_factors where " +
				"f_flow_property = " + prop.getId();
		try {
			final Set<Long> flowIds = new TreeSet<>();
			NativeSql.on(database).query(sql,
					new NativeSql.QueryResultHandler() {
						@Override
						public boolean nextResult(ResultSet result)
								throws SQLException {
							flowIds.add(result.getLong(1));
							return true;
						}
					});
			FlowDao dao = new FlowDao(database);
			return dao.getDescriptors(flowIds);
		} catch (Exception e) {
			log.error("Failed to search flow properties in flows", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> findInUnitGroups(FlowPropertyDescriptor prop) {
		String jpql = "select ug.id, ug.name, ug.description from UnitGroup ug "
				+ "where ug.defaultFlowProperty.id = :flowPropertyId";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql,
					Collections.singletonMap("flowPropertyId", prop.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				UnitGroupDescriptor d = new UnitGroupDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search flow properties in flows", e);
			return Collections.emptyList();
		}
	}

}
