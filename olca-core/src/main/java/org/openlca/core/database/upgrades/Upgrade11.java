package org.openlca.core.database.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ProviderType;
import org.openlca.util.KeyGen;
import org.slf4j.LoggerFactory;


class Upgrade11 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{10};
	}

	@Override
	public int getEndVersion() {
		return 11;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		setLinkProviderTypes(u);

		u.createColumn("tbl_parameter_redefs", "is_protected SMALLINT default 0");
		u.createColumn("tbl_impact_methods", "f_source BIGINT");
		u.createColumn("tbl_impact_methods", "code VARCHAR(255)");
		u.createColumn("tbl_impact_categories", "f_source BIGINT");
		u.createColumn("tbl_impact_categories", "code VARCHAR(255)");
		u.createColumn("tbl_impact_categories", "direction VARCHAR(255)");

		u.createTable(
				"tbl_results",
				"""
						CREATE TABLE tbl_results (

						    id                   BIGINT NOT NULL,
						    ref_id               VARCHAR(36),
						    name                 VARCHAR(2048),
						    version              BIGINT,
						    last_change          BIGINT,
						    f_category           BIGINT,
						    tags                 VARCHAR(255),
						    library              VARCHAR(255),
						    description          CLOB(64 K),

						    f_product_system     BIGINT,
						    f_impact_method      BIGINT,
						    f_reference_flow     BIGINT,

						    PRIMARY KEY (id)
						)"""
		);

		u.createTable(
				"tbl_flow_results",
				"""
						CREATE TABLE tbl_flow_results (

						    id                        BIGINT NOT NULL,
						    f_result                  BIGINT,
						    f_flow                    BIGINT,
						    f_unit                    BIGINT,
						    is_input                  SMALLINT default 0,
						    f_flow_property_factor    BIGINT,
						    resulting_amount_value    DOUBLE,
						    f_location                BIGINT,
						    description               CLOB(64 K),

						    PRIMARY KEY (id)
						)"""
		);

		u.createTable(
				"tbl_impact_results",
				"""
						CREATE TABLE tbl_impact_results (

						     id                 BIGINT NOT NULL,
						     f_result           BIGINT,
						     f_impact_category  BIGINT,
						     amount             DOUBLE,
						     description        CLOB(64 K),

						     PRIMARY KEY (id)
						 )"""
		);

		u.createTable(
				"tbl_epds",
				"""
						CREATE TABLE tbl_epds (

						    id                   BIGINT NOT NULL,
						    ref_id               VARCHAR(36),
						    name                 VARCHAR(2048),
						    version              BIGINT,
						    last_change          BIGINT,
						    f_category           BIGINT,
						    tags                 VARCHAR(255),
						    library              VARCHAR(255),
						    description          CLOB(64 K),

						    f_flow               BIGINT,
						    f_flow_property      BIGINT,
						    f_unit               BIGINT,
						    amount               DOUBLE,

						    urn                  VARCHAR(2048),
						    f_manufacturer       BIGINT,
						    f_verifier           BIGINT,
						    f_pcr                BIGINT,
						    f_program_operator   BIGINT,

						    PRIMARY KEY (id)
						)"""
		);

		u.createTable(
				"tbl_epd_modules",
				"""
						CREATE TABLE tbl_epd_modules (

						    id           BIGINT NOT NULL,
						    f_epd        BIGINT,
						    name         VARCHAR(2048),
						    f_result     BIGINT,
						    multiplier   DOUBLE
						)"""
		);

		// the UNKNOWN model type was removed and there were cases where
		// this was assigned as the content type for categories
		NativeSql.on(db).runUpdate("update tbl_categories set " +
				"model_type = null where model_type = 'UNKNOWN'");

		updateCategoryNames(u);
	}

	/**
	 * Create the new column {@code provider_type} in table
	 * {@code tbl_process_links} and set its values.
	 */
	private void setLinkProviderTypes(DbUtil u) {
		u.createColumn("tbl_process_links",
				"provider_type SMALLINT NOT NULL DEFAULT "
						+ ProviderType.PROCESS);

		// collect the IDs of product systems that are sub-systems;
		// we do not result instances as providers prior this update
		var sql = NativeSql.on(u.db);
		var systemIds = new HashSet<Long>();
		sql.query("SELECT sys.id FROM tbl_product_systems sys" +
				"  INNER JOIN tbl_product_system_processes p" +
				"  ON p.f_process = sys.id", r -> {
			systemIds.add(r.getLong(1));
			return true;
		});
		if (systemIds.isEmpty())
			return;

		// update process links of a sub system provider
		sql.updateRows("SELECT provider_type FROM tbl_process_links " +
				"WHERE f_provider IN " + NativeSql.asList(systemIds), r -> {
			r.updateByte(1, ProviderType.SUB_SYSTEM);
			r.updateRow();
			return true;
		});
	}

	private void updateCategoryNames(DbUtil u) {
		// category names must not contain the '/' character because
		// it is used as the path separator. we replace it with '|'
		// and then also update the reference IDs of the categories
		// if necessary

		var sql = NativeSql.on(u.db);
		var needsUpdate = new AtomicBoolean(false);
		var names = new HashMap<Long, String>();
		var parents = new HashMap<Long, Long>();
		sql.query(
				"select id, name, f_category from tbl_categories",
				r -> {
					long id = r.getLong(1);
					var name = r.getString(2);
					if (name != null && name.contains("/")) {
						name = name.replace('/', '|');
						needsUpdate.set(true);
					}
					names.put(id, name);
					long parent = r.getLong(3);
					if (parent != 0) {
						parents.put(id, parent);
					}
					return true;
				});

		if (!needsUpdate.get())
			return;

		sql.updateRows(
				"select id, model_type, ref_id, name from tbl_categories",
				r -> {
					long id = r.getLong(1);
					var type = r.getString(2);
					var refId = getCategoryRefId(id, type, names, parents);
					var name = names.get(id);
					r.updateString(3, refId);
					r.updateString(4, name);
					r.updateRow();
					return true;
				});
	}

	private String getCategoryRefId(
			long id, String type, Map<Long, String> names, Map<Long, Long> parents) {
		var path = new ArrayList<String>();
		Long next = id;
		var handled = new HashSet<Long>(5);
		while (next != null) {
			if (handled.contains(next)) {
				LoggerFactory.getLogger(getClass())
						.warn("there are loops in the category tree: id=" + next);
				break;
			}
			handled.add(next);
			path.add(names.get(next));
			next = parents.get(next);
		}
		path.add(type);
		Collections.reverse(path);
		return KeyGen.get(path.toArray(String[]::new));
	}
}
