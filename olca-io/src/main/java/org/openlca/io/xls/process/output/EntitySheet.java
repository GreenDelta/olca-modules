package org.openlca.io.xls.process.output;

import org.openlca.core.model.RefEntity;

interface EntitySheet {

	void flush();

	void visit(RefEntity entity);

}
