package org.openlca.proto.io.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.store.EntityStore;
import org.openlca.proto.ProtoRef;

import java.util.function.Consumer;

public record WriterConfig(EntityStore db, DependencyHandler deps) {

	public static WriterConfig of(IDatabase db) {
		return new WriterConfig(db, new DefaultHandler());
	}

	void dep(RefEntity e, Consumer<ProtoRef> ref) {
		if (e == null)
			return;
		if (deps != null && e instanceof RootEntity re) {
			deps.push(re);
		}
		ref.accept(Refs.refOf(e).build());
	}

	private static class DefaultHandler implements DependencyHandler {

		@Override
		public void push(RootEntity dependency) {
		}

		@Override
		public void push(RootDescriptor descriptor) {
		}
	}
}
