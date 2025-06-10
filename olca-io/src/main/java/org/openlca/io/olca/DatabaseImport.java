package org.openlca.io.olca;

import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.io.Import;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Import {

	private final ImportLog log;
	private final Config conf;
	private volatile boolean cancelled;

	public DatabaseImport(IDatabase source, IDatabase target) {
		conf = Config.of(source, target);
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
			CategoryImport.run(conf);
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

		// import unit groups and remember which unit groups
		// need to be updated with a default flow property
		var unitImport = new UnitGroupImport(conf);
		unitImport.run();

		// import flow properties
		conf.syncAll(FlowProperty.class, prop -> {
			var copy = prop.copy();
			copy.unitGroup = conf.swap(prop.unitGroup);
			return copy;
		});

		// update default properties in unit groups
		var db = conf.target();
		for (var link : unitImport.getDefaultLinks()) {
			var unitGroup = link.targetUnitGroup();
			long propId = conf.seq().get(
					ModelType.FLOW_PROPERTY, link.sourcePropertyId());
			unitGroup.defaultFlowProperty = db.get(FlowProperty.class, propId);
			db.update(unitGroup);
		}
	}

	private void copyEntities() {

		conf.syncAll(Actor.class, Actor::copy);
		conf.syncAll(Location.class, Location::copy);
		conf.syncAll(Source.class, Source::copy);

		copyGlobalParameters();
		copyFlows();
		CurrencyImport.run(conf);
		copySocialIndicators();
		conf.syncAll(DQSystem.class, system -> {
			var copy = system.copy();
			copy.source = conf.swap(system.source);
			return copy;
		});

		ProcessImport.run(conf);
		ProductSystemImport.run(conf);
		copyImpactCategories();
		ImpactMethodImport.run(conf);
		ProjectImport.run(conf);
		ResultImport.run(conf);
		copyEpds();
	}

	private void copyGlobalParameters() {
		var existing = new ParameterDao(conf.target())
				.getGlobalParameters()
				.stream()
				.map(p -> p.name)
				.collect(Collectors.toSet());

		new ParameterDao(conf.source())
				.getGlobalParameters()
				.stream()
				.filter(p -> !existing.contains(p.name))
				.forEach(p -> {
					var copy = p.copy();
					copy.refId = p.refId;
					copy.category = conf.swap(p.category);
					conf.target().insert(copy);
				});
	}

	private void copyFlows() {
		conf.syncAll(Flow.class, flow -> {
			var copy = flow.copy();
			copy.location = conf.swap(flow.location);
			copy.referenceFlowProperty = conf.swap(flow.referenceFlowProperty);
			for (var fac : copy.flowPropertyFactors) {
				fac.flowProperty = conf.swap(fac.flowProperty);
			}
			return copy;
		});
	}

	private void copySocialIndicators() {
		conf.syncAll(SocialIndicator.class, src -> {
			var copy = src.copy();
			copy.activityQuantity = conf.swap(src.activityQuantity);
			copy.activityUnit = conf.mapUnit(copy.activityQuantity, src.activityUnit);
			return copy;
		});
	}

	private void copyImpactCategories() {
		conf.syncAll(ImpactCategory.class, impact -> {
			var copy = impact.copy();
			copy.source = conf.swap(impact.source);
			for (var f : copy.impactFactors) {
				f.flow = conf.swap(f.flow);
				f.flowPropertyFactor = conf.mapFactor(f.flow, f.flowPropertyFactor);
				f.unit = f.unit != null
						? conf.mapUnit(f.flowPropertyFactor, f.unit)
						: null;
				f.location = conf.swap(f.location);
			}
			return copy;
		});
	}

	private void copyEpds() {
		conf.syncAll(Epd.class, epd -> {
			var copy = epd.copy();
			copy.pcr = conf.swap(epd.pcr);
			copy.programOperator = conf.swap(copy.programOperator);
			copy.manufacturer = conf.swap(copy.manufacturer);
			copy.verifier = conf.swap(copy.verifier);
			if (copy.product != null) {
				var p = copy.product;
				p.flow = conf.swap(p.flow);
				p.property = conf.swap(p.property);
				p.unit = conf.mapUnit(p.property, p.unit);
			}
			for (var mod : copy.modules) {
				mod.result = conf.swap(mod.result);
			}
			return copy;
		});
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
