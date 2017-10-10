package org.openlca.core.database;

import org.openlca.core.model.descriptors.BaseDescriptor;

public interface IDatabaseListener {

	void modelInserted(BaseDescriptor descriptor);

	void modelUpdated(BaseDescriptor descriptor);

	void modelDeleted(BaseDescriptor descriptor);

}
