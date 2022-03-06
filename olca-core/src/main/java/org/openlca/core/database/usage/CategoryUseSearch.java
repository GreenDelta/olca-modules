package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import gnu.trove.set.TLongSet;
import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;

public class CategoryUseSearch implements IUseSearch{

	private final IDatabase db;
	private ModelType type;

	public CategoryUseSearch(IDatabase db) {
		this.db = db;
	}

	public CategoryUseSearch forModelType(ModelType type) {
		this.type = type;
		return this;
	}

	@Override
	public List<? extends RootDescriptor> find(TLongSet ids) {
		if (ids.isEmpty())
			return Collections.emptyList();
		if (type != null)
			return query(type, ids);
		try {
			var exec = Executors.newFixedThreadPool(4);
			var calls = new ArrayList<Future<List<? extends RootDescriptor>>>();
			for (var type : ModelType.values()) {
				if (!type.isRoot())
					continue;
				calls.add(exec.submit(() -> query(type, ids)));
			}
			exec.shutdown();
			var descriptors = new ArrayList<RootDescriptor>();
			for (var call : calls) {
				descriptors.addAll(call.get());
			}
			return descriptors;
		} catch (Exception e) {
			throw new RuntimeException("failed to search for category usages", e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<? extends RootDescriptor> query(ModelType type, TLongSet ids) {
		if (!type.isRoot())
			return Collections.emptyList();
		var clazz = (Class<? extends RootEntity>) type.getModelClass();
		var table = clazz.getAnnotation(Table.class);
		if (table == null)
			return Collections.emptyList();
		var q = "select id from " + table + " where f_category " + Search.eqIn(ids);
		return Search.collect(db, q, clazz);
	}
}
