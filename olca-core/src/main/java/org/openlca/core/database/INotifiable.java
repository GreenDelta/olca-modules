package org.openlca.core.database;

public interface INotifiable {
	
	void addListener(IDatabaseListener listener);
	
	void removeListener(IDatabaseListener listener);

	void notifyInsert(Object object);

	void notifyUpdate(Object object);

	void notifyDelete(Object object);
	
}
