package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.AdminInfo;
import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;

public class FlowPropertyBag implements IBag<FlowProperty> {

	private FlowProperty flowProperty;
	private String[] langs;

	public FlowPropertyBag(FlowProperty flowProperty, String... langs) {
		this.flowProperty = flowProperty;
		this.langs = langs;
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
			return LangString.getFirst(info.name, langs);
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.generalComment, langs);
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
		FlowPropertyInfo info = flowProperty.flowPropertyInfo;
		if (info != null) {
			QuantitativeReference qRef = info.quantitativeReference;
			if (qRef != null) {
				return qRef.unitGroup;
			}
		}
		return null;
	}

	private DataSetInfo getDataSetInformation() {
		if (flowProperty.flowPropertyInfo != null)
			return flowProperty.flowPropertyInfo.dataSetInfo;
		return null;
	}

	public String getVersion() {
		if (flowProperty == null)
			return null;
		AdminInfo info = flowProperty.adminInfo;
		if (info == null)
			return null;
		Publication pub = info.publication;
		if (pub == null)
			return null;
		else
			return pub.version;
	}

	public Date getTimeStamp() {
		if (flowProperty == null)
			return null;
		AdminInfo info = flowProperty.adminInfo;
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
