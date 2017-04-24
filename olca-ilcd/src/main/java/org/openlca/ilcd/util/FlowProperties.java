package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;

public final class FlowProperties {

	private FlowProperties() {
	}

	public static FlowPropertyInfo getFlowPropertyInfo(FlowProperty fp) {
		if (fp == null)
			return null;
		return fp.flowPropertyInfo;
	}

	public static FlowPropertyInfo flowPropertyInfo(FlowProperty fp) {
		if (fp.flowPropertyInfo == null)
			fp.flowPropertyInfo = new FlowPropertyInfo();
		return fp.flowPropertyInfo;
	}

	public static QuantitativeReference getQuantitativeReference(
			FlowProperty fp) {
		FlowPropertyInfo fpi = getFlowPropertyInfo(fp);
		if (fpi == null)
			return null;
		return fpi.quantitativeReference;
	}

	public static QuantitativeReference quantitativeReference(FlowProperty fp) {
		FlowPropertyInfo fpi = flowPropertyInfo(fp);
		if (fpi.quantitativeReference == null)
			fpi.quantitativeReference = new QuantitativeReference();
		return fpi.quantitativeReference;
	}

	public static Ref getUnitGroupRef(FlowProperty fp) {
		QuantitativeReference qr = getQuantitativeReference(fp);
		if (qr == null)
			return null;
		return qr.unitGroup;
	}

	public static Ref unitGroupRef(FlowProperty fp) {
		QuantitativeReference qr = quantitativeReference(fp);
		if (qr.unitGroup == null) {
			qr.unitGroup = new Ref();
			qr.unitGroup.type = DataSetType.UNIT_GROUP;
		}
		return qr.unitGroup;
	}

	public static DataSetInfo getDataSetInfo(FlowProperty fp) {
		FlowPropertyInfo fpi = getFlowPropertyInfo(fp);
		if (fpi == null)
			return null;
		return fpi.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(FlowProperty fp) {
		FlowPropertyInfo fpi = flowPropertyInfo(fp);
		if (fpi.dataSetInfo == null)
			fpi.dataSetInfo = new DataSetInfo();
		return fpi.dataSetInfo;
	}

	public static AdminInfo getAdminInfo(FlowProperty fp) {
		if (fp == null)
			return null;
		return fp.adminInfo;
	}

	public static AdminInfo adminInfo(FlowProperty fp) {
		if (fp.adminInfo == null)
			fp.adminInfo = new AdminInfo();
		return fp.adminInfo;
	}

	public static DataEntry getDataEntry(FlowProperty fp) {
		AdminInfo ai = getAdminInfo(fp);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(FlowProperty fp) {
		AdminInfo ai = adminInfo(fp);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static Publication getPublication(FlowProperty fp) {
		AdminInfo ai = getAdminInfo(fp);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(FlowProperty fp) {
		AdminInfo ai = adminInfo(fp);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

}
