package org.openlca.io.ecospold2.input;

import com.google.common.eventbus.EventBus;
import org.openlca.core.model.ModelType;
import org.openlca.io.FileImport;
import org.openlca.io.ImportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spold2.DataSet;

import java.io.File;

/**
 * The import of data sets in the EcoSpold v2 format. The import expects a set
 * of SPOLD files in the EcoSpold v2 format or ZIP files which contain such
 * files.
 */
public class EcoSpold2Import implements FileImport {

	private final Logger log = LoggerFactory.getLogger(getClass());
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
			log.info("files is null, nothing to do");
			return;
		}
		log.trace("run import with: {}", config);
		RefDataIndex index = importRefData(files);
		importProcesses(files, index);

		// expand ISIC category trees
		log.info("expand ISIC categories");
		new IsicCategoryTreeSync(config.db, ModelType.FLOW).run();
		new IsicCategoryTreeSync(config.db, ModelType.PROCESS).run();

		log.info("swap waste flows");
		WasteFlows.map(config.db);
		config.db.getEntityFactory().getCache().evictAll();
	}

	private RefDataIndex importRefData(File[] files) {
		log.trace("import reference data");
		RefDataImport imp = new RefDataImport(config);
		if (eventBus != null)
			eventBus.post(new ImportEvent("reference data"));
		try (DataSetIterator it = new DataSetIterator(files)) {
			while (!canceled && it.hasNext()) {
				DataSet ds = it.next();
				imp.importDataSet(ds);
			}
		} catch (Exception e) {
			log.error("reference data import failed", e);
		}
		return imp.getIndex();
	}

	private void importProcesses(File[] files, RefDataIndex index) {
		log.trace("import processes");
		ProcessImport imp = new ProcessImport(index, config);
		try (DataSetIterator it = new DataSetIterator(files)) {
			while (!canceled && it.hasNext()) {
				DataSet ds = it.next();
				fireEvent(ds);
				imp.importDataSet(ds);
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
