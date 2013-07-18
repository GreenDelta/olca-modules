package org.openlca.core.indices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechnosphereLinkIndex {

	/** Maps process IDs to inputs of these processes. */
	private HashMap<Long, List<TechnosphereLink>> productInputs = new HashMap<>();

	/** Maps flow IDs to outputs with these flows. */
	private HashMap<Long, List<TechnosphereLink>> productOutputs = new HashMap<>();

	public TechnosphereLinkIndex(IDatabase database) {
		build(database);
	}

	private void build(IDatabase database) {
		try (TechnosphereLinkTable table = new TechnosphereLinkTable(database)) {
			List<TechnosphereLink> links = table.getAll();
			for (TechnosphereLink link : links) {
				// TODO: handle waste flows !
				if (link.isInput())
					add(link.getProcessId(), link, productInputs);
				else
					add(link.getFlowId(), link, productOutputs);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to build technosphere link index", e);
		}
	}

	private void add(long key, TechnosphereLink link,
			HashMap<Long, List<TechnosphereLink>> map) {
		List<TechnosphereLink> links = map.get(key);
		if (links == null) {
			links = new ArrayList<>();
			map.put(key, links);
		}
		links.add(link);
	}

	/**
	 * Returns the links that represent an input of the process with the given
	 * ID.
	 * 
	 * @param processId
	 *            the ID of the process
	 */
	public List<TechnosphereLink> getProductInputs(long processId) {
		List<TechnosphereLink> list = productInputs.get(processId);
		if (list == null)
			return Collections.emptyList();
		return list;
	}

	/**
	 * Returns the links that represent an output with the flow with the given
	 * ID.
	 * 
	 * @param flowId
	 *            the ID of the flow.
	 */
	public List<TechnosphereLink> getProductOutputs(long flowId) {
		List<TechnosphereLink> list = productOutputs.get(flowId);
		if (list == null)
			return Collections.emptyList();
		return list;
	}

}
