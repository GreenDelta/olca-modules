package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Config conf;

	public DatabaseImport(IDatabase source, IDatabase target) {
		conf = Config.of(source, target);
	}

	@Override
	public void run() {
		log.trace(
				"run database import from {} to {}", conf.source(), conf.target());
		try {
			importSimple();
			importUnitsAndQuantities();
			importStructs();
			new MappingFileImport(conf).run();
			new FileImport(conf).run();
		} catch (Exception e) {
			log.error("Database import failed", e);
		}
	}

	private void importSimple() {
		new CategoryImport(conf).run();
		conf.syncAll(Actor.class, Actor::copy);
		conf.syncAll(Location.class, Location::copy);
		conf.syncAll(Source.class, Source::copy);
		new ParameterImport(conf).run();
	}

	private void importUnitsAndQuantities() {

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

	private void importStructs() {

		// flows
		conf.syncAll(Flow.class, flow -> {
			var copy = flow.copy();
			copy.location = conf.swap(flow.location);
			copy.referenceFlowProperty = conf.swap(flow.referenceFlowProperty);
			for (var fac : copy.flowPropertyFactors) {
				fac.flowProperty = conf.swap(fac.flowProperty);
			}
			return copy;
		});

		// currencies
		new CurrencyImport(conf).run();

		// social indicators
		conf.syncAll(SocialIndicator.class, indicator -> {
			var copy = indicator.copy();
			copy.activityQuantity = conf.swap(indicator.activityQuantity);
			if (indicator.activityUnit != null) {
				long unitId = conf.seq().get(Seq.UNIT, indicator.activityUnit.refId);
				copy.activityUnit = new UnitDao(conf.target()).getForId(unitId);
			}
			return copy;
		});

		// data quality systems
		conf.syncAll(DQSystem.class, system -> {
			var copy = system.copy();
			copy.source = conf.swap(system.source);
			return copy;
		});

		new ProcessImport(conf).run();
		new ProductSystemImport(conf).run();
		new ImpactCategoryImport(conf).run();
		new ImpactMethodImport(source, target, seq).run();
		new ProjectImport(source, target, seq).run();
	}
}
