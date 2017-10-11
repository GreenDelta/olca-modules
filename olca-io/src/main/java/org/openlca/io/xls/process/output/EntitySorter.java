package org.openlca.io.xls.process.output;

import java.util.Comparator;

import org.openlca.core.model.RootEntity;
import org.openlca.util.Strings;

class EntitySorter implements Comparator<RootEntity> {

	@Override
	public int compare(RootEntity e1, RootEntity e2) {
		if (e1 == null && e2 == null)
			return 0;
		if (e1 == null)
			return -1;
		if (e2 == null)
			return 1;
		return Strings.compare(e1.getName(), e2.getName());
	}

}
