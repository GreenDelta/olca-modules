package org.openlca.io.simapro.csv.input;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.MapFactor;
import org.openlca.io.maps.Maps;
import org.openlca.io.maps.OlcaFlowMapEntry;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

class ImportMap {

	private Logger log = LoggerFactory.getLogger(getClass());

	private HashMap<String, MapFactor<OlcaFlowMapEntry>> flowMap = new HashMap<>();

	private ImportMap() {
	}

	public static ImportMap load(IDatabase database) {
		ImportMap map = new ImportMap();
		map.init(database);
		return map;
	}

	private void init(IDatabase database) {
		log.trace("init import map");
		try {
			List<List<Object>> rows = Maps.readAll(Maps.SP_FLOW_IMPORT,
					database, getCellProcessors());
			log.trace("read {} flow mappings", rows.size());
			for (List<Object> row : rows)
				putFlowMapping(row);
		} catch (Exception e) {
			log.error("failed to init import map", e);
		}
	}

	private CellProcessor[] getCellProcessors() {
		CellProcessor notNull = new NotNull();
		CellProcessor optional = new Optional();
		CellProcessor number = new ParseDouble();
		return new CellProcessor[] { notNull, // 0: sp name
				notNull, // 1: sp compartment
				optional, // 2: sp sub-compartment
				notNull, // 3: sp unit
				notNull, // 4: olca flow id
				optional, // 5: olcd flow name
				notNull, // 6: olca property id
				optional, // 7: olca property name
				notNull, // 8: olca unit id
				optional, // 9: olca unit name
				number // 10: conversion factor
		};
	}

	private void putFlowMapping(List<Object> row) {
		if (row == null)
			return;
		String key = getKey(row);
		OlcaFlowMapEntry olcaFlow = getOlcaFlowEntry(row);
		double factor = Maps.getDouble(row, 10);
		MapFactor<OlcaFlowMapEntry> val = new MapFactor<>(olcaFlow, factor);
		flowMap.put(key, val);
	}

	private OlcaFlowMapEntry getOlcaFlowEntry(List<Object> row) {
		OlcaFlowMapEntry entry = new OlcaFlowMapEntry();
		entry.setFlowId(Maps.getString(row, 4));
		entry.setRefPropertyId(Maps.getString(row, 6));
		entry.setRefUnitId(Maps.getString(row, 8));
		return entry;
	}

	private String getKey(List<Object> row) {
		String name = Maps.getString(row, 0);
		String compartment = Maps.getString(row, 1);
		String subCompartment = Maps.getString(row, 2);
		String unit = Maps.getString(row, 3);
		return KeyGen.get(name, compartment, subCompartment, unit);
	}

	public MapFactor<OlcaFlowMapEntry> getFlowEntry(String key) {
		return flowMap.get(key);
	}
}
