package org.openlca.core.library.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Exchange;

class Exchanges {

	private final IDatabase db;
	private final LibReader lib;

	private Exchanges(IDatabase db, LibReader lib) {
		this.db = db;
		this.lib = lib;
	}

	static Exchanges join(IDatabase db, LibReader lib) {
		return new Exchanges(db, lib);
	}

	List<Exchange> getFor(TechFlow product) {
		if (lib == null || product == null)
			return Collections.emptyList();
		var techIndex = lib.techIndex();
		if (techIndex == null)
			return Collections.emptyList();

		// find the library index of the given product
		int column = techIndex.of(product);
		if (column < 0)
			return Collections.emptyList();

		// create the exchanges from the matrices
		var exchanges = new ArrayList<Exchange>();
		addTechFlows(exchanges, techIndex, column);
		addEnviFlows(exchanges, column);
		return exchanges;
	}

	private void addTechFlows(
		List<Exchange> exchanges, TechIndex index, int column) {
		var col = lib.columnOf(LibMatrix.A, column);
		if (col == null)
			return;
		var flowDao = new FlowDao(db);
		for (int i = 0; i < col.length; i++) {
			double val = col[i];
			if (val == 0)
				continue;
			var product = index.at(i);
			var flow = flowDao.getForId(product.flowId());
			if (flow == null)
				continue;
			var exchange = val < 0
				? Exchange.input(flow, Math.abs(val))
				: Exchange.output(flow, val);
			if (i != column) {
				exchange.defaultProviderId = product.providerId();
			}
			exchanges.add(exchange);
		}
	}

	private void addEnviFlows(List<Exchange> exchanges, int column) {
		var colB = lib.columnOf(LibMatrix.B, column);
		if (colB == null)
			return;
		var iFlows = lib.enviIndex();
		if (iFlows == null)
			return;

		var flowDao = new FlowDao(db);
		var locDao = new LocationDao(db);

		for (int i = 0; i < colB.length; i++) {
			double val = colB[i];
			if (val == 0)
				continue;
			var iFlow = iFlows.at(i);
			if (iFlow == null || iFlow.flow() == null)
				continue;
			var flow = flowDao.getForId(iFlow.flow().id);
			if (flow == null)
				continue;
			var exchange = iFlow.isInput()
				? Exchange.input(flow, -val)
				: Exchange.output(flow, val);
			if (iFlow.location() != null) {
				exchange.location = locDao.getForId(iFlow.location().id);
			}
			exchanges.add(exchange);
		}
	}
}
