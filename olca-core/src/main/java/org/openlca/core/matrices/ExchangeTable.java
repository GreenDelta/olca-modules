package org.openlca.core.matrices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.UncertaintyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeTable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<Long, List<CalcExchange>> exchanges = new HashMap<>();
	private ConversionTable conversionTable;

	public ExchangeTable(IDatabase database, Set<Long> processIds) {
		init(database, processIds);
	}

	private void init(IDatabase database, Set<Long> processIds) {
		if (processIds.isEmpty())
			return;
		conversionTable = ConversionTable.create(database);
		FlowTypeIndex flowTypes = new FlowTypeIndex(database);
		log.trace("create exchange table for {} processes", processIds.size());
		try (Connection con = database.createConnection()) {
			String query = "select * from tbl_exchanges where f_owner in "
					+ Indices.asSql(processIds);
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next()) {
				CalcExchange exchange = nextExchange(result, flowTypes);
				index(exchange);
			}
			result.close();
		} catch (Exception e) {
			log.error("failed to create exchange table", e);
		}
	}

	private void index(CalcExchange exchange) {
		Long processId = exchange.getProcessId();
		List<CalcExchange> list = exchanges.get(processId);
		if (list == null) {
			list = new ArrayList<>();
			exchanges.put(processId, list);
		}
		list.add(exchange);
	}

	private CalcExchange nextExchange(ResultSet r, FlowTypeIndex flowTypes)
			throws Exception {
		CalcExchange e = new CalcExchange();
		e.setProcessId(r.getLong("f_owner"));
		e.setAmount(r.getDouble("resulting_amount_value"));
		e.setAmountFormula(r.getString("resulting_amount_formula"));
		double factor = getConversionFactor(r);
		e.setConversionFactor(factor);
		e.setExchangeId(r.getLong("id"));
		e.setFlowId(r.getLong("f_flow"));
		e.setFlowType(flowTypes.getType(e.getFlowId()));
		e.setInput(r.getBoolean("is_input"));
		int uncertaintyType = r.getInt("distribution_type");
		if (!r.wasNull()) {
			e.setUncertaintyType(UncertaintyType.values()[uncertaintyType]);
			e.setParameter1(r.getDouble("parameter1_value"));
			e.setParameter2(r.getDouble("parameter2_value"));
			e.setParameter3(r.getDouble("parameter3_value"));
			e.setParameter1Formula(r.getString("parameter1_formula"));
			e.setParameter2Formula(r.getString("parameter2_formula"));
			e.setParameter3Formula(r.getString("parameter3_formula"));
		}
		return e;
	}

	private double getConversionFactor(ResultSet record) throws Exception {
		long propertyFactorId = record.getLong("f_flow_property_factor");
		double propertyFactor = conversionTable
				.getFlowPropertyFactor(propertyFactorId);
		long unitId = record.getLong("f_unit");
		double unitFactor = conversionTable.getUnitFactor(unitId);
		if (propertyFactor == 0)
			return 0;
		return unitFactor / propertyFactor;
	}

	public List<CalcExchange> getExchanges(long processId) {
		List<CalcExchange> list = exchanges.get(processId);
		if (list == null)
			return Collections.emptyList();
		return list;
	}

}
