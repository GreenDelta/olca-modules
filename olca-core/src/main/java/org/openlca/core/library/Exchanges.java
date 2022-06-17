package org.openlca.core.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Exchange;

class Exchanges {

	private final Library lib;
	private final IDatabase db;

	private Exchanges(Library lib, IDatabase db) {
		this.lib = lib;
		this.db = db;
	}

	static Exchanges join(Library library, IDatabase db) {
		return new Exchanges(library, db);
	}

	List<Exchange> getFor(TechFlow product) {
		if (lib == null || db == null || product == null)
			return Collections.emptyList();
		var techIndex = lib.syncTechIndex(db).orElse(null);
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
		var col = lib.getColumn(LibMatrix.A, column).orElse(null);
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
		var colB = lib.getColumn(LibMatrix.B, column).orElse(null);
		if (colB == null)
			return;
		var iFlows = lib.syncEnviIndex(db).orElse(null);
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
