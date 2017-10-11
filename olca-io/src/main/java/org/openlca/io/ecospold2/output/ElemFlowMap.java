package org.openlca.io.ecospold2.output;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ift.CellProcessor;

import spold2.Compartment;
import spold2.ElementaryExchange;

class ElemFlowMap {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<String, ExportRecord> map = new HashMap<>();

	public ElemFlowMap(IDatabase database) {
		initMap(database);
	}

	private void initMap(IDatabase database) {
		try {
			CellProcessor[] processors = getCellProcessors();
			List<List<Object>> rows = Maps.readAll(Maps.ES2_FLOW_EXPORT,
					database, processors);
			for (List<Object> row : rows) {
				String refId = Maps.getString(row, 0);
				ExportRecord record = createRecord(row);
				map.put(refId, record);
			}
		} catch (Exception e) {
			log.error("failed to initialize flow export map", e);
		}
	}

	private CellProcessor[] getCellProcessors() {
		CellProcessor string = null;
		CellProcessor optional = new Optional();
		CellProcessor number = new ParseDouble();
		//@formatter:off
		return new CellProcessor[]{
				string,     // 0: openLCA ID
				optional,   // 1: openLCA name
				string,     // 2: openLCA property ID
				optional,   // 3: openLCA property name
				string,     // 4: openLCA unit ID
				optional,   // 5: openLCA unit name
				string,     // 6: ecoinvent ID
				string,     // 7: ecoinvent name
				string,     // 8: ecoinvent unit ID
				string,     // 9: ecoinvent unit name
				string,     // 10: ecoinvent sub-compartment ID
				string,     // 11: ecoinvent compartment name
				string,     // 12: ecoinvent sub-compartment name
				number      // 13: conversion factor
				};
		//@formatter:on
	}

	private ExportRecord createRecord(List<Object> row) {
		ExportRecord record = new ExportRecord();
		record.olcaPropertyId = Maps.getString(row, 2);
		record.olcaUnitId = Maps.getString(row, 4);
		record.id = Maps.getString(row, 6);
		record.name = Maps.getString(row, 7);
		record.unitId = Maps.getString(row, 8);
		record.unitName = Maps.getString(row, 9);
		record.subCompartmentId = Maps.getString(row, 10);
		record.compartment = Maps.getString(row, 11);
		record.subCompartment = Maps.getString(row, 12);
		record.conversionFactor = Maps.getDouble(row, 13);
		return record;
	}

	public ElementaryExchange apply(Exchange olca) {
		if (olca == null || olca.flow == null) {
			log.warn("could not map exchange {}, exchange or flow is null",
					olca);
			return null;
		}
		ExportRecord record = map.get(olca.flow.getRefId());
		if (record == null || !isValid(record, olca)) {
			log.warn(
					"elementary flow {} cannot be mapped to an ecoinvent flow",
					olca.flow);
			return null;
		}
		return createExchange(olca, record);
	}

	private boolean isValid(ExportRecord record, Exchange olca) {
		return record != null
				&& olca != null
				&& olca.flowPropertyFactor != null
				&& olca.flowPropertyFactor.getFlowProperty() != null
				&& Objects.equals(record.olcaPropertyId, olca.flowPropertyFactor.getFlowProperty().getRefId())
				&& olca.unit != null
				&& Objects.equals(record.olcaUnitId, olca.unit.getRefId());
	}

	private ElementaryExchange createExchange(Exchange olca, ExportRecord record) {
		ElementaryExchange exchange = new ElementaryExchange();
		if (olca.isInput)
			exchange.inputGroup = 4;
		else
			exchange.outputGroup = 4;
		exchange.id = new UUID(olca.getId(), 0L).toString();
		exchange.flowId = record.id;
		exchange.name = Strings.cut(record.name, 120);
		exchange.compartment = createCompartment(record);
		exchange.unit = record.unitName;
		exchange.unitId = record.unitId;
		exchange.amount = record.conversionFactor * olca.amount;
		if (olca.amountFormula != null) {
			exchange.mathematicalRelation = record.conversionFactor + " * ("
			+ olca.amountFormula + ")";
		}
		// TODO: convert uncertainty information
		return exchange;
	}

	private Compartment createCompartment(ExportRecord record) {
		Compartment compartment = new Compartment();
		compartment.id = record.subCompartmentId;
		compartment.compartment = record.compartment;
		compartment.subCompartment = record.subCompartment;
		return compartment;
	}

	private class ExportRecord {

		String id;
		String name;
		String unitId;
		String unitName;
		String subCompartmentId;
		String subCompartment;
		String compartment;

		String olcaPropertyId;
		String olcaUnitId;
		double conversionFactor;

	}

}
