package org.openlca.core.database;

import static org.openlca.core.model.ModelType.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;

import gnu.trove.set.hash.TLongHashSet;

/**
 * This is a utility class for searching transitive dependencies of root
 * entities in a database. The returned lists will also contain the references
 * of the root entities for which the search was done and all their transitive
 * dependencies: the datasets that are referenced transitively in the respective
 * object graphs.
 */
public class TransDeps {

	private final IDatabase db;
	private final NativeSql sql;
	private final EnumMap<ModelType, TLongHashSet> refs;

	private TransDeps(List<RootDescriptor> ds, IDatabase db) {
		this.db = db;
		this.sql = NativeSql.on(db);
		this.refs = new EnumMap<>(ModelType.class);
		for (var d : ds) {
			put(d.type, d.id);
		}
	}

	public static List<RootDescriptor> of(RootDescriptor d, IDatabase db) {
		return d == null
				? Collections.emptyList()
				: of(List.of(d), db);
	}

	public static List<RootDescriptor> of(List<RootDescriptor> ds, IDatabase db) {
		if (ds == null || db == null)
			return Collections.emptyList();
		return new TransDeps(ds, db)
				.scanTables()
				.loadDescriptors();
	}

	private TransDeps scanTables() {
		// the order is important here!
		scanProjectTables();
		scanEpdTables();
		scanResultTables();
		scanImpactMethodTables();
		scanImpactCategoryTables();
		scanSocialIndicatorTables();
		scanProductSystemTables();
		scanProcessTables();
		scanFlowTables();
		scanFlowPropertyTables();
		scanUnitGroupTables();
		scanCurrencyTables();
		scanDQSystemTables();
		return this;
	}

	private void scanEpdTables() {
		if (!refs.containsKey(ModelType.EPD))
			return;

		// tbl_epds
		sql.query("""
			select
			  id,
			  f_flow,
			  f_flow_property,
			  f_manufacturer,
			  f_verifier,
			  f_pcr,
			  f_program_operator
			from tbl_epds
			""", r -> {
			if (has(EPD, r, 1)) {
				put(FLOW, r, 2);
				put(FLOW_PROPERTY, r, 3);
				put(ACTOR, r, 4);
				put(ACTOR, r, 5);
				put(SOURCE, r, 6);
				put(ACTOR, r, 7);
			}
			return true;
		});

		// tbl_epd_modules
		sql.query("""
			select
			  f_epd,
			  f_result
			from tbl_epd_modules
			""", r -> {
			if (has(EPD, r, 1)) {
				put(RESULT, r, 2);
			}
			return true;
		});
	}

	private void scanProjectTables() {

		// tbl_projects
		sql.query("""
			select
			  id,
			  f_impact_method
			from tbl_projects
			""", r -> {
			if (has(PROJECT, r, 1)) {
				put(IMPACT_METHOD, r, 2);
			}
			return true;
		});

		// tbl_project_variants
		sql.query("""
			select
			  f_project,
			  f_product_system
			from tbl_project_variants
			""", r -> {
			if (has(PROJECT, r, 1)) {
				put(PRODUCT_SYSTEM, r, 2);
			}
			return true;
		});
	}

	private void scanImpactMethodTables() {
		// tbl_impact_methods
		sql.query("""
			select
			  id,
				f_source,
			from tbl_impact_methods
			""", r -> {
			if (has(IMPACT_METHOD, r, 1)) {
				put(SOURCE, r, 2);
			}
			return true;
		});

		// tbl_impact_links
		sql.query("""
			select
			  f_impact_method,
			  f_impact_category
			from tbl_impact_links
			""", r -> {
			if (has(IMPACT_METHOD, r, 1)) {
				put(IMPACT_CATEGORY, r, 2);
			}
			return true;
		});
	}

	private void scanImpactCategoryTables() {
		// tbl_impact_categories
		sql.query("""
			select
			  id,
			  f_source
			from tbl_impact_categories
			""", r -> {
			if (has(IMPACT_CATEGORY, r, 1)) {
				put(SOURCE, r, 2);
			}
			return true;
		});

		// tbl_impact_factors
		sql.query("""
			select
			  f_impact_category,
			  f_flow,
				f_location
			from tbl_impact_factors
			""", r -> {
			if (has(IMPACT_CATEGORY, r, 1)) {
				put(FLOW, r, 2);
				put(LOCATION, r, 3);
			}
			return true;
		});
	}

	private void scanProductSystemTables() {

		var providers = new TLongHashSet();

		// tbl_product_systems
		sql.query("""
			select
			  id,
			  f_reference_process
			from tbl_product_systems
			""", r -> {
			if (has(PRODUCT_SYSTEM, r, 1)) {
				var p = r.getLong(2);
				put(PROCESS, p);
				providers.add(p);
			}
			return true;
		});

		// tbl_process_links
		sql.query("""
			select
			  f_product_system,
			  provider_type,
			  f_provider,
			  f_process
			from tbl_process_links
			""", r -> {
			if (has(PRODUCT_SYSTEM, r, 1)) {

				var providerType = switch (r.getInt(2)) {
					case 1 -> PRODUCT_SYSTEM;
					case 2 -> RESULT;
					default -> PROCESS;
				};
				var provider = r.getLong(3);
				put(providerType, provider);
				providers.add(provider);

				var process = r.getLong(4);
				put(PROCESS, process);
				providers.add(process);
			}
			return true;
		});

		// tbl_product_system_processes
		sql.query("""
			select
			  f_product_system,
			  f_process
			from tbl_product_system_processes
			""", r -> {
			if (!has(PRODUCT_SYSTEM, r, 1))
				return true;
			// only needed for unlinked providers; then we need to
			// know the type
			var p = r.getLong(2);
			if (providers.contains(p))
				return true;

			var d = db.getDescriptor(Process.class, p);
			if (d != null) {
				put(PROCESS, p);
				return true;
			}
			d = db.getDescriptor(ProductSystem.class, p);
			if (d != null) {
				put(PRODUCT_SYSTEM, p);
				return true;
			}
			put(RESULT, p);
			return true;
		});
	}

