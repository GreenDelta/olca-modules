package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.AdministrativeInformation;
import org.openlca.ilcd.flowproperties.DataEntry;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInformation;
import org.openlca.ilcd.flowproperties.Publication;
import org.openlca.ilcd.flowproperties.QuantitativeReference;

public class FlowPropertyBag implements IBag<FlowProperty> {

	private FlowProperty flowProperty;
	private IlcdConfig config;

	public FlowPropertyBag(FlowProperty flowProperty, IlcdConfig config) {
		this.flowProperty = flowProperty;
		this.config = config;
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
			return LangString.get(info.getName(), config);
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getGeneralComment(), config);
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			ClassificationInfo classInfo = info
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

	public String getVersion() {
		if (flowProperty == null)
			return null;
		AdministrativeInformation info = flowProperty
				.getAdministrativeInformation();
		if (info == null)
			return null;
		Publication pub = info.getPublication();
		if (pub == null)
			return null;
		else
			return pub.getDataSetVersion();
	}

	public Date getTimeStamp() {
		if (flowProperty == null)
			return null;
		AdministrativeInformation info = flowProperty
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
