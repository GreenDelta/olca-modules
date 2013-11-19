package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.FlowPropertyBuilder;
import org.openlca.ilcd.util.LangString;

/**
 * The export of an openLCA flow property to an ILCD flow property data set.
 */
public class FlowPropertyExport {

	private FlowProperty flowProperty;
	private IDatabase database;
	private DataStore dataStore;
	private String baseUri;

	public FlowPropertyExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public org.openlca.ilcd.flowproperties.FlowProperty run(
			FlowProperty flowProperty) throws DataStoreException {
		this.flowProperty = flowProperty;
		DataSetInformation dataSetInfo = makeDataSetInfo();
		DataSetReference unitGroupRef = makeUnitGroupRef();
		org.openlca.ilcd.flowproperties.FlowProperty iProperty = FlowPropertyBuilder
				.makeFlowProperty().withBaseUri(baseUri)
				.withDataSetInfo(dataSetInfo)
				.withUnitGroupReference(unitGroupRef).getFlowProperty();
		dataStore.put(iProperty, flowProperty.getRefId());
		this.flowProperty = null;
		return iProperty;
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(flowProperty.getRefId());
		LangString.addLabel(dataSetInfo.getName(), flowProperty.getName());
		if (flowProperty.getDescription() != null) {
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					flowProperty.getDescription());
		}
		CategoryConverter converter = new CategoryConverter();
		ClassificationInformation classInfo = converter
				.getClassificationInformation(flowProperty.getCategory());
		dataSetInfo.setClassificationInformation(classInfo);
		return dataSetInfo;
	}

	private DataSetReference makeUnitGroupRef() {
		UnitGroup unitGroup = flowProperty.getUnitGroup();
		return ExportDispatch
				.forwardExportCheck(unitGroup, database, dataStore);
	}

}
