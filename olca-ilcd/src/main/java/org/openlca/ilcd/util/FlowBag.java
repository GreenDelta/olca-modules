package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
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
import org.openlca.ilcd.flows.FlowPropertyList;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.flows.QuantitativeReference;

public class FlowBag implements IBag<Flow> {

	private Flow flow;
	private IlcdConfig config;

	public FlowBag(Flow flow, IlcdConfig config) {
		this.flow = flow;
		this.config = config;
	}

	@Override
	public Flow getValue() {
		return flow;
	}

	@Override
	public String getId() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.uuid;
		return null;
	}

	public String getName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			FlowName flowName = info.name;
			if (flowName != null) {
				return LangString.getVal(flowName.baseName, config);
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
			return LangString.getVal(info.generalComment, config);
		return null;
	}

	public Integer getReferenceFlowPropertyId() {
		FlowInfo info = flow.flowInformation;
		if (info != null) {
			QuantitativeReference qRef = info.getQuantitativeReference();
			if (qRef != null && qRef.referenceFlowProperty != null) {
				return qRef.referenceFlowProperty.intValue();
			}
		}
		return null;
	}

	public FlowType getFlowType() {
		ModellingAndValidation mav = flow.modellingAndValidation;
		if (mav != null) {
			LCIMethod method = mav.lciMethod;
			if (method != null)
				return method.flowType;
		}
		return null;
	}

	public List<FlowPropertyRef> getFlowPropertyReferences() {
		FlowPropertyList list = flow.flowProperties;
		if (list != null) {
			return list.flowProperty;
		}
		return Collections.emptyList();
	}

	public List<org.openlca.ilcd.commons.Class> getSortedClasses() {
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			FlowCategoryInfo categoryInfo = info.classificationInformation;
			if (categoryInfo != null) {
				List<Classification> classifications = categoryInfo.classifications;
				if (classifications != null && classifications.size() > 0) {
					return ClassList.sortedList(classifications.get(0));
				}
			}
		}
		return Collections.emptyList();
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
		FlowInfo info = flow.flowInformation;
		if (info == null)
			return Collections.emptyList();
		Geography geo = info.getGeography();
		if (geo == null)
			return Collections.emptyList();
		else
			return geo.location;
	}

	public String getSynonyms() {
		DataSetInfo info = getDataSetInformation();
		if (info == null)
			return null;
		return LangString.getVal(info.synonyms, config);
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
		if (flow.flowInformation != null)
			return flow.flowInformation.getDataSetInformation();
		return null;
	}

	public String getVersion() {
		if (flow == null)
			return null;
		AdminInfo info = flow.administrativeInformation;
		if (info == null)
			return null;
		Publication pub = info.publication;
		if (pub == null)
			return null;
		else
			return pub.dataSetVersion;
	}

	public Date getTimeStamp() {
		if (flow == null)
			return null;
		AdminInfo info = flow.administrativeInformation;
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
