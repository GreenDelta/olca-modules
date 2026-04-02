package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProviderType;
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

			swapDefaultProviders();
			copyMappingFiles();
			FileImport.run(ctx);
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

	private void swapDefaultProviders() {
		// see the process import for more information how default providers
		// are handled (as negative values of their original IDs when they are
		// not available in the import yet)
		var q = "select f_default_provider, default_provider_type " +
			"from tbl_exchanges where f_default_provider < 0";
		NativeSql.on(ctx.target()).updateRows(q, r -> {
			long sourceId = Math.abs(r.getLong(1));
			var type = ProviderType.toModelType(r.getByte(2));
			long targetId = ctx.seq().get(type, sourceId);
			if (targetId > 0) {
				r.updateLong(1, targetId);
				r.updateRow();
			}
			return true;
		});
	}
}
