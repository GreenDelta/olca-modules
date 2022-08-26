package org.openlca.proto.io.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;
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
		if (deps != null) {
			deps.push(e);
		}
		ref.accept(Refs.refOf(e).build());
	}

	private static class DefaultHandler implements DependencyHandler {

		@Override
		public void push(RefEntity dependency) {
		}

		@Override
		public void push(Descriptor descriptor) {
		}
	}
}
