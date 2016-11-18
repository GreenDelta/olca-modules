package org.openlca.core.matrix.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

public class ProcessTable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final TLongObjectHashMap<ProcessType> typeMap = new TLongObjectHashMap<>();
	private final TLongObjectHashMap<AllocationMethod> allocMap = new TLongObjectHashMap<>();

	/**
	 * Maps IDs of product flows to process IDs that have this product as
	 * output: product-id -> provider-process-id. We need this when we build a
	 * product system automatically.
	 */
	private final TLongObjectHashMap<TLongArrayList> productMap = new TLongObjectHashMap<>();

	public static ProcessTable create(IDatabase db, FlowTypeTable flowTypes) {
		ProcessTable table = new ProcessTable(db, flowTypes);
		return table;
	}

	private ProcessTable(IDatabase db, FlowTypeTable flowTypes) {
		log.trace("build process index table");
		initTypeAndAllocation(db);
		initProductMap(db, flowTypes);
	}

	private void initProductMap(IDatabase db, FlowTypeTable flowTypes) {
		log.trace("load product->process map");
		String query = "select f_owner, f_flow from tbl_exchanges"
				+ " where is_input = 0";
		try {
			NativeSql.on(db).query(query, r -> {
				long processId = r.getLong(1);
				long flowId = r.getLong(2);
				if (flowTypes.get(flowId) == FlowType.PRODUCT_FLOW)
					indexProvider(flowId, processId);
				return true;
			});
			log.trace("{} products mapped", productMap.size());
		} catch (Exception e) {
			log.error("failed to load process products", e);
		}
	}

	private void indexProvider(long productId, long processId) {
		TLongArrayList list = productMap.get(productId);
		if (list == null) {
			list = new TLongArrayList();
			productMap.put(productId, list);
		}
		list.add(processId);
	}

	private void initTypeAndAllocation(IDatabase database) {
		log.trace("index process and allocation types");
		try (Connection con = database.createConnection()) {
			String query = "select id, process_type, default_allocation_method "
					+ "from tbl_processes";
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next())
				fetchValues(result);
			result.close();
			log.trace("{} processes indexed", typeMap.size());
		} catch (Exception e) {
			log.error("failed to build process type index", e);
		}
	}

	private void fetchValues(ResultSet result) throws Exception {
		long id = result.getLong("id");
		String typeString = result.getString("process_type");
		ProcessType type = Objects.equals(ProcessType.LCI_RESULT.name(),
				typeString) ? ProcessType.LCI_RESULT : ProcessType.UNIT_PROCESS;
		typeMap.put(id, type);
		String allocString = result.getString("default_allocation_method");
		if (allocString != null) {
			AllocationMethod m = AllocationMethod.valueOf(allocString);
			allocMap.put(id, m);
		}
	}

	/** Returns the process type for the given process-ID. */
	public ProcessType getType(long processId) {
		return typeMap.get(processId);
	}

	/** Note that this method can return <code>null</code> */
	public AllocationMethod getDefaultAllocationMethod(long processId) {
		return allocMap.get(processId);
	}

	/**
	 * Returns the list of process IDs that have the product flow with the given
	 * ID as output.
	 */
	public long[] getProductProviders(long productId) {
		TLongArrayList list = productMap.get(productId);
		if (list == null)
			return new long[0];
		return list.toArray();
	}

	/** Gets all process products of the database. */
	public List<LongPair> getProcessProducts() {
		List<LongPair> list = new ArrayList<>();
		TLongObjectIterator<TLongArrayList> it = productMap.iterator();
		while (it.hasNext()) {
			it.advance();
			long productId = it.key();
			for (long processId : it.value().toArray()) {
				list.add(LongPair.of(processId, productId));
			}
		}
		return list;
	}
}
