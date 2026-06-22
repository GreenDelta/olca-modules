package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ModelType;
import org.openlca.io.Import;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Import {

	private final ImportLog log;
	private final TransferContext ctx;
	private volatile boolean cancelled;

	public DatabaseImport(IDatabase source, IDatabase target) {
		ctx = TransferContext.create(source, target);
		log = ctx.log();
	}

	public ImportLog log() {
		return log;
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	@Override
	public boolean isCanceled() {
		return cancelled;
	}

	@Override
	public void run() {

		var importOrder = new ModelType[]{
			ModelType.CATEGORY,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.ACTOR,
			ModelType.LOCATION,
			ModelType.SOURCE,
			ModelType.PARAMETER,
			ModelType.FLOW,
			ModelType.CURRENCY,
			ModelType.SOCIAL_INDICATOR,
			ModelType.DQ_SYSTEM,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.EPD,
		};

		try {
			for (var type : importOrder) {
				var transfer = ctx.getTransfer(type);
				transfer.syncAll();
			}
			ProcessTransfer.swapDefaultProviders(ctx);
			copyMappingFiles();
		} catch (Exception e) {
			log.error("database import failed", e);
		}
	}

	private void copyMappingFiles() {
		var sourceDao = new MappingFileDao(ctx.source());
		var targetDao = new MappingFileDao(ctx.target());
		for (var file : sourceDao.getAll()) {
			if (file == null || file.content == null)
				continue;
			try {
				var copy = targetDao.getForName(file.name);
				if (copy != null)
					continue;
				copy = new MappingFile();
				copy.content = file.content;
				copy.name = file.name;
				targetDao.insert(copy);
				ctx.log().info("copied mapping file " + file.name);
			} catch (Exception e) {
				ctx.log().error("failed to copy mapping file " + file.name, e);
			}
		}
	}
}