	private void scanProcessTables() {

		// tbl_exchanges
		sql.query("""
			select
			  f_owner,
			  f_flow,
				f_default_provider,
			  f_location,
			  f_currency
			from tbl_exchanges
			""", r -> {
			if (has(PROCESS, r, 1)) {
				put(FLOW, r, 2);
				put(PROCESS, r, 3);
				put(LOCATION, r, 4);
				put(CURRENCY, r, 5);
			}
			return true;
		});

		// tbl_social_aspects
		sql.query("""
			select
			  f_process,
			  f_indicator,
			  f_source
			from tbl_social_aspects
			""", r -> {
			if (has(PROCESS, r, 1)) {
				put(SOCIAL_INDICATOR, r, 2);
				put(SOURCE, r, 3);
			}
			return true;
		});

		/*
		// tbl_reviews
		sql.query("""
			select
			  proc.id
			  rev.f_owner,
			f_report,
			from tbl_reviews
			""", r -> {
			if (has(_, r, 1)) {
				// put ...
			}
			return true;
		});
   */
	}

	private void scanFlowTables() {
		// TODO: implement
	}

	private void scanFlowPropertyTables() {
		// tbl_flow_properties
		sql.query("""
			select
			  id,
			  f_unit_group
			from tbl_flow_properties
			""", r -> {
			if (has(FLOW_PROPERTY, r, 1)) {
				put(UNIT_GROUP, r, 2);
			}
			return true;
		});
	}

	private void scanUnitGroupTables() {
		// tbl_unit_groups
		sql.query("""
			select
			  id,
			  f_default_flow_property
			from tbl_unit_groups
			""", r -> {
			if (has(UNIT_GROUP, r, 1)) {
				put(FLOW_PROPERTY, r, 2);
			}
			return true;
		});
	}

	private void scanSocialIndicatorTables() {
		// tbl_social_indicators
		sql.query("""
			select
			  id,
				f_activity_quantity
			from tbl_social_indicators
			""", r -> {
			if (has(SOCIAL_INDICATOR, r, 1)) {
				put(FLOW_PROPERTY, r, 2);
			}
			return true;
		});
	}

	private void scanCurrencyTables() {
		// tbl_currencies
		sql.query("""
			select
			  id,
				f_reference_currency
			from tbl_currencies
			""", r -> {
			if (has(CURRENCY, r, 1)) {
				put(CURRENCY, r, 2);
			}
			return true;
		});
	}

	private void scanDQSystemTables() {
		// tbl_dq_systems
		sql.query("""
			select
			  id,
				f_source
			from tbl_dq_systems
			""", r -> {
			if (has(DQ_SYSTEM, r, 1)) {
				put(SOURCE, r, 2);
			}
			return true;
		});
	}

	private void scanResultTables() {
		// tbl_results
		sql.query("""
			select
			  id,
				f_product_system,
			  f_impact_method
			from tbl_results
			""", r -> {
			if (has(RESULT, r, 1)) {
				put(PRODUCT_SYSTEM, r, 2);
				put(IMPACT_METHOD, r, 3);
			}
			return true;
		});

		// tbl_impact_results
		sql.query("""
			select
			  f_result,
			  f_impact_category
			from tbl_impact_results
			""", r -> {
			if (has(RESULT, r, 1)) {
				put(IMPACT_CATEGORY, r, 2);
			}
			return true;
		});

		// tbl_flow_results
		sql.query("""
			select
			  f_result,
			  f_flow,
			  f_location
			from tbl_flow_results
			""", r -> {
			if (has(RESULT, r, 1)) {
				put(FLOW, r, 2);
				put(LOCATION, r, 3);
			}
			return true;
		});
	}

	private void put(ModelType type, ResultSet r, int pos) {
		try {
			put(type, r.getLong(pos));
		} catch (SQLException e) {
			throw new RuntimeException(
					"failed to get ID value for " + type + " at column " + pos, e);
		}
	}

	private void put(ModelType type, long id) {
		if (type == null || id == 0L)
			return;
		var ids = refs.computeIfAbsent(type, $ -> new TLongHashSet());
		ids.add(id);
	}

	private boolean has(ModelType type, ResultSet r, int pos) {
		try {
			return has(type, r.getLong(pos));
		} catch (SQLException e) {
			throw new RuntimeException(
					"failed to get ID value for " + type + " at column " + pos, e);
		}
	}

	private boolean has(ModelType type, long id) {
		if (type == null || id == 0L)
			return false;
		var ids = refs.get(type);
		return ids != null && ids.contains(id);
	}

	private List<RootDescriptor> loadDescriptors() {
		var descriptors = new ArrayList<RootDescriptor>();
		for (var e : refs.entrySet()) {
			var type = e.getKey();
			var ids = e.getValue();
			if (ids.isEmpty())
				continue;
			var dm = Daos.root(db, type).descriptorMap();
			for (var it = ids.iterator(); it.hasNext(); ) {
				var id = it.next();
				var d = dm.get(id);
				if (d != null) {
					descriptors.add(d);
				}
			}
		}
		return descriptors;
	}

}

