package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.FlowCategorization;
import org.openlca.ilcd.commons.FlowCategoryInformation;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.flows.AdministrativeInformation;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInformation;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInformation;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyList;
import org.openlca.ilcd.flows.FlowPropertyReference;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.flows.QuantitativeReference;

public class FlowBag implements IBag<Flow> {

	private Flow flow;

	public FlowBag(Flow flow) {
		this.flow = flow;
	}

	@Override
	public Flow getValue() {
		return flow;
	}

	@Override
	public String getId() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getUUID();
		return null;
	}

	public String getName() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			FlowName flowName = info.getName();
			if (flowName != null) {
				return LangString.get(flowName.getBaseName());
			}
		}
		return null;
	}

	public String getCasNumber() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getCASNumber();
		return null;
	}

	public String getSumFormula() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getSumFormula();
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getGeneralComment());
		return null;
	}

	public Integer getReferenceFlowPropertyId() {
		FlowInformation info = flow.getFlowInformation();
		if (info != null) {
			QuantitativeReference qRef = info.getQuantitativeReference();
			if (qRef != null && qRef.getReferenceFlowProperty() != null) {
				return qRef.getReferenceFlowProperty().intValue();
			}
		}
		return null;
	}

	public FlowType getFlowType() {
		ModellingAndValidation mav = flow.getModellingAndValidation();
		if (mav != null) {
			LCIMethod method = mav.getLCIMethod();
			if (method != null)
				return method.getFlowType();
		}
		return null;
	}

	public List<FlowPropertyReference> getFlowPropertyReferences() {
		FlowPropertyList list = flow.getFlowProperties();
		if (list != null) {
			return list.getFlowProperty();
		}
		return Collections.emptyList();
	}

	public List<org.openlca.ilcd.commons.Class> getSortedClasses() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			FlowCategoryInformation categoryInfo = info
					.getClassificationInformation();
			if (categoryInfo != null) {
				List<Classification> classifications = categoryInfo
						.getClassifications();
				if (classifications != null && classifications.size() > 0) {
					return ClassList.sortedList(classifications.get(0));
				}
			}
		}
		return Collections.emptyList();
	}

	public List<Category> getSortedCompartments() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			FlowCategoryInformation categoryInfo = info
					.getClassificationInformation();
			return getCompartments(categoryInfo);
		}
		return Collections.emptyList();
	}

	public List<Label> getLocation() {
		FlowInformation info = flow.getFlowInformation();
		if (info == null)
			return Collections.emptyList();
		Geography geo = info.getGeography();
		if (geo == null)
			return Collections.emptyList();
		else
			return geo.getLocation();
	}

	private List<Category> getCompartments(FlowCategoryInformation categoryInfo) {
		if (categoryInfo != null) {
			List<FlowCategorization> categorizations = categoryInfo
					.getElementaryFlowCategorizations();
			if (categorizations != null && categorizations.size() > 0) {
				FlowCategorization categorization = categorizations.get(0);
				List<Category> categories = categorization.getCategories();
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
				if (cat1.getLevel() != null && cat2.getLevel() != null) {
					c = cat1.getLevel().compareTo(cat2.getLevel());
				}
				return c;
			}
		});
	}

	private DataSetInformation getDataSetInformation() {
		if (flow.getFlowInformation() != null)
			return flow.getFlowInformation().getDataSetInformation();
		return null;
	}

	public String getVersion() {
		if (flow == null)
			return null;
		AdministrativeInformation info = flow.getAdministrativeInformation();
		if (info == null)
			return null;
		Publication pub = info.getPublication();
		if (pub == null)
			return null;
		else
			return pub.getDataSetVersion();
	}

	public Date getTimeStamp() {
		if (flow == null)
			return null;
		AdministrativeInformation info = flow
				.getAdministrativeInformation();
		if (info == null)
			return null;
		DataEntry entry = info.getDataEntry();
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.getTimeStamp();
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
