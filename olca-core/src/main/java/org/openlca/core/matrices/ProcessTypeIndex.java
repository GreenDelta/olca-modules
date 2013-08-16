package org.openlca.core.matrices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTypeIndex {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<Long, ProcessType> map = new HashMap<>();

	public ProcessTypeIndex(IDatabase database) {
		init(database);
	}

	private void init(IDatabase database) {
		log.trace("build process type index");
		try (Connection con = database.createConnection()) {
			String query = "select id, process_type from tbl_processes";
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next())
				fetchProcessType(result);
			result.close();
		} catch (Exception e) {
			log.error("failed to build process type index", e);
		}
	}

	private void fetchProcessType(ResultSet result) throws Exception {
		long id = result.getLong("id");
		String typeString = result.getString("process_type");
		ProcessType type = Objects.equals(ProcessType.LCI_RESULT.name(),
				typeString) ? ProcessType.LCI_RESULT : ProcessType.UNIT_PROCESS;
		map.put(id, type);
	}

	public ProcessType getType(long processId) {
		return map.get(processId);
	}

	public int size() {
		return map.size();
	}

	public List<Long> keys() {
		return new ArrayList<>(map.keySet());
	}
}
