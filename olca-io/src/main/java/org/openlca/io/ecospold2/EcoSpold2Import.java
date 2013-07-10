package org.openlca.io.ecospold2;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.ecospold2.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoSpold2Import {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProcessImport processImport;

	public EcoSpold2Import(IDatabase database) {
		this.processImport = new ProcessImport(database);
	}

	public void run(File[] files) {
		try (DataSetIterator iterator = new DataSetIterator(files)) {
			while (iterator.hasNext()) {
				DataSet dataSet = iterator.next();
				processImport.importDataSet(dataSet);
			}
		} catch (Exception e) {
			log.error("process import failed", e);
		}
	}

}
