package org.openlca.core.library;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.util.CategoryPathBuilder;
import org.openlca.util.Strings;

class Protos {

	private Protos() {
	}

	static Proto.Process asProtoProcess(
			CategorizedDescriptor d,
			CategoryPathBuilder paths,
			IDatabase db) {
		var proto = Proto.Process.newBuilder();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setCategory(Strings.orEmpty(paths.build(d.category)));
		return proto.build();
	}
}
