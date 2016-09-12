package org.openlca.ilcd.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.sources.AdministrativeInformation;
import org.openlca.ilcd.sources.DataEntry;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.DigitalFileReference;
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
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getUUID();
		return null;
	}

	public String getShortName() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getShortName(), config);
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getSourceDescriptionOrComment(), config);
		return null;
	}

	public String getSourceCitation() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getSourceCitation();
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

	public List<String> getExternalFileURIs() {
		DataSetInformation info = getDataSetInformation();
		if (info == null)
			return Collections.emptyList();
		List<DigitalFileReference> refs = info.getReferenceToDigitalFile();
		List<String> uris = new ArrayList<>();
		for (DigitalFileReference ref : refs) {
			if (ref.getUri() != null)
				uris.add(ref.getUri());
		}
		return uris;
	}

	private DataSetInformation getDataSetInformation() {
		if (source.getSourceInformation() != null)
			return source.getSourceInformation().getDataSetInformation();
		return null;
	}

	public String getVersion() {
		if (source == null)
			return null;
		AdministrativeInformation info = source.getAdministrativeInformation();
		if (info == null)
			return null;
		Publication pub = info.getPublicationAndOwnership();
		if (pub == null)
			return null;
		else
			return pub.getDataSetVersion();
	}

	public Date getTimeStamp() {
		if (source == null)
			return null;
		AdministrativeInformation info = source.getAdministrativeInformation();
		if (info == null)
			return null;
		DataEntry entry = info.getDataEntryBy();
		if (entry == null)
			return null;
		XMLGregorianCalendar cal = entry.getTimeStamp();
		if (cal == null)
			return null;
		else
			return cal.toGregorianCalendar().getTime();
	}

}
