package org.openlca.core.matrices;

import gnu.trove.map.hash.TLongObjectHashMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTypeIndex {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final TLongObjectHashMap<ProcessType> typeMap = new TLongObjectHashMap<>();
	private final TLongObjectHashMap<AllocationMethod> allocMap = new TLongObjectHashMap<>();

	public static ProcessTypeIndex create(IDatabase database) {
		ProcessTypeIndex index = new ProcessTypeIndex();
		index.init(database);
		return index;
	}

	private void init(IDatabase database) {
		log.trace("build process type index");
		try (Connection con = database.createConnection()) {
			String query = "select id, process_type, default_allocation_method "
					+ "from tbl_processes";
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next())
				fetchValues(result);
			result.close();
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

	public ProcessType getType(long processId) {
		return typeMap.get(processId);
	}

	/** Note that this method can return <code>null</code> */
	public AllocationMethod getDefaultAllocationMethod(long processId) {
		return allocMap.get(processId);
	}

	public int size() {
		return typeMap.size();
	}

	public long[] keys() {
		return typeMap.keys();
	}
}
