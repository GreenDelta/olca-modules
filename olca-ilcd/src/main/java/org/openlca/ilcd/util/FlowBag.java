package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.CompartmentList;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowCategoryInfo;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.openlca.ilcd.flows.QuantitativeReference;

public class FlowBag implements IBag<Flow> {

	private Flow flow;
	private String[] langs;

	public FlowBag(Flow flow, String... langs) {
		this.flow = flow;
		this.langs = langs;
	}

	@Override
	public Flow getValue() {
		return flow;
	}

	@Override
	public String getId() {
		return flow == null ? null : flow.getUUID();
	}

	public String getName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			FlowName flowName = info.name;
			if (flowName != null) {
				return LangString.getFirst(flowName.baseName, langs);
			}
		}
		return null;
	}

	public String getCasNumber() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.casNumber;
		return null;
	}

	public String getSumFormula() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.sumFormula;
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.generalComment, langs);
		return null;
	}

	public Integer getReferenceFlowPropertyId() {
		FlowInfo info = flow.flowInfo;
		if (info != null) {
			QuantitativeReference qRef = info.quantitativeReference;
			if (qRef != null && qRef.referenceFlowProperty != null) {
				return qRef.referenceFlowProperty.intValue();
			}
		}
		return null;
	}

	public FlowType getFlowType() {
		Modelling mav = flow.modelling;
		if (mav != null) {
			LCIMethod method = mav.lciMethod;
			if (method != null)
				return method.flowType;
		}
		return null;
	}

	public List<org.openlca.ilcd.commons.Category> getSortedClasses() {
		return ClassList.sortedList(flow);
	}

	public List<Compartment> getSortedCompartments() {
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			FlowCategoryInfo categoryInfo = info.classificationInformation;
			return getCompartments(categoryInfo);
		}
		return Collections.emptyList();
	}

	public List<LangString> getLocation() {
		FlowInfo info = flow.flowInfo;
		if (info == null)
			return Collections.emptyList();
		Geography geo = info.geography;
		if (geo == null)
			return Collections.emptyList();
		else
			return geo.location;
	}

	public String getSynonyms() {
		DataSetInfo info = getDataSetInformation();
		if (info == null)
			return null;
		return LangString.getFirst(info.synonyms, langs);
	}

	private List<Compartment> getCompartments(FlowCategoryInfo categoryInfo) {
		if (categoryInfo != null) {
			List<CompartmentList> categorizations = categoryInfo.compartmentLists;
			if (categorizations != null && categorizations.size() > 0) {
				CompartmentList categorization = categorizations.get(0);
				List<Compartment> categories = categorization.compartments;
				if (categories != null && categories.size() > 0) {
					Collections.sort(categories, (c1, c2) -> c1.level - c2.level);
					return categories;
				}
			}
		}
		return Collections.emptyList();
	}

	private DataSetInfo getDataSetInformation() {
		if (flow.flowInfo != null)
			return flow.flowInfo.dataSetInfo;
		return null;
	}

	public String getVersion() {
		if (flow == null)
			return null;
		return flow.getVersion();
	}

	public Date getTimeStamp() {
		if (flow == null)
			return null;
		AdminInfo info = flow.adminInfo;
		if (info == null)
			return null;
		DataEntry entry = info.dataEntry;
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.timeStamp;
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
