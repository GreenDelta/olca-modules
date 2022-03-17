package org.openlca.io.xls.process.output;

import java.util.Comparator;

import org.openlca.core.model.RefEntity;
import org.openlca.util.Strings;

class EntitySorter implements Comparator<RefEntity> {

	@Override
	public int compare(RefEntity e1, RefEntity e2) {
		if (e1 == null && e2 == null)
			return 0;
		if (e1 == null)
			return -1;
		if (e2 == null)
			return 1;
		return Strings.compare(e1.name, e2.name);
	}

}
