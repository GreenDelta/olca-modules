package org.openlca.core.database.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A look-up for the type of a process. */
class ProcessTypeTable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Map<String, ProcessType> typeMap = new HashMap<>();
	private IDatabase database;

	public ProcessTypeTable(IDatabase database) {
		this.database = database;
	}

	/**
	 * Loads the types for the process IDs into the look-up table. Does nothing
	 * if a process ID is already contained in this table. Thus there is no
	 * significant performance lost if the processes are already contained in
	 * this table.
	 */
	public void load(Collection<String> processIds) {
		List<String> fetchIds = new ArrayList<>();
		for (String processId : processIds) {
			if (typeMap.containsKey(processId))
				continue;
			fetchIds.add(processId);
		}
		if (fetchIds.isEmpty())
			return;
		realLoad(fetchIds);
	}

	private void realLoad(List<String> fetchIds) {
		String query = "select id, processtype from tbl_processes where id in ";
		query += asSql(fetchIds);
		log.trace("load process types for {} processes", fetchIds);
		try (Connection con = database.createConnection();
				ResultSet rs = con.createStatement().executeQuery(query)) {
			while (rs.next()) {
				String processId = rs.getString("id");
				int type = rs.getInt("processtype");
				ProcessType processType = type == 0 ? ProcessType.LCI_Result
						: ProcessType.UnitProcess;
				typeMap.put(processId, processType);
			}
		} catch (Exception e) {
			log.error("Failed to load process types", e);
		}
	}

	private String asSql(List<String> ids) {
		StringBuilder b = new StringBuilder();
		b.append('(');
		for (int i = 0; i < ids.size(); i++) {
			b.append('\'').append(ids.get(i)).append('\'');
			if (i < (ids.size() - 1))
				b.append(',');
		}
		b.append(')');
		return b.toString();
	}

	public ProcessType getType(String processId) {
		return typeMap.get(processId);
	}

}
