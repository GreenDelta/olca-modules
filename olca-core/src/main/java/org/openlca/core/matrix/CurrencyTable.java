package org.openlca.core.matrix;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * Contains conversion factors of currencies to the reference currency of a
 * database.
 */
public class CurrencyTable {

	private LongIndex index = new LongIndex();
	private TDoubleArrayList factors = new TDoubleArrayList();

	private CurrencyTable() {
	}

	public static CurrencyTable create(IDatabase db) {
		CurrencyTable table = new CurrencyTable();
		table.init(db);
		return table;
	}

	private void init(IDatabase db) {
		String query = "select id, conversion_factor from tbl_currencies";
		try {
			NativeSql.on(db).query(query, r -> {
				long id = r.getLong(1);
				double factor = r.getDouble(2);
				index.put(id);
				factors.add(factor);
				return true;
			});
		} catch (Exception e) {
			String m = "failed to load currency factors via: " + query;
			throw new RuntimeException(m, e);
		}
	}

	public double getFactor(long currencyId) {
		if (currencyId == 0)
			return 1.0;
		int idx = index.getIndex(currencyId);
		if (idx < 0 || idx >= factors.size())
			return 1.0;
		return factors.get(idx);
	}
}
