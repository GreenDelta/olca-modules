package org.openlca.ilcd.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;

public class SourceBag implements IBag<Source> {

	private Source source;
	private String[] langs;

	public SourceBag(Source source, String... langs) {
		this.source = source;
		this.langs = langs;
	}

	@Override
	public Source getValue() {
		return source;
	}

	@Override
	public String getId() {
		return source == null ? null : source.getUUID();
	}

	public String getShortName() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.name, langs);
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.description, langs);
		return null;
	}

	public String getSourceCitation() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.citation;
		return null;
	}

	public List<Category> getSortedClasses() {
		return ClassList.sortedList(source);
	}

	public List<String> getExternalFileURIs() {
		DataSetInfo info = getDataSetInformation();
		if (info == null)
			return Collections.emptyList();
		List<FileRef> refs = info.files;
		List<String> uris = new ArrayList<>();
		for (FileRef ref : refs) {
			if (ref.uri != null)
				uris.add(ref.uri);
		}
		return uris;
	}

	private DataSetInfo getDataSetInformation() {
		if (source.sourceInfo != null)
			return source.sourceInfo.dataSetInfo;
		return null;
	}

	public String getVersion() {
		if (source == null)
			return null;
		return source.getVersion();
	}

	public Date getTimeStamp() {
		if (source == null)
			return null;
		AdminInfo info = source.adminInfo;
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
