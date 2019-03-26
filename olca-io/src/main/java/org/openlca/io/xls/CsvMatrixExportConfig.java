package org.openlca.io.xls;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ProductSystem;

import java.io.File;

public class CsvMatrixExportConfig {

	public final ProductSystem productSystem;
	public final IDatabase db;

	public File technologyFile;
	public File interventionFile;
	public String decimalSeparator = ".";
	public String columnSeperator = ",";

	private MatrixCache matrixCache;
	private EntityCache cache;

	public CsvMatrixExportConfig(
			ProductSystem productSystem,
			IDatabase db) {
		this.productSystem = productSystem;
		this.db = db;
	}

	MatrixCache getMatrixCache() {
		if (matrixCache == null) {
			matrixCache = MatrixCache.createLazy(db);
		}
		return matrixCache;
	}

	EntityCache getEntityCache() {
		if (cache == null) {
			cache = EntityCache.create(db);
		}
		return cache;
	}

	boolean valid() {
		return matrixCache != null && productSystem != null
				&& technologyFile != null && interventionFile != null
				&& decimalSeparator != null && columnSeperator != null;
	}

}
