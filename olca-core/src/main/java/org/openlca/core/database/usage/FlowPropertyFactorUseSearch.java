package org.openlca.core.database.usage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for the usage of flow property factors of a given flow in other
 * entities of the database.
 */
public class FlowPropertyFactorUseSearch implements
		IUseSearch<FlowPropertyFactor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase db;
	private Flow flow;

	public FlowPropertyFactorUseSearch(Flow flow, IDatabase db) {
		this.flow = flow;
		this.db = db;
	}

	@Override
	public List<BaseDescriptor> findUses(FlowPropertyFactor factor) {
		if (flow == null || factor == null || db == null)
			return Collections.emptyList();
		List<ProcessDescriptor> processes = findInProcesses(factor);
		List<ImpactMethodDescriptor> methods = findInMethods(factor);
		List<BaseDescriptor> descriptors = new ArrayList<>(processes.size()
				+ methods.size());
		descriptors.addAll(processes);
		descriptors.addAll(methods);
		return descriptors;
	}

	private List<ProcessDescriptor> findInProcesses(FlowPropertyFactor fac) {
		try {
			Set<Long> processIds = searchProcessIds(fac);
			if (processIds.isEmpty())
				return Collections.emptyList();
			ProcessDao dao = new ProcessDao(db);
			return dao.getDescriptors(processIds);
		} catch (Exception e) {
			log.error("Failed to search flow property factor in processes: "
					+ fac, e);
			return Collections.emptyList();
		}
	}

	private Set<Long> searchProcessIds(FlowPropertyFactor fac) throws Exception {
		String query = "select distinct f_owner from tbl_exchanges where "
				+ "f_flow = " + flow.getId()
				+ " and f_flow_property_factor = " + fac.getId();
		final Set<Long> ids = new TreeSet<>();
		NativeSql.on(db).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				ids.add(result.getLong(1));
				return true;
			}
		});
		return ids;
	}

	private List<ImpactMethodDescriptor> findInMethods(FlowPropertyFactor fac) {
		try {
			Set<Long> methodIds = searchMethodIds(fac);
			if (methodIds.isEmpty())
				return Collections.emptyList();
			ImpactMethodDao dao = new ImpactMethodDao(db);
			return dao.getDescriptors(methodIds);
		} catch (Exception e) {
			log.error("Failed to search processes with used flow", e);
			return Collections.emptyList();
		}
	}

	private Set<Long> searchMethodIds(FlowPropertyFactor fac) throws Exception {
		if (flow == null)
			return Collections.emptySet();
		Set<Long> catIds = searchImpactCategoryIds(fac);
		Set<Long> methodIds = new TreeSet<>();
		for (Long catId : catIds) {
			long id = getMethodId(catId);
			if (id != 0)
				methodIds.add(id);
		}
		return methodIds;
	}

	private long getMethodId(Long categoryId) throws Exception {
		String query = "select f_impact_method from tbl_impact_categories " +
				"where id = " + categoryId;
		final AtomicLong id = new AtomicLong(0);
		NativeSql.on(db).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				id.set(result.getLong(1));
				return true;
			}
		});
		return id.get();
	}

	private Set<Long> searchImpactCategoryIds(FlowPropertyFactor fac)
			throws Exception {
		String query = "select f_impact_category from tbl_impact_factors where "
				+ "f_flow = " + flow.getId()
				+ "and f_flow_property_factor = " + fac.getId();
		final Set<Long> ids = new TreeSet<>();
		NativeSql.on(db).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				ids.add(result.getLong(1));
				return true;
			}
		});
		return ids;
	}
}
