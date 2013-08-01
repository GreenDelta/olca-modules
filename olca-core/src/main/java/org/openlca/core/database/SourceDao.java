package org.openlca.core.database;

import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceDao extends CategorizedEntityDao<Source, SourceDescriptor> {

	public SourceDao(IDatabase database) {
		super(Source.class, SourceDescriptor.class, database);
	}

}
