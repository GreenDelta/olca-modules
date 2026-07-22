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
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.UncertaintyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ExchangeCache {

	public static LoadingCache<Long, List<CalcExchange>> create(IDatabase db) {
		var conversions = ConversionTable.create(db);
		var flows = FlowTable.create(db);
		var loader = new ExchangeLoader(db, conversions, flows);
		return CacheBuilder.newBuilder().build(loader);
	}

	private static class ExchangeLoader extends
			CacheLoader<Long, List<CalcExchange>> {

		private final Logger log = LoggerFactory.getLogger(getClass());
		private final IDatabase db;
		private final ConversionTable conversions;
		private final FlowTable flows;

		public ExchangeLoader(
			IDatabase db, ConversionTable conversions, FlowTable flows
		) {
			this.db = db;
			this.conversions = conversions;
			this.flows = flows;
		}

		@Override
		public List<CalcExchange> load(Long key) {
			if (key == null)
				return Collections.emptyList();
			log.trace("fetch exchanges for key {}", key);
			String query = "select * from tbl_exchanges where f_owner = " + key;
			try (Connection con = db.createConnection()) {
				Statement statement = con.createStatement();
				ResultSet result = statement.executeQuery(query);
				ArrayList<CalcExchange> exchanges = new ArrayList<>();
				while (result.next()) {
					CalcExchange exchange = nextExchange(result);
					exchanges.add(exchange);
				}
				result.close();
				statement.close();
				log.trace("fetched {} exchanges", exchanges.size());
				return exchanges;
			} catch (Exception e) {
				log.error("failed to fetch exchange vector", e);
				return Collections.emptyList();
			}
		}

		@Override
		public Map<Long, List<CalcExchange>> loadAll(
				Iterable<? extends Long> keys) {
			log.trace("fetch exchanges for multiple keys");
			try (Connection con = db.createConnection()) {
				String query = "select * from tbl_exchanges where f_owner in "
						+ CacheUtil.asSql(keys);
				Statement statement = con.createStatement();
				HashMap<Long, List<CalcExchange>> map = new HashMap<>();
				ResultSet result = statement.executeQuery(query);
				while (result.next()) {
					CalcExchange e = nextExchange(result);
					map.computeIfAbsent(e.processId, _id -> new ArrayList<>())
							.add(e);
				}
				result.close();
				statement.close();
				log.trace("{} lists loaded", map.size());
				return map;
			} catch (Exception e) {
				log.error("failed to fetch exchange vectors", e);
				return Collections.emptyMap();
			}
		}

		private CalcExchange nextExchange(ResultSet r) throws Exception {
			CalcExchange e = new CalcExchange();
			e.processId = r.getLong("f_owner");
			e.amount = r.getDouble("resulting_amount_value");
			e.formula = r.getString("resulting_amount_formula");
			e.conversionFactor = conversions.forExchange(
				r.getLong("f_unit"),
				r.getLong("f_flow_property_factor"));
			e.exchangeId = r.getLong("id");
			e.flowId = r.getLong("f_flow");
			e.flowType = flows.type(e.flowId);
			e.isInput = r.getBoolean("is_input");
			e.defaultProviderId = r.getLong("f_default_provider");
			e.isAvoided = r.getBoolean("avoided_product");

			// costs
			long currency = r.getLong("f_currency");
			if (!r.wasNull()) {
				e.costValue = r.getDouble("cost_value");
				e.costFormula = r.getString("cost_formula");
				e.currencyFactor = conversions.forCurrency(currency);
			}

			// uncertainties
			int uncertaintyType = r.getInt("distribution_type");
			if (!r.wasNull()) {
				e.uncertaintyType = UncertaintyType.values()[uncertaintyType];
				e.parameter1 = r.getDouble("parameter1_value");
				e.parameter2 = r.getDouble("parameter2_value");
				e.parameter3 = r.getDouble("parameter3_value");
			}
			return e;
		}
	}

}
