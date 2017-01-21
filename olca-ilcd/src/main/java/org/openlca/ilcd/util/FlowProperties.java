package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;

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
