package org.openlca.io.olca;

import java.util.Calendar;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
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
			importUnitRefs();
			importStructs();
			new MappingFileImport().run();
			new FileImport(source, target).run();
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

	private void importUnitRefs() {
		var unitImport = new UnitGroupImport(conf);
		unitImport.run();
		var requireUpdate = unitImport.getRequirePropertyUpdate();
		new FlowPropertyImport(source, target, seq).run();
		var propertyDao = new FlowPropertyDao(target);
		var unitGroupDao = new UnitGroupDao(target);
		for (var refId : requireUpdate.keySet()) {
			var unitGroup = requireUpdate.get(refId);
			long propId = seq.get(seq.FLOW_PROPERTY, refId);
			unitGroup.defaultFlowProperty = propertyDao.getForId(propId);
			unitGroup.lastChange = Calendar.getInstance().getTimeInMillis();
			Version.incUpdate(unitGroup);
			unitGroupDao.update(unitGroup);
		}
	}

	private void importStructs(Seq seq) {
		new FlowImport(source, target, seq).run();
		new CurrencyImport(source, target, seq).run();
		new SocialIndicatorImport(source, target, seq).run();
		new DQSystemImport(source, target, seq).run();
		new ProcessImport(source, target, seq).run();
		new ProductSystemImport(source, target, seq).run();
		new ImpactCategoryImport(source, target, seq).run();
		new ImpactMethodImport(source, target, seq).run();
		new ProjectImport(source, target, seq).run();
	}
}
