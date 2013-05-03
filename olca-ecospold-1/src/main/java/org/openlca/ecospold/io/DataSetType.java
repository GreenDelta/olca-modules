package org.openlca.ecospold.io;

import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.internal.impact.ImpactMethodFactory;
import org.openlca.ecospold.internal.process.ProcessFactory;

public enum DataSetType {

	PROCESS("http://www.EcoInvent.org/EcoSpold01", new ProcessFactory()),

	IMPACT_METHOD("http://www.EcoInvent.org/EcoSpold01Impact",
			new ImpactMethodFactory());

	private String namespace;

	private IEcoSpoldFactory factory;

	public IEcoSpoldFactory getFactory() {
		return factory;
	}

	private DataSetType(String namespace, IEcoSpoldFactory factory) {
		this.namespace = namespace;
		this.factory = factory;
	}

	static DataSetType forNamespace(String namespace) {
		for (DataSetType type : values()) {
			if (type.namespace.equals(namespace)) {
				return type;
			}
		}
		return null;
	}

}