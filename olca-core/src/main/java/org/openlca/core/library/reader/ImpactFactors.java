package org.openlca.core.library.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.model.Direction;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

class ImpactFactors {

	private final LibReader lib;
	private final IDatabase db;

	private ImpactFactors(IDatabase db, LibReader lib) {
		this.lib = lib;
		this.db = db;
	}

	static ImpactFactors join(IDatabase db, LibReader lib) {
		return new ImpactFactors(db, lib);
	}

	List<ImpactFactor> getFor(ImpactDescriptor impact) {
		if (lib == null || db == null || impact == null)
			return Collections.emptyList();

		// get the row of the impact category
		var impactIndex = lib.impactIndex();
		if (impactIndex == null)
			return Collections.emptyList();
		int row = impactIndex.of(impact);
		if (row < 0)
			return Collections.emptyList();

		// sync the flows
		var flowIndex = lib.enviIndex();
		if (flowIndex == null)
			return Collections.emptyList();

		// get the values
		var m = lib.matrixOf(LibMatrix.C);
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
