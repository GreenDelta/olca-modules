package org.openlca.core.matrix.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcCostEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

class CostEntryCache {

	public static LoadingCache<Long, List<CalcCostEntry>> create(
			IDatabase database) {
		return CacheBuilder.newBuilder().build(new EntryLoader(database));
	}

	private static class EntryLoader extends
			CacheLoader<Long, List<CalcCostEntry>> {

		private Logger log = LoggerFactory.getLogger(getClass());
		private IDatabase database;

		public EntryLoader(IDatabase database) {
			this.database = database;
		}

		@Override
		public List<CalcCostEntry> load(Long processId) throws Exception {
			log.trace("load cost entries for process {}", processId);
			try (Connection con = database.createConnection()) {
				String query = "select * from tbl_process_cost_entries where f_process = "
						+ processId;
				Statement stmt = con.createStatement();
				ResultSet result = stmt.executeQuery(query);
				List<CalcCostEntry> list = new ArrayList<>();
				while (result.next()) {
					CalcCostEntry costEntry = fetchEntry(result);
					list.add(costEntry);
				}
				result.close();
				stmt.close();
				return list;
			} catch (Exception e) {
				log.error("failed to load cost entries for process "
						+ processId, e);
				return Collections.emptyList();
			}
		}

		@Override
		public Map<Long, List<CalcCostEntry>> loadAll(
				Iterable<? extends Long> keys) throws Exception {
			log.trace("load process cost entries");
			try (Connection con = database.createConnection()) {
				String query = "select * from tbl_process_cost_entries where f_process in "
						+ CacheUtil.asSql(keys);
				Statement stmt = con.createStatement();
				ResultSet result = stmt.executeQuery(query);
				Map<Long, List<CalcCostEntry>> map = new HashMap<>();
				while (result.next()) {
					CalcCostEntry costEntry = fetchEntry(result);
					CacheUtil.addListEntry(map, costEntry,
							costEntry.getProcessId());
				}
				result.close();
				stmt.close();
				return map;
			} catch (Exception e) {
				log.error("failed to load cost entries from database", e);
				return Collections.emptyMap();
			}
		}

		private CalcCostEntry fetchEntry(ResultSet result) throws Exception {
			CalcCostEntry entry = new CalcCostEntry();
			entry.setAmount(result.getDouble("amount"));
			entry.setCostCategoryId(result.getLong("f_cost_category"));
			entry.setExchangeId(result.getLong("f_exchange"));
			entry.setProcessId(result.getLong("f_process"));
			return entry;
		}

	}

}
