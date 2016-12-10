package org.openlca.ilcd.util;

import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.openlca.ilcd.flows.Technology;

public final class Flows {

	private Flows() {
	}

	public static AdminInfo getAdminInfo(Flow f) {
		if (f == null)
			return null;
		return f.adminInfo;
	}

	public static AdminInfo adminInfo(Flow f) {
		if (f.adminInfo == null)
			f.adminInfo = new AdminInfo();
		return f.adminInfo;
	}

	public static DataEntry getDataEntry(Flow f) {
		AdminInfo ai = getAdminInfo(f);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(Flow f) {
		AdminInfo ai = adminInfo(f);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static Publication getPublication(Flow f) {
		AdminInfo ai = getAdminInfo(f);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(Flow f) {
		AdminInfo ai = adminInfo(f);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

	public static FlowInfo getFlowInfo(Flow f) {
		if (f == null)
			return null;
		return f.flowInfo;
	}

	public static FlowInfo flowInfo(Flow f) {
		if (f.flowInfo == null)
			f.flowInfo = new FlowInfo();
		return f.flowInfo;
	}

	public static DataSetInfo getDataSetInfo(Flow f) {
		FlowInfo fi = getFlowInfo(f);
		if (fi == null)
			return null;
		return fi.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(Flow f) {
		FlowInfo fi = flowInfo(f);
		if (fi.dataSetInfo == null)
			fi.dataSetInfo = new DataSetInfo();
		return fi.dataSetInfo;
	}

	public static QuantitativeReference getQuantitativeReference(Flow f) {
		FlowInfo fi = getFlowInfo(f);
		if (fi == null)
			return null;
		return fi.quantitativeReference;
	}

	public static QuantitativeReference quantitativeReference(Flow f) {
		FlowInfo fi = flowInfo(f);
		if (fi.quantitativeReference == null)
			fi.quantitativeReference = new QuantitativeReference();
		return fi.quantitativeReference;
	}

	public static Geography getGeography(Flow f) {
		FlowInfo fi = getFlowInfo(f);
		if (fi == null)
			return null;
		return fi.geography;
	}

	public static Geography geography(Flow f) {
		FlowInfo fi = flowInfo(f);
		if (fi.geography == null)
			fi.geography = new Geography();
		return fi.geography;
	}

	public static Technology getTechnology(Flow f) {
		FlowInfo fi = getFlowInfo(f);
		if (fi == null)
			return null;
		return fi.technology;
	}

	public static Technology technology(Flow f) {
		FlowInfo fi = flowInfo(f);
		if (fi.technology == null)
			fi.technology = new Technology();
		return fi.technology;
	}

}
