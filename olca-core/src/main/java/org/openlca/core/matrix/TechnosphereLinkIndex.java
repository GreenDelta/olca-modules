package org.openlca.core.matrix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechnosphereLinkIndex {

	private Logger log = LoggerFactory.getLogger(getClass());

	/** Maps process IDs to inputs of these processes. */
	private HashMap<Long, List<TechnosphereLink>> productInputs = new HashMap<>();

	/** Maps flow IDs to outputs with these flows. */
	private HashMap<Long, List<TechnosphereLink>> productOutputs = new HashMap<>();

	public TechnosphereLinkIndex(IDatabase database) {
		build(database);
	}

	private void build(IDatabase database) {
		List<TechnosphereLink> links = loadLinks(database);
		for (TechnosphereLink link : links) {
			// TODO: handle waste flows !
			if (link.isInput())
				add(link.getProcessId(), link, productInputs);
			else
				add(link.getFlowId(), link, productOutputs);
		}
	}

	private List<TechnosphereLink> loadLinks(IDatabase database) {
		log.trace("load technosphere links");
		//@formatter:off
		String query = 
			"select e.f_owner, e.f_flow, e.resulting_amount_value, "
          + "       e.is_input, e.f_default_provider, f.flow_type "
          + "from   tbl_exchanges e inner join tbl_flows f on e.f_flow = f.id "
          + "where  f.flow_type <> 'ELEMENTARY_FLOW'";
		//@formatter:on		
		try (Connection con = database.createConnection()) {
			List<TechnosphereLink> links = new ArrayList<>();
			ResultSet results = con.createStatement().executeQuery(query);
			while (results.next()) {
				TechnosphereLink link = fetchLink(results);
				if (link != null)
					links.add(link);
			}
			return links;
		} catch (Exception e) {
			log.error("failed to load technosphere links", e);
			return Collections.emptyList();
		}
	}

	private TechnosphereLink fetchLink(ResultSet rs) {
		try {
			TechnosphereLink link = new TechnosphereLink();
			link.setAmount(rs.getDouble("resulting_amount_value"));
			link.setDefaultProviderId(rs.getLong("f_default_provider"));
			link.setFlowId(rs.getLong("f_flow"));
			link.setInput(rs.getBoolean("is_input"));
			link.setProcessId(rs.getLong("f_owner"));
			String flowType = rs.getString("flow_type");
			link.setWaste(FlowType.WASTE_FLOW.name().equals(flowType));
			return link;
		} catch (Exception e) {
			log.error("failed to create technospher link", e);
			return null;
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
