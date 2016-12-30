package org.openlca.ilcd.util;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.AdminInfo;
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
		return flowProperty == null ? null : flowProperty.getUUID();
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

	public List<Category> getSortedClasses() {
		return ClassList.sortedList(flowProperty);
	}

	public Ref getUnitGroupReference() {
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
		return flowProperty.getVersion();
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
