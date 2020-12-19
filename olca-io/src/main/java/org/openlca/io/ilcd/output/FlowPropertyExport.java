package org.openlca.io.ilcd.output;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

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

	public FlowProperty run(org.openlca.core.model.FlowProperty property) {
		if (config.store.contains(FlowProperty.class, property.refId))
			return config.store.get(FlowProperty.class, property.refId);
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
		dataSetInfo.uuid = flowProperty.refId;
		LangString.set(dataSetInfo.name, flowProperty.name,
				config.lang);
		if (flowProperty.description != null) {
			LangString.set(dataSetInfo.generalComment,
					flowProperty.description, config.lang);
		}
		CategoryConverter converter = new CategoryConverter();
		Classification c = converter.getClassification(
				flowProperty.category);
		if (c != null)
			dataSetInfo.classifications.add(c);
		return dataSetInfo;
	}

	private QuantitativeReference makeUnitGroupRef() {
		QuantitativeReference qRef = new QuantitativeReference();
		UnitGroup unitGroup = flowProperty.unitGroup;
		qRef.unitGroup = Export.of(unitGroup, config);
		return qRef;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Xml.calendar(flowProperty.lastChange);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(flowProperty.version);
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "flowproperties/" + flowProperty.refId;
	}

}
