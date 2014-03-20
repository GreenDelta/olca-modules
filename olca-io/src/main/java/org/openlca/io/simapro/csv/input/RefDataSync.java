package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;

class RefDataSync {

	private final IDatabase database;
	private final SpRefDataIndex index;

	public RefDataSync(SpRefDataIndex index, IDatabase database) {
		this.database = database;
		this.index = index;
	}

	public RefData run() {
		RefData refData = new RefData();
		new GlobalParameterSync(index, database).run();
		new SourceSync(index, database).run(refData);
		new UnitSync(index, database).run(refData);
		new FlowSync(index, refData.getUnitMapping(), database).run(refData);
		return refData;
	}

}
