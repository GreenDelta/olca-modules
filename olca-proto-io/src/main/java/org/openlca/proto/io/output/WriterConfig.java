package org.openlca.proto.io.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;

public record WriterConfig(IDatabase db, DependencyHandler deps) {

	public static WriterConfig of(IDatabase db) {
		return new WriterConfig(db, new DefaultHandler());
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
