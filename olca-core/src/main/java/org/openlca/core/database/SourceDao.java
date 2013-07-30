package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceDao extends CategorizedEnitityDao<Source, SourceDescriptor> {

	public SourceDao(IDatabase database) {
		super(Source.class, SourceDescriptor.class, database);
	}

	public List<BaseDescriptor> whereUsed(Source source) {
		return new SourceUseSearch(getDatabase()).findUses(source);
	}

}
