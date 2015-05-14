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
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for the use of flows in other entities. Flows can be used in
 * processes and LCIA methods.
 */
public class FlowUseSearch implements IUseSearch<FlowDescriptor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public FlowUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<BaseDescriptor> findUses(Flow flow) {
		return findUses(Descriptors.toDescriptor(flow));
	}

	@Override
	public List<BaseDescriptor> findUses(FlowDescriptor flow) {
		if (flow == null)
			return Collections.emptyList();
		List<ProcessDescriptor> processes = findInProcesses(flow);
		List<ImpactMethodDescriptor> methods = findInMethods(flow);
		List<BaseDescriptor> descriptors = new ArrayList<>(processes.size()
				+ methods.size() + 2);
		descriptors.addAll(processes);
		descriptors.addAll(methods);
		return descriptors;
	}

	private List<ImpactMethodDescriptor> findInMethods(FlowDescriptor flow) {
		try {
			Set<Long> methodIds = searchMethodIds(flow);
			if (methodIds.isEmpty())
				return Collections.emptyList();
			ImpactMethodDao dao = new ImpactMethodDao(database);
			return dao.getDescriptors(methodIds);
		} catch (Exception e) {
			log.error("Failed to search processes with used flow", e);
			return Collections.emptyList();
		}
	}

	private Set<Long> searchMethodIds(FlowDescriptor flow) throws Exception {
		if (flow == null)
			return Collections.emptySet();
		Set<Long> catIds = searchImpactCategoryIds(flow);
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
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				id.set(result.getLong(1));
				return true;
			}
		});
		return id.get();
	}

	private Set<Long> searchImpactCategoryIds(FlowDescriptor flow)
			throws Exception {
		String query = "select f_impact_category from tbl_impact_factors where "
				+ "f_flow = " + flow.getId();
		final Set<Long> ids = new TreeSet<>();
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				ids.add(result.getLong(1));
				return true;
			}
		});
		return ids;
	}

	private List<ProcessDescriptor> findInProcesses(FlowDescriptor flow) {
		try {
			Set<Long> processIds = searchProcessIds(flow);
			if (processIds.isEmpty())
				return Collections.emptyList();
			ProcessDao dao = new ProcessDao(database);
			return dao.getDescriptors(processIds);
		} catch (Exception e) {
			log.error("Failed to search processes with used flow", e);
			return Collections.emptyList();
		}
	}

	private Set<Long> searchProcessIds(FlowDescriptor flow) throws Exception {
		if (flow == null)
			return Collections.emptySet();
		String query = "select distinct f_owner from tbl_exchanges where " +
				"f_flow = " + flow.getId();
		final Set<Long> ids = new TreeSet<>();
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				ids.add(result.getLong(1));
				return true;
			}
		});
		return ids;
	}
}
