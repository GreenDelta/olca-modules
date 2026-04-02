package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Location;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Source;
import org.openlca.io.Import;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Import {

	private final ImportLog log;
	private final TransferConfig conf;
	private volatile boolean cancelled;

	public DatabaseImport(IDatabase source, IDatabase target) {
		conf = TransferConfig.of(source, target);
		log = conf.log();
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
		try {
			// the order is very important for correct linking
			new CategoryTransfer(conf).syncAll();
			copyUnits();
			copyEntities();
			swapDefaultProviders();
			copyMappingFiles();
			FileImport.run(conf);
		} catch (Exception e) {
			log.error("database import failed", e);
		}
	}

	private void copyUnits() {
		new UnitGroupTransfer(conf).syncAll();
		new FlowPropertyTransfer(conf).syncAll();
	}

	private void copyEntities() {

		conf.syncAll(Actor.class, Actor::copy);
		conf.syncAll(Location.class, Location::copy);
		conf.syncAll(Source.class, Source::copy);

		new ParameterTransfer(conf).syncAll();
		new FlowTransfer(conf).syncAll();
		new CurrencyTransfer(conf).syncAll();
		new SocialIndicatorTransfer(conf).syncAll();
		new DqsTransfer(conf).syncAll();

		new ProcessTransfer(conf).syncAll();
		new ProductSystemTransfer(conf).syncAll();
		new ImpactCategoryTransfer(conf).syncAll();
		new ImpactMethodTransfer(conf).syncAll();
		ProjectTransfer.run(conf);
		new ResultTransfer(conf).syncAll();
		new EpdTransfer(conf).syncAll();
	}

	private void copyMappingFiles() {
		var sourceDao = new MappingFileDao(conf.source());
		var targetDao = new MappingFileDao(conf.target());
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
				conf.log().info("copied mapping file " + file.name);
			} catch (Exception e) {
				conf.log().error("failed to copy mapping file " + file.name, e);
			}
		}
	}

	private void swapDefaultProviders() {
		// see the process import for more information how default providers
		// are handled (as negative values of their original IDs when they are
		// not available in the import yet)
		var q = "select f_default_provider, default_provider_type " +
				"from tbl_exchanges where f_default_provider < 0";
		NativeSql.on(conf.target()).updateRows(q, r -> {
			long sourceId = Math.abs(r.getLong(1));
			var type = ProviderType.toModelType(r.getByte(2));
			long targetId = conf.seq().get(type, sourceId);
			if (targetId > 0) {
				r.updateLong(1, targetId);
				r.updateRow();
			}
			return true;
		});
	}
}
