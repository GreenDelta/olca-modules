package org.openlca.core.matrix.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

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

	private IDatabase database;

	// primitive maps for the factors: no-key: 0, default conversion factors: 1
	private TLongDoubleHashMap unitFactors = new TLongDoubleHashMap(
			Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0L, 1d);
	private TLongDoubleHashMap propertyFactors = new TLongDoubleHashMap(
			Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0L, 1d);
	private TLongDoubleHashMap currencyFactors = new TLongDoubleHashMap(
			Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0L, 1d);

	public static ConversionTable create(IDatabase db) {
		ConversionTable table = new ConversionTable(db);
		table.init();
		return table;
	}

	private ConversionTable(IDatabase db) {
		this.database = db;
	}

	// TODO: when we remove the matrix cache, we can also remove this
	// reload function.
	@Deprecated
	public void reload() {
		unitFactors.clear();
		propertyFactors.clear();
		currencyFactors.clear();
		init();
	}

	private void init() {
		try (Connection con = database.createConnection()) {
			loadFactors(con, "tbl_units", unitFactors);
			loadFactors(con, "tbl_flow_property_factors", propertyFactors);
			loadFactors(con, "tbl_currencies", currencyFactors);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to initialize conversion table", e);
		}
	}

	private void loadFactors(
			Connection con,
			String table,
			TLongDoubleHashMap map) throws Exception {
		Statement statement = con.createStatement();
		String query = "select id, conversion_factor from " + table;
		ResultSet set = statement.executeQuery(query);
		while (set.next()) {
			long id = set.getLong(1);
			double factor = set.getDouble(2);
			map.put(id, factor);
		}
		statement.close();
		set.close();
	}

	/**
	 * Get the conversion factor of the unit with the given ID to the reference
	 * unit of the unit group to which this unit belongs.
	 */
	public double getUnitFactor(long unitId) {
		return unitFactors.get(unitId);
	}

	/**
	 * Get the conversion factor of the given flow property factor to the
	 * reference flow property factor of a flow.
	 */
	public double getPropertyFactor(long flowPropertyFactorId) {
		return propertyFactors.get(flowPropertyFactorId);
	}

	/**
	 * Get the conversion factor of the currency with the given ID to the
	 * reference currency in the database.
	 */
	public double getCurrencyFactor(long currencyID) {
		return currencyFactors.get(currencyID);
	}
}
