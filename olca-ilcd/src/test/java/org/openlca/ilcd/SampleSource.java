package org.openlca.ilcd;

import java.util.UUID;

import org.openlca.ilcd.sources.AdministrativeInformation;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.Publication;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInformation;
import org.openlca.ilcd.util.LangString;

public final class SampleSource {

	private SampleSource() {
	}

	public static Source create() {
		Source source = new Source();
		SourceInformation info = new SourceInformation();
		source.setSourceInformation(info);
		info.setDataSetInformation(makeDataInfo());
		source.setAdministrativeInformation(makeAdminInfo());
		return source;
	}

	private static DataSetInformation makeDataInfo() {
		String id = UUID.randomUUID().toString();
		DataSetInformation info = new DataSetInformation();
		LangString.addLabel(info.getShortName(), "test source");
		info.setUUID(id);
		return info;
	}

	private static AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		Publication pub = new Publication();
		info.setPublicationAndOwnership(pub);
		pub.setDataSetVersion("01.00.101");
		pub.setPermanentDataSetURI("http://openlca.org/ilcd/resource/mytestsource");
		return info;
	}

}
