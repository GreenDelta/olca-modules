package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataEntry;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
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
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.uuid;
		return null;
	}

	public String getName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.name, config);
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.generalComment, config);
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInfo info = getDataSetInformation();
		if (info != null) {
			ClassificationInfo classInfo = info.classificationInformation;
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	public DataSetReference getUnitGroupReference() {
		FlowPropertyInfo info = flowProperty.flowPropertyInformation;
		if (info != null) {
			QuantitativeReference qRef = info.quantitativeReference;
			if (qRef != null) {
				return qRef.unitGroup;
			}
		}
		return null;
	}

	private DataSetInfo getDataSetInformation() {
		if (flowProperty.flowPropertyInformation != null)
			return flowProperty.flowPropertyInformation.dataSetInformation;
		return null;
	}

	public String getVersion() {
		if (flowProperty == null)
			return null;
		AdminInfo info = flowProperty.administrativeInformation;
		if (info == null)
			return null;
		Publication pub = info.publication;
		if (pub == null)
			return null;
		else
			return pub.dataSetVersion;
	}

	public Date getTimeStamp() {
		if (flowProperty == null)
			return null;
		AdminInfo info = flowProperty.administrativeInformation;
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
