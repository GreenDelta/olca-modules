package org.openlca.ilcd.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataEntry;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.DigitalFileRef;
import org.openlca.ilcd.sources.Publication;
import org.openlca.ilcd.sources.Source;

public class SourceBag implements IBag<Source> {

	private Source source;
	private IlcdConfig config;

	public SourceBag(Source source, IlcdConfig config) {
		this.source = source;
		this.config = config;
	}

	@Override
	public Source getValue() {
		return source;
	}

	@Override
	public String getId() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.uuid;
		return null;
	}

	public String getShortName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.shortName, config);
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getVal(info.sourceDescriptionOrComment, config);
		return null;
	}

	public String getSourceCitation() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.sourceCitation;
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

	public List<String> getExternalFileURIs() {
		DataSetInfo info = getDataSetInformation();
		if (info == null)
			return Collections.emptyList();
		List<DigitalFileRef> refs = info.referenceToDigitalFile;
		List<String> uris = new ArrayList<>();
		for (DigitalFileRef ref : refs) {
			if (ref.uri != null)
				uris.add(ref.uri);
		}
		return uris;
	}

	private DataSetInfo getDataSetInformation() {
		if (source.sourceInformation != null)
			return source.sourceInformation.dataSetInformation;
		return null;
	}

	public String getVersion() {
		if (source == null)
			return null;
		AdminInfo info = source.administrativeInformation;
		if (info == null)
			return null;
		Publication pub = info.publicationAndOwnership;
		if (pub == null)
			return null;
		else
			return pub.dataSetVersion;
	}

	public Date getTimeStamp() {
		if (source == null)
			return null;
		AdminInfo info = source.administrativeInformation;
		if (info == null)
			return null;
		DataEntry entry = info.dataEntryBy;
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.timeStamp;
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
