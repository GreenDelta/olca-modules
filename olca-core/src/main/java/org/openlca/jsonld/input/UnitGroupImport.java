package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

class UnitGroupImport implements EntityResolver {

	private final JsonImport imp;

	UnitGroupImport(JsonImport imp) {
		this.imp = Objects.requireNonNull(imp);
	}

	public void importAll() {
		for (var refId : imp.reader.getRefIds(ModelType.UNIT_GROUP)) {
			get(UnitGroup.class, refId);
		}
	}

	@Override
	public IDatabase db() {
		return imp.db();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends RootEntity> T get(Class<T> type, String refId) {

		// first return null for default-flow property
		// to avoid cyclic calls
		if (Objects.equals(type, FlowProperty.class))
			return null;

		if (!Objects.equals(type, UnitGroup.class))
			return imp.get(type, refId);

		var item = imp.fetch(type, refId);
		if (item.isError() || item.isVisited())
			return item.entity();

		// read / update a unit group
		UnitGroup group = null;
		var reader = new UnitGroupReader(this);
		if (item.isNew()) {
			group = imp.db().insert(reader.read(item.json()));
		} else if (item.entity() instanceof UnitGroup g){
			reader.update(g, item.json());
			group = imp.db().update(g);
		}
		if (group == null)
			return null;
		imp.visited(group);

		// set a possible default flow property
		var propId = Json.getRefId(item.json(), "defaultFlowProperty");
		if (propId != null) {
			var prop = imp.get(FlowProperty.class, propId);
			if (prop != null) {
				group.defaultFlowProperty = prop;
				group = imp.db().update(group);
				imp.visited(group);
			}
		}

		return (T) group;
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
