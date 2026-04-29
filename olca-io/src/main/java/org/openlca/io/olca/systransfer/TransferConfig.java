package org.openlca.io.olca.systransfer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;

public record TransferConfig(
	IDatabase source,
	IDatabase target,
	ProductSystem system,
	MatchingStrategy strategy) {

	boolean isNotComplete() {
		return source == null
			|| target == null
			|| system == null
			|| strategy == null;
	}
}
