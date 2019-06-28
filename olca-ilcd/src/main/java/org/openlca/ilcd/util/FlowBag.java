package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.Geography;

public class FlowBag implements IBag<Flow> {

	public final Flow flow;
	public final String[] langs;

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

	private DataSetInfo getDataSetInformation() {
		if (flow.flowInfo != null)
			return flow.flowInfo.dataSetInfo;
		return null;
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
