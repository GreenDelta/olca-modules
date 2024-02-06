package org.openlca.io.ilcd.output;

import org.openlca.core.database.FileStore;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SourceExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Export exp;
	private String baseUri;
	private org.openlca.core.model.Source source;

	public SourceExport(Export exp) {
		this.exp = exp;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void run(org.openlca.core.model.Source source) {
		if (source == null || exp.store.contains(Source.class, source.refId))
			return;
		this.source = source;
		log.trace("Run source export with {}", source);
		var ds = new Source()
				.withVersion("1.1")
				.withAdminInfo(makeAdminInfo());
		var info = makeDateSetInfo();
		ds.withSourceInfo().withDataSetInfo(info);
		File extFile = getExternalFile();
		if (extFile == null)
			exp.store.put(ds);
		else {
			addFileRef(info, extFile);
			exp.store.put(ds, new File[]{extFile});
		}
	}

	private File getExternalFile() {
		String name = source.externalFile;
		if (name == null)
			return null;
		File dbDir = exp.db.getFileStorageLocation();
		if (dbDir == null)
			return null;
		String path = FileStore.getPath(ModelType.SOURCE, source.refId);
		File docDir = new File(dbDir, path);
		if (!docDir.exists())
			return null;
		File file = new File(docDir, name);
		return file.exists() ? file : null;
	}

	private DataSetInfo makeDateSetInfo() {
		var info = new DataSetInfo()
				.withUUID(source.refId);
		exp.add(info::withName, source.name);
		exp.add(info::withDescription, source.description);
		addTextReference(info);
		Categories.toClassification(
				source.category, info::withClassifications);
		return info;
	}

	private void addTextReference(DataSetInfo dataSetInfo) {
		String cit = source.textReference;
		if (cit == null)
			return;
		if (source.year != null) {
			cit += " " + source.year;
		}
		dataSetInfo.withCitation(cit);
	}

	private void addFileRef(DataSetInfo info, File extFile) {
		info.withFiles().add(
				new FileRef()
						.withUri("../external_docs/" + extFile.getName()));
	}

	private AdminInfo makeAdminInfo() {
		var info = new AdminInfo();
		info.withDataEntry()
				.withTimeStamp(Xml.calendar(source.lastChange))
				.withFormats()
				.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		info.withPublication()
				.withVersion(Version.asString(source.version))
				.withUri(baseUri + "sources/" + source.refId);
	}
}
