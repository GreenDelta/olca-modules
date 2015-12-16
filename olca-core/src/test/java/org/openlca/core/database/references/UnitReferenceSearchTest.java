package org.openlca.core.database.references;

import java.util.Collections;
import java.util.List;

import org.openlca.core.Tests;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class UnitReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return null;
	}

	@Override
	protected Class<?> getModelClass() {
		return Unit.class;
	}

	@Override
	protected List<Reference> findReferences(long id) {
		return new UnitReferenceSearch(Tests.getDb())
				.findReferences(Collections.singleton(id));
	}

	@Override
	protected Unit createModel() {
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		group.getUnits().add(unit);
		group = insertAndAddExpected(group);
		return group.getUnit(unit.getName());
	}
}
