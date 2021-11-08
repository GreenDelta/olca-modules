package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactMethod;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactMethods {

	private final Logger log = LoggerFactory.getLogger(ImpactMethods.class);
	private final IDatabase db;
	private final RefData refData;
	private final ImpactMethodBlock block;

	ImpactMethods(IDatabase db, RefData refData, ImpactMethodBlock block) {
		this.db = db;
		this.refData = refData;
		this.block = block;
	}


	static void map(IDatabase db, RefData refData, ImpactMethodBlock block){
		new ImpactMethods(db, refData, block).exec();
	}

	private void exec() {


		var refId = KeyGen.get("SimaPro CSV/" + block.name());
		var method = db.get(ImpactMethod.class, refId);
		if (method != null) {
			log.warn("an LCIA method refId={} already exists; skipped", refId);
			return;
		}

		method = new ImpactMethod();
		method.refId = refId;
		method.name = block.name();


	}


}
