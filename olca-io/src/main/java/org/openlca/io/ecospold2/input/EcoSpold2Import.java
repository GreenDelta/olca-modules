package org.openlca.io.ecospold2.input;

import org.openlca.core.io.ImportLog;
import org.openlca.core.model.ModelType;
import org.openlca.io.Import;

import java.io.File;

/**
 * The import of data sets in the EcoSpold v2 format. The import expects a set
 * of SPOLD files in the EcoSpold v2 format or ZIP files which contain such
 * files.
 */
public class EcoSpold2Import implements Import {

	private boolean canceled = false;
	private File[] files;
	private final ImportConfig config;
	private  final ImportLog log;

	public EcoSpold2Import(ImportConfig config) {
		this.config = config;
		this.log = config.log();
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public ImportLog log() {
		return log;
	}

	@Override
	public void run() {
		if (files == null) {
			log.info("files is null, nothing to do");
			return;
		}
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
		log.info("import reference data");
		var imp = new RefDataImport(config);
		try (var it = new DataSetIterator(files)) {
			while (!canceled && it.hasNext()) {
				var dataSet = it.next();
				imp.importDataSet(dataSet);
			}
		} catch (Exception e) {
			log.error("reference data import failed", e);
		}
		return imp.getIndex();
	}

	private void importProcesses(File[] files, RefDataIndex index) {
		log.info("import processes");
		var imp = new ProcessImport(index, config);
		try (var it = new DataSetIterator(files)) {
			while (!canceled && it.hasNext()) {
				var dataSet = it.next();
				imp.importDataSet(dataSet);
			}
		} catch (Exception e) {
			log.error("process import failed", e);
		}
	}
}
