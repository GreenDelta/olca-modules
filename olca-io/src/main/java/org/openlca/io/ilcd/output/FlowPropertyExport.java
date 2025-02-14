package org.openlca.io.ilcd.output;

import org.openlca.core.model.Version;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

public class FlowPropertyExport {

	private final Export exp;
	private final org.openlca.core.model.FlowProperty property;
	private String baseUri;

	public FlowPropertyExport(
			Export exp, org.openlca.core.model.FlowProperty property
	) {
		this.exp = exp;
		this.property = property;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void run() {
		if (exp.store.contains(FlowProperty.class, property.refId))
			return;
		var iProp = new FlowProperty()
				.withAdminInfo(makeAdminInfo());
		iProp.withFlowPropertyInfo()
				.withDataSetInfo(makeDataSetInfo())
				.withQuantitativeReference()
				.withUnitGroup(exp.writeRef(property.unitGroup));
		exp.store.put(iProp);
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo()
				.withUUID(property.refId);
		exp.add(info::withName, property.name);
		exp.add(info::withComment, property.description);
		Categories.toClassification(
				property.category, info::withClassifications);
		return info;
	}

	private AdminInfo makeAdminInfo() {
		var info = new AdminInfo();
		info.withDataEntry()
				.withTimeStamp(Xml.calendar(property.lastChange))
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
				.withVersion(Version.asString(property.version))
				.withUri(baseUri + "flowproperties/" + property.refId);
	}
}
