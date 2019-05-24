package org.openlca.io.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ParseDouble;

public class FlowMap extends BaseDescriptor {

	/** Description of the source system. */
	public BaseDescriptor source;

	/** Description of the target system. */
	public BaseDescriptor target;

	public final List<FlowMapEntry> entries = new ArrayList<>();

	private Map<String, FlowMapEntry> index;

	/**
	 * Get the mapping entry for the source flow with the given ID.
	 */
	public FlowMapEntry getEntry(String sourceFlowID) {
		if (index == null) {
			index = new HashMap<>();
			for (FlowMapEntry e : entries) {
				String sid = e.sourceFlowID();
				if (sid != null) {
					index.put(sid, e);
				}
			}
		}
		return index.get(sourceFlowID);
	}

	/**
	 * Reads the flow map with the given identifier from this package or the
	 * given database.
	 * 
	 * @deprecated we should remove implicit mappings that are loaded from this
	 *             package and also loading mappings from the database
	 */
	@Deprecated
	public static FlowMap of(String map, IDatabase db) {
		Logger log = LoggerFactory.getLogger(FlowMap.class);
		log.trace("Initialize flow assignment map {}.", map);
		FlowMap m = new FlowMap();
		m.name = map;
		try {
			HashSet<String> dbIDs = new HashSet<>();
			new FlowDao(db).getDescriptors().stream()
					.forEach(d -> dbIDs.add(d.refId));
			Maps.readAll(map, db, null, null, new ParseDouble()).forEach(r -> {
				String sourceID = Maps.getString(r, 0);
				String targetID = Maps.getString(r, 1);
				if (targetID == null || !dbIDs.contains(targetID))
					return;
				FlowMapEntry e = new FlowMapEntry();
				e.sourceFlow = new FlowRef();
				e.sourceFlow.flow = new FlowDescriptor();
				e.sourceFlow.flow.refId = sourceID;
				e.targetFlow = new FlowRef();
				e.targetFlow.flow = new FlowDescriptor();
				e.targetFlow.flow.refId = targetID;
				e.factor = Maps.getDouble(r, 2);
				m.entries.add(e);
			});
		} catch (Exception e) {
			log.error("Error while reading mapping file", e);
		}
		return m;
	}

}
