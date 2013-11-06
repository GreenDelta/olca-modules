package org.openlca.io.maps;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import org.openlca.core.model.Flow;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private HashMap<String, FlowMapEntry> map;
	private HashMap<String, Flow> cache = new HashMap<>();

	public FlowMap(MapType mapType) {
		init(mapType);
	}

	/** Caches a flow for the given id. */
	public void cache(String id, Flow flow) {
		cache.put(id, flow);
	}

	/**
	 * Returns the cached flow for the given ID, or null if no such flow is
	 * cached.
	 */
	public Flow getCached(String id) {
		return cache.get(id);
	}

	private void init(MapType mapType) {
		log.trace("Initialize flow assignment map {}.", mapType);
		map = new HashMap<>();
		try (InputStream is = this.getClass().getResourceAsStream(
				mapFileName(mapType))) {
			String[] lines = Strings.readLines(is);
			parse(lines);
		} catch (Exception e) {
			log.error("Cannot read mapping file", e);
		}
	}

	private String mapFileName(MapType mapType) {
		switch (mapType) {
		case ECOSPOLD_FLOW:
			return "ecospold_flow_map.csv";
		case ECOSPOLD_2_FLOW:
			return "ecospold_2_flow_map.csv";
		case ILCD_FLOW:
			return "ilcd_flow_map.csv";
		default:
			return "ecospold_flow_map.csv";
		}
	}

	private void parse(String[] lines) {
		for (String line : lines) {
			if (line == null || !line.contains("\""))
				continue;
			String[] args = line.split("\"");
			createEntry(args);
		}
	}

	private void createEntry(String[] args) {
		if (args.length < 4)
			return;
		FlowMapEntry entry = new FlowMapEntry();
		entry.setExternalFlowKey(args[1].toLowerCase());
		entry.setOpenlcaFlowKey(args[3]);
		try {
			double factor = Double.parseDouble(args[4].replace(";", ""));
			entry.setConversionFactor(factor);
			map.put(entry.getExternalFlowKey(), entry);
		} catch (Exception e) {
			log.error("Invalid number format in mapping " + "file args="
					+ Arrays.asList(args), e);
		}
	}

	public FlowMapEntry getEntry(String externalKey) {
		if (map != null && externalKey != null)
			return map.get(externalKey.toLowerCase());
		return null;
	}

	/** Get the conversion factor for the external key, default value is 1.0. */
	public double getFactor(String externalKey) {
		FlowMapEntry entry = getEntry(externalKey);
		if (entry == null)
			return 1.0;
		return entry.getConversionFactor();
	}

}
