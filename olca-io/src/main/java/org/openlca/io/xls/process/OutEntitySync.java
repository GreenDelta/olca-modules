package org.openlca.io.xls.process;

import org.openlca.core.model.RootEntity;

interface OutEntitySync {

	void flush();

	void visit(RootEntity entity);

}
