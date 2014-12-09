package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInformation;
import org.openlca.ilcd.flowproperties.QuantitativeReference;

public class FlowPropertyBag implements IBag<FlowProperty> {

	private FlowProperty flowProperty;

	public FlowPropertyBag(FlowProperty flowProperty) {
		this.flowProperty = flowProperty;
	}

	@Override
	public FlowProperty getValue() {
		return flowProperty;
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
		if (info != null)
			return LangString.get(info.getName());
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getGeneralComment());
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			ClassificationInformation classInfo = info
					.getClassificationInformation();
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	public DataSetReference getUnitGroupReference() {
		FlowPropertyInformation info = flowProperty
				.getFlowPropertyInformation();
		if (info != null) {
			QuantitativeReference qRef = info.getQuantitativeReference();
			if (qRef != null) {
				return qRef.getUnitGroup();
			}
		}
		return null;
	}

	private DataSetInformation getDataSetInformation() {
		if (flowProperty.getFlowPropertyInformation() != null)
			return flowProperty.getFlowPropertyInformation()
					.getDataSetInformation();
		return null;
	}

}
