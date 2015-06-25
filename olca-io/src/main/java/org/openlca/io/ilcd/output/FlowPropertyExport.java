package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.AdministrativeInformation;
import org.openlca.ilcd.flowproperties.DataEntry;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.flowproperties.FlowPropertyInformation;
import org.openlca.ilcd.flowproperties.Publication;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;

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
		org.openlca.ilcd.flowproperties.FlowProperty iProperty = new org.openlca.ilcd.flowproperties.FlowProperty();
		iProperty.setVersion("1.1");
		FlowPropertyInformation info = new FlowPropertyInformation();
		iProperty.setFlowPropertyInformation(info);
		info.setDataSetInformation(makeDataSetInfo());
		info.setQuantitativeReference(makeUnitGroupRef());
		iProperty.setAdministrativeInformation(makeAdminInfo());
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

	private QuantitativeReference makeUnitGroupRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		UnitGroup unitGroup = flowProperty.getUnitGroup();
		DataSetReference ref = ExportDispatch.forwardExportCheck(unitGroup,
				database, dataStore);
		qRef.setUnitGroup(ref);
		return qRef;
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		DataEntry entry = new DataEntry();
		info.setDataEntry(entry);
		entry.setTimeStamp(Out.getTimestamp(flowProperty));
		entry.getReferenceToDataSetFormat().add(Reference.forIlcdFormat());
		addPublication(info);
		return info;
	}

	private void addPublication(AdministrativeInformation info) {
		Publication pub = new Publication();
		info.setPublication(pub);
		pub.setDataSetVersion(Version.asString(flowProperty.getVersion()));
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.setPermanentDataSetURI(baseUri + "flowproperties/"
				+ flowProperty.getRefId());
	}

}
