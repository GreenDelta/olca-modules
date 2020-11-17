package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;

public class DQSystemReferenceSearchTest extends BaseReferenceSearchTest {

	private final List<DQSystem> systems = new ArrayList<>();

	@Override
	public void clear() {
		for (DQSystem system : systems)
			new DQSystemDao(Tests.getDb()).delete(system);
		db.clear();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.DQ_SYSTEM;
	}

	@Override
	protected DQSystem createModel() {
		DQSystem system = new DQSystem();
		system.source = insertAndAddExpected("source", new Source());
		system = db.insert(system);
		systems.add(system);
		return system;
	}

}
