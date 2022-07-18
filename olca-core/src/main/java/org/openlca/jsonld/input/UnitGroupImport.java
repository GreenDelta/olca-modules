package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

class UnitGroupImport implements EntityResolver {

	private final JsonImport imp;

	UnitGroupImport(JsonImport imp) {
		this.imp = Objects.requireNonNull(imp);
	}

	void importAll() {
		var newGroups = new ArrayList<UnitGroup>();
		var updatedGroups = new ArrayList<UnitGroup>();

		for (var refId : imp.reader.getRefIds(ModelType.UNIT_GROUP)) {
			var item = imp.fetch(UnitGroup.class, refId);
			switch (item.state()) {
				case NEW -> {
					var next = new UnitGroupReader(this).read(item.json());
					newGroups.add(next);
				}
				case UPDATE -> {
					var reader = new UnitGroupReader(this);
					reader.update(item.entity(), item.json());
					updatedGroups.add(item.entity());
				}
			}
		}

		imp.db().transaction( em -> {
			for (var g : newGroups) {
				em.persist(g);
				imp.visited(ModelType.UNIT_GROUP, g.refId);
			}
			for (var g : updatedGroups) {
				em.merge(g);
				imp.visited(ModelType.UNIT_GROUP, g.refId);
			}
		});
	}

	@Override
	public IDatabase db() {
		return imp.db();
	}

	@Override
	public <T extends RootEntity> T get(Class<T> type, String refId) {



		return Objects.equals(type, FlowProperty.class)
			? null
			: imp.get(type, refId);
	}

	@Override
	public Category getCategory(ModelType type, String path) {
		return imp.getCategory(type, path);
	}

	@Override
	public void resolveProvider(String providerId, Exchange exchange) {
		imp.resolveProvider(providerId, exchange);
	}
}
