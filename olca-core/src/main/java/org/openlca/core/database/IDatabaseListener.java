package org.openlca.core.database;

public interface IDatabaseListener {

	void modelInserted(Object object);

	void modelUpdated(Object object);

	void modelDeleted(Object object);

}
