package org.openlca.io.ilcd.output;

import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

public class FlowPropertyExport {

	private final ILCDExport exp;
	private org.openlca.core.model.FlowProperty flowProperty;
	private String baseUri;

	public FlowPropertyExport(ILCDExport exp) {
		this.exp = exp;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public FlowProperty run(org.openlca.core.model.FlowProperty property) {
		if (exp.store.contains(FlowProperty.class, property.refId))
			return exp.store.get(FlowProperty.class, property.refId);
		this.flowProperty = property;
		FlowProperty iProperty = new FlowProperty();
		iProperty.version = "1.1";
		FlowPropertyInfo info = new FlowPropertyInfo();
		iProperty.flowPropertyInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		info.quantitativeReference = makeUnitGroupRef();
		iProperty.adminInfo = makeAdminInfo();
		exp.store.put(iProperty);
		this.flowProperty = null;
		return iProperty;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo();
		info.uuid = flowProperty.refId;
		exp.add(info.name, flowProperty.name);
		exp.add(info.generalComment, flowProperty.description);
		var converter = new CategoryConverter();
		var c = converter.getClassification(flowProperty.category);
		if (c != null) {
			info.classifications.add(c);
		}
		return info;
	}

	private QuantitativeReference makeUnitGroupRef() {
		var qRef = new QuantitativeReference();
		var unitGroup = flowProperty.unitGroup;
		qRef.unitGroup = Export.of(unitGroup, exp);
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
