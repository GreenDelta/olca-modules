package org.openlca.io.ecospold2.input;

import java.io.File;

import org.openlca.io.FileImport;
import org.openlca.io.ImportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import spold2.DataSet;

/**
 * The import of data sets in the EcoSpold v2 format. The import expects a set
 * of SPOLD files in the EcoSpold v2 format or ZIP files which contain such
 * files.
 */
public class EcoSpold2Import implements FileImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EventBus eventBus;
	private boolean canceled = false;
	private File[] files;
	private final ImportConfig config;

	public EcoSpold2Import(ImportConfig config) {
		this.config = config;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void run() {
		if (files == null) {
			log.trace("files is null, nothing to do");
			return;
		}
		log.trace("run import with: {}", config);
		RefDataIndex index = importRefData(files);
		importProcesses(files, index);
	}

	private RefDataIndex importRefData(File[] files) {
		log.trace("import reference data");
		RefDataImport refDataImport = new RefDataImport(config);
		if (eventBus != null)
			eventBus.post(new ImportEvent("reference data"));
		try (DataSetIterator iterator = new DataSetIterator(files)) {
			while (!canceled && iterator.hasNext()) {
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
		ProcessImport processImport = new ProcessImport(index, config);
		try (DataSetIterator iterator = new DataSetIterator(files)) {
			while (!canceled && iterator.hasNext()) {
				DataSet dataSet = iterator.next();
				fireEvent(dataSet);
				processImport.importDataSet(dataSet);
			}
		} catch (Exception e) {
			log.error("process import failed", e);
		}
	}

	private void fireEvent(DataSet dataSet) {
		if (eventBus == null
				|| dataSet.description == null
				|| dataSet.description.activity == null)
			return;
		eventBus.post(new ImportEvent(dataSet.description.activity.name));
	}
}
