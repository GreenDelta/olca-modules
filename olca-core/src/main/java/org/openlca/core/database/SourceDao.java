package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class SourceDao extends CategorizedEnitityDao<Source> {

	public SourceDao(IDatabase database) {
		super(Source.class, database);
	}

	public List<BaseDescriptor> whereUsed(Source source) {
		return new SourceUseSearch(getDatabase()).findUses(source);
	}

}
