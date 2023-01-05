package org.openlca.io.xls.process.output;

import org.openlca.core.model.RootEntity;

interface EntitySheet {

	void flush();

	void visit(RootEntity entity);

}
