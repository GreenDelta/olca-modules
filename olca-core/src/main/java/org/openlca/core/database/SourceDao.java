package org.openlca.core.database;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Source;
import org.openlca.core.model.lean.BaseDescriptor;

public class SourceDao extends CategorizedEnitityDao<Source> {

	public SourceDao(EntityManagerFactory emf) {
		super(Source.class, emf);
	}

	public List<BaseDescriptor> whereUsed(Source source) {
		return new SourceUseSearch(getEntityFactory()).findUses(source);
	}

}
