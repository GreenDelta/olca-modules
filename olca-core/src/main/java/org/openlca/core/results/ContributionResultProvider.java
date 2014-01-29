package org.openlca.core.results;

import org.openlca.core.database.EntityCache;

public class ContributionResultProvider<T extends ContributionResult> extends
		SimpleResultProvider<T> {

	public ContributionResultProvider(T result, EntityCache cache) {
		super(result, cache);
	}

}
