package org.openlca.ilcd;

import java.util.UUID;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Publication;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;

public final class SampleSource {

	private SampleSource() {
	}

	public static Source create() {
		Source source = new Source();
		SourceInfo info = new SourceInfo();
		source.sourceInformation = info;
		info.dataSetInformation = makeDataInfo();
		source.administrativeInformation = makeAdminInfo();
		return source;
	}

	private static DataSetInfo makeDataInfo() {
		String id = UUID.randomUUID().toString();
		DataSetInfo info = new DataSetInfo();
		LangString.set(info.shortName, "test source", "en");
		info.uuid = id;
		return info;
	}

	private static AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		Publication pub = new Publication();
		info.publicationAndOwnership = pub;
		pub.dataSetVersion = "01.00.101";
		pub.permanentDataSetURI = "http://openlca.org/ilcd/resource/mytestsource";
		return info;
	}

}
