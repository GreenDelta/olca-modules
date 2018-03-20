package org.openlca.io.maps;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ParseDouble;

/**
 * Maps (elementary) flow IDs of a data exchange format to the IDs of the
 * openLCA reference flows. The maps are directly stored as recourses in this
 * package in the following format:
 * 
 * "<external UUID>";"<openLCA ref. UUID>";<conversion factor>
 * 
 */
public class FlowMap {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private final HashMap<String, FlowMapEntry> map = new HashMap<>();
	private final HashMap<String, Flow> cache = new HashMap<>();

	public FlowMap(String map, IDatabase db) {
		init(map, db);
	}

	public FlowMap(Iterable<FlowMapEntry> entries) {
		if (entries != null) {
			entries.forEach(e -> {
				if (e != null) {
					map.put(e.externalFlowID, e);
				}
			});
		}
	}

	/** Caches a flow for the given id. */
	public void cache(String id, Flow flow) {
		cache.put(id, flow);
	}

	/**
	 * Returns the cached flow for the given ID, or null if no such flow is cached.
	 */
	public Flow getCached(String id) {
		return cache.get(id);
	}

	private void init(String map, IDatabase db) {
		log.trace("Initialize flow assignment map {}.", map);
		try {
			Maps.readAll(map, db, null, null, new ParseDouble())
					.forEach(this::createEntry);
		} catch (Exception e) {
			log.error("Cannot read mapping file", e);
		}
	}

	private void createEntry(List<Object> csvRow) {
		FlowMapEntry entry = new FlowMapEntry();
		entry.externalFlowID = Maps.getString(csvRow, 0);
		entry.referenceFlowID = Maps.getString(csvRow, 1);
		double factor = Maps.getDouble(csvRow, 2);
		entry.conversionFactor = factor;
		map.put(entry.externalFlowID, entry);
	}

	public FlowMapEntry getEntry(String externalID) {
		return map.get(externalID);
	}

	/** Get the conversion factor for the external key, default value is 1.0. */
	public double getFactor(String externalKey) {
		FlowMapEntry entry = getEntry(externalKey);
		if (entry == null)
			return 1.0;
		return entry.conversionFactor;
	}

}
