package org.openlca.io.xls;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;

public class CsvMatrixExportConfig {

	public final ProductSystem productSystem;
	public final IDatabase db;

	public File technologyFile;
	public File interventionFile;
	public String decimalSeparator = ".";
	public String columnSeperator = ",";

	private EntityCache cache;

	public CsvMatrixExportConfig(
			ProductSystem productSystem,
			IDatabase db) {
		this.productSystem = productSystem;
		this.db = db;
	}

	EntityCache getEntityCache() {
		if (cache == null) {
			cache = EntityCache.create(db);
		}
		return cache;
	}

	boolean valid() {
		return db != null && productSystem != null
				&& technologyFile != null && interventionFile != null
				&& decimalSeparator != null && columnSeperator != null;
	}

}
