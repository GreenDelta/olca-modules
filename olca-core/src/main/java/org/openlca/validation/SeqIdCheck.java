package org.openlca.validation;

import org.openlca.core.database.NativeSql;

import java.util.concurrent.atomic.AtomicLong;

class SeqIdCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;

	SeqIdCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			scanTables();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked ID sequences in tables");
			}
		} catch (Exception e) {
			v.error("error in validation of ID sequences", e);
		} finally {
			v.workerFinished();
		}
	}

	private void scanTables() {

		var sql = NativeSql.on(v.db);
		var seqRef = new AtomicLong(-1);
		var seqQuery = """
						select seq_count from sequence where seq_name = 'entity_seq'
				""";
		sql.query(seqQuery, r -> {
			seqRef.set(r.getLong(1));
			return false;
		});
		var seq = seqRef.get();

		var tables = new String[] {
				"tbl_categories",
				"tbl_actors",
				"tbl_locations",
				"tbl_sources",
				"tbl_units",
				"tbl_unit_groups",
				"tbl_flow_properties",
				"tbl_flows",
				"tbl_flow_property_factors",
				"tbl_processes",
				"tbl_process_docs",
				"tbl_exchanges",
				"tbl_allocation_factors",
				"tbl_product_systems",
				"tbl_parameter_redef_sets",
				"tbl_impact_methods",
				"tbl_impact_categories",
				"tbl_impact_factors",
				"tbl_nw_sets",
				"tbl_nw_factors",
				"tbl_parameters",
				"tbl_parameter_redefs",
				"tbl_projects",
				"tbl_project_variants",
				"tbl_mapping_files",
				"tbl_currencies",
				"tbl_process_group_sets",
				"tbl_social_indicators",
				"tbl_social_aspects",
				"tbl_dq_systems",
				"tbl_dq_indicators",
				"tbl_dq_scores",
				"tbl_results",
				"tbl_flow_results",
				"tbl_impact_results",
				"tbl_epds",
				"tbl_epd_modules",
		};

		for (var table : tables) {
			if (v.wasCanceled())
				break;
			var maxRef = new AtomicLong(-1);
			sql.query("select max(id) from " + table, r -> {
				maxRef.set(r.getLong(1));
				return false;
			});
			var max = maxRef.get();
			if (max < 0)
				continue;
			if (max > seq) {
				v.error("max(id) in table '" + table +
						"' is larger than the ID sequence of this database.");
				foundErrors = true;
			}
		}
	}
}
