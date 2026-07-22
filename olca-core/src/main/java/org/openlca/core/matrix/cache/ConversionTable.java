package org.openlca.core.matrix.cache;

import java.sql.Connection;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

/**
 * A table that contains the conversion factors for units and flow property
 * factors. (Note: A flow can have multiple flow properties and a flow property
 * factor describes the flow specific conversion of a flow property to the
 * reference flow property of a flow).
 */
public class ConversionTable {

	private final IDatabase db;

	private final TLongDoubleHashMap units = newMap();
	private final TLongDoubleHashMap properties = newMap();
	private final TLongDoubleHashMap currencies = newMap();

	public static ConversionTable create(IDatabase db) {
		ConversionTable table = new ConversionTable(db);
		table.init();
		return table;
	}

	private static TLongDoubleHashMap newMap() {
		return new TLongDoubleHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			0L, // non-entry key
			1d  // default conversion factor is 1
		);
	}

	private ConversionTable(IDatabase db) {
		this.db = db;
	}


	private void init() {
		try (Connection con = db.createConnection()) {
			loadFactors(con, "tbl_units", units);
			loadFactors(con, "tbl_flow_property_factors", properties);
			loadFactors(con, "tbl_currencies", currencies);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to initialize conversion table", e);
		}
	}

	private void loadFactors(
		Connection con, String table, TLongDoubleHashMap map
	) throws Exception {
		var qry = "select id, conversion_factor from " + table;
		try (var stmt = con.createStatement();
		     var rs = stmt.executeQuery(qry)) {
			while (rs.next()) {
				long id = rs.getLong(1);
				double factor = rs.getDouble(2);
				map.put(id, factor);
			}
		}
	}

	/**
	 * Get the conversion factor of the unit with the given ID to the reference
	 * unit of the unit group to which this unit belongs.
	 */
	public double getUnitFactor(long unitId) {
		return units.get(unitId);
	}

	/**
	 * Get the conversion factor of the given flow property factor to the
	 * reference flow property factor of a flow.
	 */
	public double getPropertyFactor(long flowPropertyFactorId) {
		return properties.get(flowPropertyFactorId);
	}

	/**
	 * Get the conversion factor of the currency with the given ID to the
	 * reference currency in the database.
	 */
	public double getCurrencyFactor(long currencyID) {
		return currencies.get(currencyID);
	}

	public double forExchange(long unitId, long propertyFactorId) {
		double uf = units.get(unitId);
		double pf = properties.get(propertyFactorId);
		return pf != 0 ? uf / pf : 0;
	}
}
