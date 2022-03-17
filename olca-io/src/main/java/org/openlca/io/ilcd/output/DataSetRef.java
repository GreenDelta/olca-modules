package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

class DataSetRef {

	private DataSetRef() {
	}

	public static Ref makeRef(RootEntity model,
                              ExportConfig config) {
		if (model == null) {
			return new Ref();
		}
		Ref ref = new Ref();
		ref.version = "01.00.000";
		ref.uuid = model.refId;
		setUriAndType(model, ref);
		if (model.name != null) {
			LangString.set(ref.name, model.name,
					config.lang);
		}
		return ref;
	}

	private static void setUriAndType(RootEntity iModel,
                                      Ref ref) {
		String uri = "../";
		if (iModel instanceof Actor) {
			ref.type = DataSetType.CONTACT;
			uri += "contacts/";
		} else if (iModel instanceof Source) {
			ref.type = DataSetType.SOURCE;
			uri += "sources/";
		} else if (iModel instanceof UnitGroup) {
			ref.type = DataSetType.UNIT_GROUP;
			uri += "unitgroups/";
		} else if (iModel instanceof FlowProperty) {
			ref.type = DataSetType.FLOW_PROPERTY;
			uri += "flowproperties/";
		} else if (iModel instanceof Flow) {
			ref.type = DataSetType.FLOW;
			uri += "flows/";
		} else if (iModel instanceof Process) {
			ref.type = DataSetType.PROCESS;
			uri += "processes/";
		}
		uri += iModel.refId;
		ref.uri = uri + ".xml";
	}

}
