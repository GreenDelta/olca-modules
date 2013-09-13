package org.openlca.io.xls;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;

public class CsvMatrixExportData {

	private EntityCache cache;
	private IDatabase database;
	private ProductSystem productSystem;
	private File technologyFile;
	private File interventionFile;
	private String decimalSeparator;
	private String columnSeperator;

	IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	ProductSystem getProductSystem() {
		return productSystem;
	}

	public void setProductSystem(ProductSystem productSystem) {
		this.productSystem = productSystem;
	}

	File getTechnologyFile() {
		return technologyFile;
	}

	public void setTechnologyFile(File technologyFile) {
		this.technologyFile = technologyFile;
	}

	File getInterventionFile() {
		return interventionFile;
	}

	public void setInterventionFile(File interventionFile) {
		this.interventionFile = interventionFile;
	}

	String getDecimalSeparator() {
		return decimalSeparator;
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	String getColumnSeperator() {
		return columnSeperator;
	}

	public void setColumnSeperator(String columnSeperator) {
		this.columnSeperator = columnSeperator;
	}

	public void setCache(EntityCache cache) {
		this.cache = cache;
	}

	EntityCache getCache() {
		return cache != null ? cache : EntityCache.create(database);
	}

	boolean valid() {
		return database != null && productSystem != null
				&& technologyFile != null && interventionFile != null
				&& decimalSeparator != null && columnSeperator != null;
	}

}
