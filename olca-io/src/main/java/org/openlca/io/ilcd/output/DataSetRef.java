package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.util.LangString;

class DataSetRef {

	private DataSetRef() {
	}

	public static DataSetReference makeRef(CategorizedEntity model) {
		if (model == null) {
			return new DataSetReference();
		}
		DataSetReference ref = new DataSetReference();
		ref.setVersion("01.00.000");
		ref.setUuid(model.getRefId());
		setUriAndType(model, ref);
		if (model.getName() != null) {
			LangString.addShortText(ref.getShortDescription(), model.getName());
		}
		return ref;
	}

	private static void setUriAndType(CategorizedEntity iModel, DataSetReference ref) {
		String uri = "../";
		if (iModel instanceof Actor) {
			ref.setType(DataSetType.CONTACT_DATA_SET);
			uri += "contacts/";
		} else if (iModel instanceof Source) {
			ref.setType(DataSetType.SOURCE_DATA_SET);
			uri += "sources/";
		} else if (iModel instanceof UnitGroup) {
			ref.setType(DataSetType.UNIT_GROUP_DATA_SET);
			uri += "unitgroups/";
		} else if (iModel instanceof FlowProperty) {
			ref.setType(DataSetType.FLOW_PROPERTY_DATA_SET);
			uri += "flowproperties/";
		} else if (iModel instanceof Flow) {
			ref.setType(DataSetType.FLOW_DATA_SET);
			uri += "flows/";
		} else if (iModel instanceof Process) {
			ref.setType(DataSetType.PROCESS_DATA_SET);
			uri += "processes/";
		}
		uri += iModel.getRefId();
		ref.setUri(uri);
	}

}
