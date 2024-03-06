package org.openlca.io.ilcd.output;

import org.openlca.core.model.Version;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

public class FlowPropertyExport {

	private final Export exp;
	private org.openlca.core.model.FlowProperty flowProperty;
	private String baseUri;

	public FlowPropertyExport(Export exp) {
		this.exp = exp;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void run(org.openlca.core.model.FlowProperty property) {
		if (property == null
				|| exp.store.contains(FlowProperty.class, property.refId))
			return;
		this.flowProperty = property;
		var iProperty = new FlowProperty()
				.withAdminInfo(makeAdminInfo());
		iProperty.withFlowPropertyInfo()
				.withDataSetInfo(makeDataSetInfo())
				.withQuantitativeReference()
				.withUnitGroup(exp.writeRef(flowProperty.unitGroup));
		exp.store.put(iProperty);
		this.flowProperty = null;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo()
				.withUUID(flowProperty.refId);
		exp.add(info::withName, flowProperty.name);
		exp.add(info::withComment, flowProperty.description);
		Categories.toClassification(
				flowProperty.category, info::withClassifications);
		return info;
	}

	private AdminInfo makeAdminInfo() {
		var info = new AdminInfo();
		info.withDataEntry()
				.withTimeStamp(Xml.calendar(flowProperty.lastChange))
				.withFormats()
				.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		info.withPublication()
				.withVersion(Version.asString(flowProperty.version))
				.withUri(baseUri + "flowproperties/" + flowProperty.refId);
	}
}
