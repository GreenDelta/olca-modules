package org.openlca.core.database;

import org.openlca.core.model.descriptors.Descriptor;

public interface IDatabaseListener {

	void modelInserted(Descriptor descriptor);

	void modelUpdated(Descriptor descriptor);

	void modelDeleted(Descriptor descriptor);

}
