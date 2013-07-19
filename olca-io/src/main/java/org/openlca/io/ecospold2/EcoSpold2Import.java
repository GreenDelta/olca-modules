package org.openlca.io.ecospold2;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.ecospold2.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The import of data sets in the EcoSpold v2 format. The import expects a set
 * of SPOLD files in the EcoSpold v2 format or ZIP files which contain such
 * files.
 */
public class EcoSpold2Import {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public EcoSpold2Import(IDatabase database) {
		this.database = database;
	}

	public void run(File file) {
		if (file == null)
			return;
		run(new File[] { file });
	}

	public void run(File[] files) {
		RefDataIndex index = importRefData(files);
		importProcesses(files, index);
	}

	private RefDataIndex importRefData(File[] files) {
		log.trace("import reference data");
		RefDataImport refDataImport = new RefDataImport(database);
		try (DataSetIterator iterator = new DataSetIterator(files)) {
			while (iterator.hasNext()) {
				DataSet dataSet = iterator.next();
				refDataImport.importDataSet(dataSet);
			}
		} catch (Exception e) {
			log.error("reference data import failed", e);
		}
		return refDataImport.getIndex();
	}

	private void importProcesses(File[] files, RefDataIndex index) {
		log.trace("import processes");
		ProcessImport processImport = new ProcessImport(database, index);
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
