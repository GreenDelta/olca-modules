package org.openlca.core.database;

import org.openlca.core.model.descriptors.BaseDescriptor;

public interface INotifiable {
	
	void addListener(IDatabaseListener listener);
	
	void removeListener(IDatabaseListener listener);

	void notifyInsert(BaseDescriptor descriptor);

	void notifyUpdate(BaseDescriptor descriptor);

	void notifyDelete(BaseDescriptor descriptor);
	
}
