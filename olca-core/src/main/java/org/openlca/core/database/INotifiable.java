package org.openlca.core.database;

import org.openlca.core.model.descriptors.Descriptor;

public interface INotifiable {
	
	void addListener(IDatabaseListener listener);
	
	void removeListener(IDatabaseListener listener);

	void notifyInsert(Descriptor descriptor);

	void notifyUpdate(Descriptor descriptor);

	void notifyDelete(Descriptor descriptor);
	
}
