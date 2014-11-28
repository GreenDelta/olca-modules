package org.openlca.ilcd.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.DigitalFileReference;
import org.openlca.ilcd.sources.Source;

public class SourceBag implements IBag<Source> {

	private Source source;

	public SourceBag(Source source) {
		this.source = source;
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
			return LangString.get(info.getShortName());
		return null;
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getSourceDescriptionOrComment());
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
			ClassificationInformation classInfo = info
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

}
