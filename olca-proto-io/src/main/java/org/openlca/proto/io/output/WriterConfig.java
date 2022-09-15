package org.openlca.proto.io.output;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.store.EntityStore;
import org.openlca.proto.ProtoRef;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class WriterConfig {

	private final EntityStore store;
	private final DependencyHandler deps;
	private final EnumMap<ModelType, TLongObjectHashMap<RootDescriptor>> descriptors;

	public WriterConfig(EntityStore store, DependencyHandler deps) {
		this.store = store;
		this.deps = deps;
		descriptors = new EnumMap<>(ModelType.class);
	}

	public static WriterConfig of(IDatabase db) {
		return new WriterConfig(db, null);
	}

	void dep(RefEntity e, Consumer<ProtoRef> ref) {
		if (e == null)
			return;
		if (deps != null && e instanceof RootEntity re) {
			deps.push(re);
		}
		ref.accept(Refs.refOf(e).build());
	}

	RootDescriptor getDescriptor(ModelType type, long id) {
		if (type == null)
			return null;
		var map = descriptors.computeIfAbsent(type, t -> {
			var descriptors = store.getDescriptors(type.getModelClass());
			var m = new TLongObjectHashMap<RootDescriptor>(descriptors.size());
			for (var d : descriptors) {
				m.put(d.id, d);
			}
			return m;
		});
		return map.get(id);
	}

}
