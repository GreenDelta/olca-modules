package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ProcessLink;

import gnu.trove.set.hash.TLongHashSet;

public class Upgrade11 implements IUpgrade {

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
	}

	/**
	 * Create the new column {@code provider_type} in table
	 * {@code tbl_process_links} and set its values.
	 */
	private void setLinkProviderTypes(DbUtil u) {

		u.createColumn("tbl_process_links", "provider_type SMALLINT default 0");

		// collect system IDs; we do not have result providers prior this update
		var sql = NativeSql.on(u.db);
		var systemIds = new TLongHashSet();
		sql.query("select id from tbl_product_systems", r -> {
			systemIds.add(r.getLong(1));
			return true;
		});

		// set the provider types
		sql.updateRows(
			"select f_provider, provider_type from tbl_process_links",
			r -> {
				long providerId = r.getLong(1);
				byte type = systemIds.contains(providerId)
					? ProcessLink.ProviderType.SUB_SYSTEM
					: ProcessLink.ProviderType.PROCESS;
				r.updateByte(2, type);
				r.updateRow();
				return true;
			});

	}
}

