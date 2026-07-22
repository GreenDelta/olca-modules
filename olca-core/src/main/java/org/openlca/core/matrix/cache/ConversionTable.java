package org.openlca.core.matrix.cache;

import java.sql.Connection;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

/// A table that contains the conversion factors for units, flow property
/// factors, and currencies.
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

	double forUnit(long unitId) {
		return units.get(unitId);
	}

	double forPropertyFactor(long flowPropertyFactorId) {
		return properties.get(flowPropertyFactorId);
	}

	public double forCurrency(long currencyId) {
		return currencies.get(currencyId);
	}

	public double forExchange(long unitId, long propertyFactorId) {
		double uf = units.get(unitId);
		double pf = properties.get(propertyFactorId);
		return pf != 0 ? uf / pf : 0;
	}

	public double forCharacterization(long unitId, long propertyFactorId) {
		double uf = units.get(unitId);
		double pf = properties.get(propertyFactorId);
		return uf != 0 ? pf / uf : 0;
	}
}
