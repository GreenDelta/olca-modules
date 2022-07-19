package org.openlca.core.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Direction;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

class ImpactFactors {

	private final Library lib;
	private final IDatabase db;

	private ImpactFactors(Library lib, IDatabase db) {
		this.lib = lib;
		this.db = db;
	}

	static ImpactFactors join(Library lib, IDatabase db) {
		return new ImpactFactors(lib, db);
	}

	List<ImpactFactor> getFor(ImpactDescriptor impact) {
		if (lib == null || db == null || impact == null)
			return Collections.emptyList();

		// get the row of the impact category
		var impactIndex = lib.syncImpactIndex(db).orElse(null);
		if (impactIndex == null)
			return Collections.emptyList();
		int row = impactIndex.of(impact);
		if (row < 0)
			return Collections.emptyList();

		// sync the flows
		var flowIndex = lib.syncEnviIndex(db).orElse(null);
		if (flowIndex == null)
			return Collections.emptyList();

		// get the values
		var m = lib.getMatrix(LibMatrix.C).orElse(null);
		if (m == null || m.rows() <= row)
			return Collections.emptyList();
		var values = m.getRow(row);

		// create the factors
		var flowDao = new FlowDao(db);
		var locDao = new LocationDao(db);
		var factors = new ArrayList<ImpactFactor>();
		for (int col = 0; col < values.length; col++) {
			double val = values[col];
			if (val == 0)
				continue;
			var iFlow = flowIndex.at(col);
			if (iFlow == null || iFlow.flow() == null)
				continue;
			var flow = flowDao.getForId(iFlow.flow().id);
			if (flow == null)
				continue;

			if (impact.direction != null) {
				if (impact.direction == Direction.INPUT) {
					val = -val;
				}
			} else if (iFlow.isInput()) {
				val = -val;
			}

			var factor = ImpactFactor.of(flow, val);
			if (iFlow.location() != null) {
				factor.location = locDao.getForId(iFlow.location().id);
			}
			factors.add(factor);
		}

		return factors;
	}

}
