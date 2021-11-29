package org.openlca.io.simapro.csv.input;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.util.Pair;

class ProductSystems {

	private final IDatabase db;
	private final List<Pair<ProductStageBlock, Process>> pairs;

	private ProductSystems(
		IDatabase db, List<Pair<ProductStageBlock, Process>> pairs) {
		this.db = db;
		this.pairs = pairs;
	}



}
