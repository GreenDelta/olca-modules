package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowCategoryInfo;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyList;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
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

	public static FlowName getFlowName(Flow f) {
		DataSetInfo dsi = getDataSetInfo(f);
		if (dsi == null)
			return null;
		return dsi.name;
	}

	public static FlowName flowName(Flow f) {
		DataSetInfo dsi = dataSetInfo(f);
		if (dsi.name == null)
			dsi.name = new FlowName();
		return dsi.name;
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

	public static List<Classification> getClassifications(Flow f) {
		DataSetInfo info = getDataSetInfo(f);
		if (info == null || info.classificationInformation == null)
			return Collections.emptyList();
		return info.classificationInformation.classifications;
	}

	public static List<Classification> classifications(Flow f) {
		DataSetInfo info = dataSetInfo(f);
		if (info.classificationInformation == null)
			info.classificationInformation = new FlowCategoryInfo();
		return info.classificationInformation.classifications;
	}

	public static Modelling getModelling(Flow f) {
		return f == null ? null : f.modelling;
	}

	public static Modelling modelling(Flow f) {
		if (f.modelling == null)
			f.modelling = new Modelling();
		return f.modelling;
	}

	public static LCIMethod getInventoryMethod(Flow f) {
		Modelling m = getModelling(f);
		return m == null ? null : m.lciMethod;
	}

	public static LCIMethod inventoryMethod(Flow f) {
		Modelling m = modelling(f);
		if (m.lciMethod == null)
			m.lciMethod = new LCIMethod();
		return m.lciMethod;
	}

	public static FlowType getType(Flow f) {
		LCIMethod m = getInventoryMethod(f);
		return m == null ? null : m.flowType;
	}

	public static List<FlowPropertyRef> getFlowProperties(Flow f) {
		if (f == null || f.flowPropertyList == null)
			return Collections.emptyList();
		return f.flowPropertyList.flowProperties;
	}

	public static List<FlowPropertyRef> flowProperties(Flow f) {
		if (f.flowPropertyList == null)
			f.flowPropertyList = new FlowPropertyList();
		return f.flowPropertyList.flowProperties;
	}

}
