package org.openlca.git.actions;

import java.util.List;

import org.openlca.git.model.Diff;

public interface MergedData {

	List<Diff> getDiffs();
	
	default byte[] get(Diff diff) {
		return null;
	}

}
