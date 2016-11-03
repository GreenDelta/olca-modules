package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.FlowCategorization;
import org.openlca.ilcd.commons.FlowCategoryInfo;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyRef;
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

	public List<FlowPropertyRef> getFlowPropertyReferences() {
		return flow.flowProperties;
	}

	public List<org.openlca.ilcd.commons.Class> getSortedClasses() {
		return ClassList.sortedList(flow);
	}

	public List<Category> getSortedCompartments() {
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

	private List<Category> getCompartments(FlowCategoryInfo categoryInfo) {
		if (categoryInfo != null) {
			List<FlowCategorization> categorizations = categoryInfo.elementaryFlowCategorizations;
			if (categorizations != null && categorizations.size() > 0) {
				FlowCategorization categorization = categorizations.get(0);
				List<Category> categories = categorization.categories;
				if (categories != null && categories.size() > 0) {
					sort(categories);
					return categories;
				}
			}
		}
		return Collections.emptyList();
	}

	private void sort(List<Category> categories) {
		Collections.sort(categories, new Comparator<Category>() {
			@Override
			public int compare(Category cat1, Category cat2) {
				int c = 0;
				if (cat1.level != null && cat2.level != null) {
					c = cat1.level.compareTo(cat2.level);
				}
				return c;
			}
		});
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
