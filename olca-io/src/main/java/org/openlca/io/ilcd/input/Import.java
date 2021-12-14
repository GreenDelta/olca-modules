package org.openlca.io.ilcd.input;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.ilcd.commons.IDataSet;

public interface Import<T extends IDataSet, R extends CategorizedEntity> {

	R run(T dataSet);

}
