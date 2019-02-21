package org.openlca.core.matrix.cache;

import java.sql.ResultSet;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.UncertaintyType;

public class ExchangeTable {

	private final IDatabase db;
	private final ConversionTable conversions;
	// TODO we should replace this with just a id->type map
	private final FlowTable flows;

	public ExchangeTable(IDatabase db) {
		this.db = db;
		conversions = ConversionTable.create(db);
		flows = FlowTable.create(db);
	}

	/**
	 * Calls the given function for each exchange of the processes in the given
	 * index.
	 */
	public void each(TechIndex techIndex, Consumer<CalcExchange> fn) {
		try {
			NativeSql.on(db).query(query(), r -> {
				long owner = r.getLong(2);
				if (techIndex.isProvider(owner)) {
					try {
						fn.accept(next(owner, r));
					} catch (Exception e) {
						throw new RuntimeException(
								"failed to read exchange row", e);
					}
				}
				return true;
			});
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("failed to query exchange table", e);
		}
	}

	private static String query() {
		return "SELECT"
				+ /* 1 */ " id,"
				+ /* 2 */ " f_owner,"
				+ /* 3 */ " f_flow,"
				+ /* 4 */ " f_flow_property_factor,"
				+ /* 5 */ " f_unit,"
				+ /* 6 */ " resulting_amount_value,"
				+ /* 7 */ " resulting_amount_formula,"
				+ /* 8 */ " is_input,"
				+ /* 9 */ " avoided_product,"
				+ /* 10 */ " f_default_provider,"
				+ /* 11 */ " cost_value,"
				+ /* 12 */ " cost_formula,"
				+ /* 13 */ " f_currency,"
				+ /* 14 */ " distribution_type,"
				+ /* 15 */ " parameter1_value,"
				+ /* 16 */ " parameter2_value,"
				+ /* 17 */ " parameter3_value"
				+ " FROM tbl_exchanges";
	}

	private CalcExchange next(long owner, ResultSet r) throws Exception {
		CalcExchange e = new CalcExchange();
		e.exchangeId = r.getLong(1);
		e.processId = owner;
		e.flowId = r.getLong(3);
		e.flowType = flows.type(e.flowId);
		double factor = getConversionFactor(r);
		e.conversionFactor = factor;
		e.amount = r.getDouble(6);
		e.amountFormula = r.getString(7);
		e.isInput = r.getBoolean(8);
		e.isAvoided = r.getBoolean(9);
		e.defaultProviderId = r.getLong(10);
		e.costValue = r.getDouble(11);
		e.costFormula = r.getString(12);
		e.currency = r.getLong(13);
		int uncertaintyType = r.getInt(14);
		if (!r.wasNull()) {
			e.uncertaintyType = UncertaintyType.values()[uncertaintyType];
			e.parameter1 = r.getDouble(15);
			e.parameter2 = r.getDouble(16);
			e.parameter3 = r.getDouble(17);
		}
		return e;
	}

	private double getConversionFactor(ResultSet record) throws Exception {
		long propertyFactorId = record.getLong(4);
		double propertyFactor = conversions.getPropertyFactor(propertyFactorId);
		long unitId = record.getLong(5);
		double unitFactor = conversions.getUnitFactor(unitId);
		if (propertyFactor == 0)
			return 0;
		return unitFactor / propertyFactor;
	}

}
