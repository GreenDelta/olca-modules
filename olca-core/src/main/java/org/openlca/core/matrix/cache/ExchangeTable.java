package org.openlca.core.matrix.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;

public class ExchangeTable {

	private final IDatabase db;
	private final ConversionTable conversions;
	private final TLongObjectHashMap<FlowType> flowTypes;

	public ExchangeTable(IDatabase db) {
		this.db = db;
		conversions = ConversionTable.create(db);
		flowTypes = FlowTable.getTypes(db);
	}

	/**
	 * Calls the given function for each exchange of the processes in the given
	 * index.
	 */
	public void each(TechIndex techIndex, Consumer<CalcExchange> fn) {
		String sql = query();
		if (techIndex.size() < 1000) {
			// avoid full table scans in LCI databases
			sql += " where f_owner in " + CacheUtil.asSql(
				techIndex.getProcessIds());
		}
		try {
			NativeSql.on(db).query(sql, r -> {
				long owner = r.getLong(2);
				if (techIndex.isProvider(owner)) {
					try {
						fn.accept(next(owner, r));
					} catch (Exception e) {
						throw new RuntimeException("failed to read exchange row", e);
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

	public void each(Consumer<CalcExchange> fn) {
		try {
			NativeSql.on(db).query(query(), r -> {
				long owner = r.getLong(2);
				try {
					fn.accept(next(owner, r));
				} catch (Exception e) {
					throw new RuntimeException("failed to read exchange row", e);
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
			+ /* 17 */ " parameter3_value,"
			+ /* 18 */ " f_location"
			+ " FROM tbl_exchanges";
	}

	private CalcExchange next(long owner, ResultSet r) throws Exception {
		CalcExchange e = new CalcExchange();
		e.exchangeId = r.getLong(1);
		e.processId = owner;
		e.flowId = r.getLong(3);
		e.flowType = flowTypes.get(e.flowId);
		e.conversionFactor = getConversionFactor(r);
		e.amount = r.getDouble(6);
		e.formula = r.getString(7);
		e.isInput = r.getBoolean(8);
		e.isAvoided = r.getBoolean(9);
		e.defaultProviderId = r.getLong(10);
		e.locationId = r.getLong(18);

		// costs
		long currency = r.getLong(13);
		if (!r.wasNull()) {
			e.costValue = r.getDouble(11);
			e.costFormula = r.getString(12);
			e.currencyFactor = conversions.getCurrencyFactor(currency);
		}

		// uncertainties
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

	public List<Linkable> linkablesOf(Set<Long> processIds) {
		if (processIds == null || processIds.isEmpty())
			return Collections.emptyList();
		var query = Linkable.query + " where f_owner in ("
			+ processIds.stream().map(id -> Long.toString(id))
			.collect(Collectors.joining(","))
			+ ")";
		var linkables = new ArrayList<Linkable>();
		NativeSql.on(db).query(query, r -> {
			var linkable = Linkable.next(this, r);
			if (linkable != null) {
				linkables.add(linkable);
			}
			return true;
		});
		return linkables;
	}

	/**
	 * A {@code Linkable} describes a product input or waste output of a process
	 * that can be linked to a provider.
	 */
	public record Linkable(
		long exchangeId,
		long processId,
		long flowId,
		boolean isInput,
		boolean isAvoided,
		long providerId,
		long locationId,
		FlowType flowType
	) {

		private static final String query = "SELECT"
			+ /* 1 */ " id,"
			+ /* 2 */ " f_owner,"
			+ /* 3 */ " f_flow,"
			+ /* 4 */ " is_input,"
			+ /* 5 */ " avoided_product,"
			+ /* 6 */ " f_default_provider,"
			+ /* 7 */ " f_location"
			+ " FROM tbl_exchanges";

		private static Linkable next(ExchangeTable table, ResultSet rs) {
			try {
				long flowId = rs.getLong(3);
				var flowType = table.flowTypes.get(flowId);
				boolean isInput = rs.getBoolean(4);
				if (!isLinkable(flowType, isInput))
					return null;
				return new Linkable(
					rs.getLong(1),
					rs.getLong(2),
					flowId,
					isInput,
					rs.getBoolean(5),
					rs.getLong(6),
					rs.getLong(7),
					flowType
				);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		private static boolean isLinkable(FlowType type, boolean isInput) {
			return (type == FlowType.PRODUCT_FLOW && isInput)
				|| (type == FlowType.WASTE_FLOW && !isInput);
		}
	}

}
