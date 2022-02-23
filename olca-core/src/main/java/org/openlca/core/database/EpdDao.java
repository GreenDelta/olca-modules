package org.openlca.core.database;

import org.openlca.core.model.Epd;
import org.openlca.core.model.descriptors.EpdDescriptor;

public class EpdDao extends RootEntityDao<Epd, EpdDescriptor> {

	public EpdDao(IDatabase db) {
		super(Epd.class, EpdDescriptor.class, db);
	}

}
