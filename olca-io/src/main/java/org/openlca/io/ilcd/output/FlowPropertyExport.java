package org.openlca.io.ilcd.output;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.Refs;

public class FlowPropertyExport {

	private final ExportConfig config;
	private org.openlca.core.model.FlowProperty flowProperty;
	private String baseUri;

	public FlowPropertyExport(ExportConfig config) {
		this.config = config;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public FlowProperty run(org.openlca.core.model.FlowProperty property)
			throws DataStoreException {
		if (config.store.contains(FlowProperty.class, property.getRefId()))
			return config.store.get(FlowProperty.class, property.getRefId());
		this.flowProperty = property;
		FlowProperty iProperty = new FlowProperty();
		iProperty.version = "1.1";
		FlowPropertyInfo info = new FlowPropertyInfo();
		iProperty.flowPropertyInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		info.quantitativeReference = makeUnitGroupRef();
		iProperty.adminInfo = makeAdminInfo();
		config.store.put(iProperty);
		this.flowProperty = null;
		return iProperty;
	}

	private DataSetInfo makeDataSetInfo() {
		DataSetInfo dataSetInfo = new DataSetInfo();
		dataSetInfo.uuid = flowProperty.getRefId();
		LangString.set(dataSetInfo.name, flowProperty.getName(),
				config.lang);
		if (flowProperty.getDescription() != null) {
			LangString.set(dataSetInfo.generalComment,
					flowProperty.getDescription(), config.lang);
		}
		CategoryConverter converter = new CategoryConverter();
		Classification c = converter.getClassification(
				flowProperty.getCategory());
		if (c != null)
			dataSetInfo.classifications.add(c);
		return dataSetInfo;
	}

	private QuantitativeReference makeUnitGroupRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		UnitGroup unitGroup = flowProperty.getUnitGroup();
		Ref ref = ExportDispatch.forwardExportCheck(unitGroup,
				config);
		qRef.unitGroup = ref;
		return qRef;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Out.getTimestamp(flowProperty);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(flowProperty.getVersion());
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "flowproperties/" + flowProperty.getRefId();
	}

}
