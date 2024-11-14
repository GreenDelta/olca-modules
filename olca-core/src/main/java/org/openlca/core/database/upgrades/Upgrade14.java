package org.openlca.core.database.upgrades;

import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

class Upgrade14 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{13};
	}

	@Override
	public int getEndVersion() {
		return 14;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		u.createTable("tbl_analysis_groups", """
				CREATE TABLE tbl_analysis_groups (

				    id     BIGINT NOT NULL,
				    name   VARCHAR(2048),
				    color  VARCHAR(255),
				    f_product_system  BIGINT,

				    PRIMARY KEY (id)
				)
				""");

		u.createTable("tbl_analysis_group_processes", """
				CREATE TABLE tbl_analysis_group_processes (
				    f_analysis_group  BIGINT NOT NULL,
				    f_process         BIGINT NOT NULL,

				    PRIMARY KEY (f_analysis_group, f_process)
				)
				""");

		incParamFormulaSize(db);
	}

	private void incParamFormulaSize(IDatabase db) {

		// query the current type
		var typeRef = new AtomicReference<String>();
		NativeSql.on(db).query("select c.columndatatype from sys.syscolumns c" +
				" join sys.systables t on c.referenceid = t.tableid" +
				" where t.tablename = 'TBL_PARAMETERS'" +
				"  and c.columnname = 'FORMULA'", r -> {
			typeRef.set(r.getString(1));
			return false;
		});
		var type = typeRef.get();
		if (type == null)
			throw new IllegalStateException("could not get datatype" +
					" of tbl_parameters.formula");

		// check the size
		type = type.strip().toLowerCase();
		if (!type.startsWith("varchar("))
			throw new IllegalStateException("tbl_parameters.formula has unknown type");
		try {
			var size = type.substring(8, type.length() - 1);
			int len = Integer.parseInt(size);
			if (len > 5000)
				return;
		} catch (Exception e) {
			throw new IllegalStateException("could not extract field length of" +
					"tbl_parameters.formula");
		}

		// increase the size
		NativeSql.on(db).runUpdate("""
				ALTER TABLE tbl_parameters ALTER COLUMN formula
				SET DATA TYPE VARCHAR(5120)
				""");
	}
}
