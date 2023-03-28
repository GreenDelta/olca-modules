package org.openlca.io.olca;

import java.util.Calendar;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase source;
	private final IDatabase dest;

	public DatabaseImport(IDatabase source, IDatabase destination) {
		this.source = source;
		this.dest = destination;
	}

	@Override
	public void run() {
		log.trace("run database import from {} to {}", source, dest);
		try {
			var seq = new Sequence(dest);
			importSimple(seq);
			importUnitRefs(seq);
			importStructs(seq);
			new MappingFileImport(source, dest).run();
			new FileImport(source, dest).run();
		} catch (Exception e) {
			log.error("Database import failed", e);
		}
	}

	private void importSimple(Sequence seq) {
		new CategoryImport(source, dest, seq).run();
		new LocationImport(source, dest, seq).run();
		new ActorImport(source, dest, seq).run();
		new SourceImport(source, dest, seq).run();
		new ParameterImport(source, dest).run();
	}

	private void importUnitRefs(Sequence seq) {
		var unitImport = new UnitGroupImport(source, dest, seq);
		unitImport.run();
		var requireUpdate = unitImport.getRequirePropertyUpdate();
		new FlowPropertyImport(source, dest, seq).run();
		var propertyDao = new FlowPropertyDao(dest);
		var unitGroupDao = new UnitGroupDao(dest);
		for (var refId : requireUpdate.keySet()) {
			var unitGroup = requireUpdate.get(refId);
			long propId = seq.get(seq.FLOW_PROPERTY, refId);
			unitGroup.defaultFlowProperty = propertyDao.getForId(propId);
			unitGroup.lastChange = Calendar.getInstance().getTimeInMillis();
			Version.incUpdate(unitGroup);
			unitGroupDao.update(unitGroup);
		}
	}

	private void importStructs(Sequence seq) {
		new FlowImport(source, dest, seq).run();
		new CurrencyImport(source, dest, seq).run();
		new SocialIndicatorImport(source, dest, seq).run();
		new DQSystemImport(source, dest, seq).run();
		new ProcessImport(source, dest, seq).run();
		new ProductSystemImport(source, dest, seq).run();
		new ImpactCategoryImport(source, dest, seq).run();
		new ImpactMethodImport(source, dest, seq).run();
		new ProjectImport(source, dest, seq).run();
	}
}
