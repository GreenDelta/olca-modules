package org.openlca.io.olca;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase source;
	private IDatabase dest;

	public DatabaseImport(IDatabase source, IDatabase destination) {
		this.source = source;
		this.dest = destination;
	}

	@Override
	public void run() {
		log.trace("run database import from {} to {}", source, dest);
		try {
			Sequence seq = new Sequence(dest);
			importSimple(seq);
			importUnitRefs(seq);
			importStructs(seq);
			new FileImport(source, dest).run();
		} catch (Exception e) {
			log.error("Database import failed", e);
		}
	}

	private void importSimple(Sequence seq) {
		new LocationImport(source, dest, seq).run();
		new CategoryImport(source, dest, seq).run();
		new ActorImport(source, dest, seq).run();
		new SourceImport(source, dest, seq).run();
	}

	private void importUnitRefs(Sequence seq) {
		UnitGroupImport unitGroupImport = new UnitGroupImport(source, dest, seq);
		unitGroupImport.run();
		HashMap<String, UnitGroup> requirePropertyUpdate = unitGroupImport
				.getRequirePropertyUpdate();
		new FlowPropertyImport(source, dest, seq).run();
		updateUnitGroups(requirePropertyUpdate, seq);
	}

	/**
	 * Set the default flow properties in the given unit groups.
	 */
	private void updateUnitGroups(HashMap<String, UnitGroup> requireUpdate,
			Sequence seq) {
		FlowPropertyDao propertyDao = new FlowPropertyDao(dest);
		UnitGroupDao unitGroupDao = new UnitGroupDao(dest);
		for (String refId : requireUpdate.keySet()) {
			UnitGroup unitGroup = requireUpdate.get(refId);
			long propId = seq.get(seq.FLOW_PROPERTY, refId);
			unitGroup.setDefaultFlowProperty(propertyDao.getForId(propId));
			unitGroupDao.update(unitGroup);
		}
	}

	private void importStructs(Sequence seq) {
		new FlowImport(source, dest, seq).run();
		new ProcessImport(source, dest, seq).run();
		new ProductSystemImport(source, dest, seq).run();
		new ImpactMethodImport(source, dest, seq).run();
		new ProjectImport(source, dest, seq).run();
	}
}
