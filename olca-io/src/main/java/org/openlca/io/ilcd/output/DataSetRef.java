package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

class DataSetRef {

	private DataSetRef() {
	}

	public static Ref refOf(RootEntity e, ILCDExport exp) {
		if (e == null) {
			return new Ref();
		}
		var ref = new Ref();
		ref.version = "01.00.000";
		exp.add(ref.name, e.name);
		ref.uuid = e.refId;
		setUriAndType(e, ref);
		return ref;
	}

	private static void setUriAndType(RootEntity e, Ref ref) {
		String uri = "../";
		if (e instanceof Actor) {
			ref.type = DataSetType.CONTACT;
			uri += "contacts/";
		} else if (e instanceof Source) {
			ref.type = DataSetType.SOURCE;
			uri += "sources/";
		} else if (e instanceof UnitGroup) {
			ref.type = DataSetType.UNIT_GROUP;
			uri += "unitgroups/";
		} else if (e instanceof FlowProperty) {
			ref.type = DataSetType.FLOW_PROPERTY;
			uri += "flowproperties/";
		} else if (e instanceof Flow) {
			ref.type = DataSetType.FLOW;
			uri += "flows/";
		} else if (e instanceof Process) {
			ref.type = DataSetType.PROCESS;
			uri += "processes/";
		}
		uri += e.refId;
		ref.uri = uri + ".xml";
	}

}
