package org.openlca.core.database.descriptors;

import org.openlca.core.model.descriptors.RootDescriptor;

import java.sql.ResultSet;

final class Util {

	private Util() {
	}

	static RuntimeException ex(String message, Exception e) {
		return new RuntimeException(message, e);
	}

	static void fill(RootDescriptor d, DescriptorReader<?> r, ResultSet row) {
		d.id = r.getId(row);
		d.refId = r.getRefIf(row);
		d.name = r.getName(row);
		d.version = r.getVersion(row);
		d.lastChange = r.getLastChange(row);
		d.category = r.getCategory(row);
		d.library = r.getLibrary(row);
		d.tags = r.getTags(row);
	}
}
