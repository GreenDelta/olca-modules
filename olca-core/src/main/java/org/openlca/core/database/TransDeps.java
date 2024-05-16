package org.openlca.core.database;

import static org.openlca.core.model.ModelType.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.formula.Formulas;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TLongObjectHashMap;
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
	private final Map<String, List<Param>> params;

	private TransDeps(List<? extends RootDescriptor> ds, IDatabase db) {
		this.db = db;
		this.sql = NativeSql.on(db);
		this.refs = new EnumMap<>(ModelType.class);
		for (var d : ds) {
			put(d.type, d.id);
		}
		this.params = Param.allOf(db);
	}

	public static List<RootDescriptor> of(RootEntity e, IDatabase db) {
		return of(Descriptor.of(e), db);
	}

	public static List<RootDescriptor> of(RootDescriptor d, IDatabase db) {
		return d == null
				? Collections.emptyList()
				: of(List.of(d), db);
	}

	public static List<RootDescriptor> of(
			List<? extends RootDescriptor> ds, IDatabase db) {
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
		scanParameterFormulas();
		return this;
	}

	private void scanEpdTables() {
		if (!refs.containsKey(EPD))
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
			if (has(EPD, r)) {
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
			if (has(EPD, r)) {
				put(RESULT, r, 2);
			}
			return true;
		});
	}

	private void scanProjectTables() {
		if (!refs.containsKey(PROJECT))
			return;

		// tbl_projects
		sql.query("""
				select
				  id,
				  f_impact_method
				from tbl_projects
				""", r -> {
			if (has(PROJECT, r)) {
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
			if (has(PROJECT, r)) {
				put(PRODUCT_SYSTEM, r, 2);
			}
			return true;
		});

		// parameter redefinitions
		sql.query("""
				select
				  proj.id,
				  redef.f_context,
				  redef.name
				from tbl_projects proj
				  inner join tbl_project_variants vari on proj.id = vari.f_project
				  inner join tbl_parameter_redefs redef on vari.id = redef.f_owner
				""", r -> {
			if (has(PROJECT, r)) {
				var context = r.getLong(2);
				if (context != 0L)
					return true;
				var ps = params.get(Param.strip(r.getString(3)));
				if (ps == null)
					return true;
				for (var param : ps) {
					if (param.isGlobal()) {
						put(PARAMETER, param.id);
						return true;
					}
				}
			}
			return true;
		});
	}

	private void scanImpactMethodTables() {
		if (!refs.containsKey(IMPACT_METHOD))
			return;

		// tbl_impact_methods
		sql.query("""
				select
				  id,
					f_source
				from tbl_impact_methods
				""", r -> {
			if (has(IMPACT_METHOD, r)) {
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
			if (has(IMPACT_METHOD, r)) {
				put(IMPACT_CATEGORY, r, 2);
			}
			return true;
		});
	}

	private void scanImpactCategoryTables() {
		if (!refs.containsKey(IMPACT_CATEGORY))
			return;

		// tbl_impact_categories
		sql.query("""
				select
				  id,
				  f_source
				from tbl_impact_categories
				""", r -> {
			if (has(IMPACT_CATEGORY, r)) {
				put(SOURCE, r, 2);
			}
			return true;
		});

		// tbl_impact_factors
		sql.query("""
				select
				  f_impact_category,
				  f_flow,
					f_location,
					formula
				from tbl_impact_factors
				""", r -> {
			var impactId = r.getLong(1);
			if (has(IMPACT_CATEGORY, impactId)) {
				put(FLOW, r, 2);
				put(LOCATION, r, 3);
				scanFormula(impactId, r.getString(4));
			}
			return true;
		});
	}

	private void scanProductSystemTables() {
		if (!refs.containsKey(PRODUCT_SYSTEM))
			return;

		var providers = new TLongHashSet();

		// tbl_product_systems
		sql.query("""
				select
				  id,
				  f_reference_process
				from tbl_product_systems
				""", r -> {
			if (has(PRODUCT_SYSTEM, r)) {
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
			if (has(PRODUCT_SYSTEM, r)) {

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
			if (!has(PRODUCT_SYSTEM, r))
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

		// parameter redefinitions
		sql.query("""
				select
				  sys.id,
				  redef.f_context,
				  redef.name
				from tbl_product_systems sys
				  inner join tbl_parameter_redef_sets rs on sys.id = rs.f_product_system
				  inner join tbl_parameter_redefs redef on rs.id = redef.f_owner
				""", r -> {
			if (has(PRODUCT_SYSTEM, r)) {
				var context = r.getLong(2);
				if (context != 0L)
					return true;
				var ps = params.get(Param.strip(r.getString(3)));
				if (ps == null)
					return true;
				for (var param : ps) {
					if (param.isGlobal()) {
						put(PARAMETER, param.id);
						return true;
					}
				}
			}
			return true;
		});
	}

	private void scanProcessTables() {
		if (!refs.containsKey(PROCESS))
			return;

		// tbl_processes
		sql.query("""
				select
				  id,
				  f_location,
				  f_dq_system,
				  f_exchange_dq_system,
				  f_social_dq_system
				from tbl_processes
				""", r -> {
			if (has(PROCESS, r)) {
				put(LOCATION, r, 2);
				put(DQ_SYSTEM, r, 3);
				put(DQ_SYSTEM, r, 4);
				put(DQ_SYSTEM, r, 5);
			}
			return true;
		});

		// tbl_exchanges
		sql.query("""
				select
				  f_owner,
				  f_flow,
					f_default_provider,
				  f_location,
				  f_currency,
				  resulting_amount_formula,
				  cost_formula
				from tbl_exchanges
				""", r -> {
			var processId = r.getLong(1);
			if (has(PROCESS, processId)) {
				put(FLOW, r, 2);
				put(PROCESS, r, 3);
				put(LOCATION, r, 4);
				put(CURRENCY, r, 5);
				scanFormula(processId, r.getString(6));
				scanFormula(processId, r.getString(7));
			}
			return true;
		});

		// tbl_allocation_factors
		sql.query("""
					select
					  f_process,
					  formula
					from tbl_allocation_factors
				""", r -> {
			var processId = r.getLong(1);
			if (has(PROCESS, processId)) {
				scanFormula(processId, r.getString(2));
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
			if (has(PROCESS, r)) {
				put(SOCIAL_INDICATOR, r, 2);
				put(SOURCE, r, 3);
			}
			return true;
		});

		// tbl_process_docs
		sql.query("""
				select
				  proc.id,
				  doc.f_data_generator,
				  doc.f_data_documentor,
				  doc.f_publication,
				  doc.f_data_owner
				from tbl_process_docs doc
				  inner join tbl_processes proc on doc.id = proc.f_process_doc
				""", r -> {
			if (has(PROCESS, r)) {
				put(ACTOR, r, 2);
				put(ACTOR, r, 3);
				put(SOURCE, r, 4);
				put(ACTOR, r, 5);
			}
			return true;
		});

		// doc sources
		sql.query("""
				select
				  proc.id,
				  link.f_source
				from tbl_processes proc
				  inner join tbl_process_docs doc on proc.f_process_doc = doc.id
				  inner join tbl_source_links link on link.f_owner = doc.id
				""", r -> {
			if (has(PROCESS, r)) {
				put(SOURCE, r, 2);
			}
			return true;
		});

		// review reports
		sql.query("""
				select
					proc.id,
					rev.f_report
				from tbl_reviews rev
				  inner join tbl_process_docs doc on doc.id = rev.f_owner
					inner join tbl_processes proc on proc.f_process_doc = doc.id
				""", r -> {
			if (has(PROCESS, r)) {
				put(SOURCE, r, 2);
			}
			return true;
		});

		// reviewers
		sql.query("""
				select
				  proc.id,
				  link.f_actor
				from tbl_reviews rev
				  inner join tbl_process_docs doc on doc.id = rev.f_owner
				  inner join tbl_processes proc on proc.f_process_doc = doc.id
				  inner join tbl_actor_links link on link.f_owner = rev.id
				""", r -> {
			if (has(PROCESS, r)) {
				put(ACTOR, r, 2);
			}
			return true;
		});

		// compliance systems
		sql.query("""
				select
				  proc.id,
				  comp.f_system
				from tbl_compliance_declarations comp
				  inner join tbl_process_docs doc on doc.id = comp.f_owner
				  inner join tbl_processes proc on proc.f_process_doc = doc.id
				""", r -> {
			if (has(PROCESS, r)) {
				put(SOURCE, r, 2);
			}
			return true;
		});

	}

	private void scanFlowTables() {
		if (!refs.containsKey(FLOW))
			return;

		// tbl_flows
		sql.query("""
				select
				  id,
					f_reference_flow_property,
				  f_location
				from tbl_flows
				""", r -> {
			if (has(FLOW, r)) {
				put(FLOW_PROPERTY, r, 2);
				put(LOCATION, r, 3);
			}
			return true;
		});

		// tbl_flow_property_factors
		sql.query("""
				select
				  f_flow,
				  f_flow_property
				from tbl_flow_property_factors
				""", r -> {
			if (has(FLOW, r)) {
				put(FLOW_PROPERTY, r, 2);
			}
			return true;
		});
	}

	private void scanFlowPropertyTables() {
		if (!refs.containsKey(FLOW_PROPERTY))
			return;

		// tbl_flow_properties
		sql.query("""
				select
				  id,
				  f_unit_group
				from tbl_flow_properties
				""", r -> {
			if (has(FLOW_PROPERTY, r)) {
				put(UNIT_GROUP, r, 2);
			}
			return true;
		});
	}

	private void scanUnitGroupTables() {
		if (!refs.containsKey(UNIT_GROUP))
			return;

		// tbl_unit_groups
		sql.query("""
				select
				  id,
				  f_default_flow_property
				from tbl_unit_groups
				""", r -> {
			if (has(UNIT_GROUP, r)) {
				put(FLOW_PROPERTY, r, 2);
			}
			return true;
		});
	}

	private void scanSocialIndicatorTables() {
		if (!refs.containsKey(SOCIAL_INDICATOR))
			return;

		// tbl_social_indicators
		sql.query("""
				select
				  id,
					f_activity_quantity
				from tbl_social_indicators
				""", r -> {
			if (has(SOCIAL_INDICATOR, r)) {
				put(FLOW_PROPERTY, r, 2);
			}
			return true;
		});
	}

	private void scanCurrencyTables() {
		if (!refs.containsKey(CURRENCY))
			return;

		// tbl_currencies
		sql.query("""
				select
				  id,
					f_reference_currency
				from tbl_currencies
				""", r -> {
			if (has(CURRENCY, r)) {
				put(CURRENCY, r, 2);
			}
			return true;
		});
	}

	private void scanDQSystemTables() {
		if (!refs.containsKey(DQ_SYSTEM))
			return;

		// tbl_dq_systems
		sql.query("""
				select
				  id,
					f_source
				from tbl_dq_systems
				""", r -> {
			if (has(DQ_SYSTEM, r)) {
				put(SOURCE, r, 2);
			}
			return true;
		});
	}

	private void scanResultTables() {
		if (!refs.containsKey(RESULT))
			return;

		// tbl_results
		sql.query("""
				select
				  id,
					f_product_system,
				  f_impact_method
				from tbl_results
				""", r -> {
			if (has(RESULT, r)) {
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
			if (has(RESULT, r)) {
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
			if (has(RESULT, r)) {
				put(FLOW, r, 2);
				put(LOCATION, r, 3);
			}
			return true;
		});
	}

	private void scanParameterFormulas() {
		// for the global parameters that were collected as dependencies
		// we need to search in their formulas for references to other
		// global parameters that need to be added; and this recursively
		// until no more global parameters are found
		var paramIds = refs.get(PARAMETER);
		if (paramIds == null || paramIds.isEmpty())
			return;

		var globalParams = new TLongObjectHashMap<Param>();
		for (var list : params.values()) {
			for (var param : list) {
				if (param.isGlobal()) {
					globalParams.put(param.id, param);
				}
			}
		}

		int oldSize, newSize;
		do {
			oldSize = paramIds.size();

			// collect the formulas first, then scan them to
			// avoid a concurrent modification exception
			var formulas = new HashSet<String>();
			for (var it = paramIds.iterator(); it.hasNext(); ) {
				var param = globalParams.get(it.next());
				if (param == null || !param.isGlobal())
					continue;
				var formula = param.formula;
				if (formula != null && !formula.isBlank()) {
					formulas.add(formula);
				}
			}

			// scan the formulas for new global parameters
			for (var formula : formulas) {
				scanFormula(0L, formula);
			}
			newSize = paramIds.size();
		} while (newSize > oldSize);
	}

	private void scanFormula(long owner, String formula) {
		if (formula == null || formula.isBlank())
			return;
		try {
			for (var v : Formulas.getVariables(formula)) {
				var ps = params.get(Param.strip(v));
				if (ps == null)
					continue;

				Param global = null;
				Param local = null;
				for (var param : ps) {
					if (param.isGlobal()) {
						global = param;
						continue;
					}
					if (param.owner == owner) {
						local = param;
						break;
					}
				}

				if (local != null) {
					// parameter bound locally
					continue;
				}
				if (global != null) {
					put(PARAMETER, global.id);
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to read variables from formula: {}", formula, e);
		}
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

	/**
	 * Tests if the value of the first column has an ID of a dataset of the
	 * given model type that is contained in the collected references.
	 */
	private boolean has(ModelType type, ResultSet r) {
		try {
			return has(type, r.getLong(1));
		} catch (SQLException e) {
			throw new RuntimeException(
					"failed to get ID value for " + type + " at column " + 1, e);
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

	private record Param(
			long id, String name, long owner, ParameterScope scope, String formula
	) {

		static Map<String, List<Param>> allOf(IDatabase db) {
			var map = new HashMap<String, List<Param>>();
			var q = "select id, name, f_owner, scope, formula from tbl_parameters";
			NativeSql.on(db).query(q, r -> {
				var param = new Param(
						r.getLong(1),
						r.getString(2),
						r.getLong(3),
						ParameterScope.fromString(r.getString(4)),
						r.getString(5));
				var list = map.computeIfAbsent(
						strip(param.name),
						$ -> new ArrayList<>());
				list.add(param);
				return true;
			});
			return map;
		}

		static String strip(String name) {
			return name != null
					? name.strip().toLowerCase()
					: "";
		}

		boolean isGlobal() {
			return owner == 0L || scope == null || scope == ParameterScope.GLOBAL;
		}
	}
}

