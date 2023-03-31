package org.openlca.io.olca;

import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Runnable {

	private final ImportLog log;
	private final Config conf;

	public DatabaseImport(IDatabase source, IDatabase target) {
		conf = Config.of(source, target);
		log = conf.log();
	}

	public ImportLog log() {
		return log;
	}

	@Override
	public void run() {
		try {
			// the order is very important for correct linking
			CategoryImport.run(conf);
			copyUnits();
			copyEntities();
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
		var requireUpdate = unitImport.getRequirePropertyUpdate();

		// import flow properties
		conf.syncAll(FlowProperty.class, prop -> {
			var copy = prop.copy();
			copy.unitGroup = conf.swap(prop.unitGroup);
			return copy;
		});

		// update unit groups
		var db = conf.target();
		for (var refId : requireUpdate.keySet()) {
			var unitGroup = requireUpdate.get(refId);
			long propId = conf.seq().get(Seq.FLOW_PROPERTY, refId);
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
					if (!conf.seq().contains(Seq.CATEGORY, p.refId)) {
						copy.refId = p.refId;
					}
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
		conf.syncAll(SocialIndicator.class, indicator -> {
			var copy = indicator.copy();
			copy.activityQuantity = conf.swap(indicator.activityQuantity);
			if (indicator.activityUnit != null) {
				long unitId = conf.seq().get(Seq.UNIT, indicator.activityUnit.refId);
				copy.activityUnit = new UnitDao(conf.target()).getForId(unitId);
			}
			return copy;
		});
	}

	private void copyImpactCategories() {
		var refs = new RefSwitcher(conf);
		conf.syncAll(ImpactCategory.class, impact -> {
			var copy = impact.copy();
			copy.source = conf.swap(impact.source);
			for (var f : copy.impactFactors) {
				f.flow = conf.swap(f.flow);
				f.unit = refs.switchRef(f.unit);
				f.flowPropertyFactor = refs.switchRef(f.flowPropertyFactor, f.flow);
				f.location = conf.swap(f.location);
			}
			return copy;
		});
	}

	private void copyEpds() {
		var refs = new RefSwitcher(conf);
		conf.syncAll(Epd.class, epd -> {
			var copy = epd.copy();
			copy.pcr = conf.swap(epd.pcr);
			copy.programOperator = conf.swap(copy.programOperator);
			copy.manufacturer = conf.swap(copy.manufacturer);
			copy.verifier = conf.swap(copy.verifier);
			if (copy.product != null) {
				var p = copy.product;
				p.flow = conf.swap(p.flow);
				p.unit = refs.switchRef(p.unit);
				p.property = conf.swap(p.property);
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
}
