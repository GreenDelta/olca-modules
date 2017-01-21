package org.openlca.ilcd.util;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Sources {

	private Sources() {
	}

	public static SourceInfo getSourceInfo(Source s) {
		if (s == null)
			return null;
		return s.sourceInfo;
	}

	public static SourceInfo sourceInfo(Source s) {
		if (s.sourceInfo == null)
			s.sourceInfo = new SourceInfo();
		return s.sourceInfo;
	}

	public static DataSetInfo getDataSetInfo(Source s) {
		SourceInfo si = getSourceInfo(s);
		if (si == null)
			return null;
		return si.dataSetInfo;
	}

	public static DataSetInfo dataSetInfo(Source s) {
		SourceInfo si = sourceInfo(s);
		if (si.dataSetInfo == null)
			si.dataSetInfo = new DataSetInfo();
		return si.dataSetInfo;
	}

	public static AdminInfo getAdminInfo(Source s) {
		if (s == null)
			return null;
		return s.adminInfo;
	}

	public static AdminInfo adminInfo(Source s) {
		if (s.adminInfo == null)
			s.adminInfo = new AdminInfo();
		return s.adminInfo;
	}

	public static DataEntry getDataEntry(Source s) {
		AdminInfo ai = getAdminInfo(s);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(Source s) {
		AdminInfo ai = adminInfo(s);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static List<FileRef> getFileRefs(Source source) {
		if (source == null || source.sourceInfo == null)
			return Collections.emptyList();
		if (source.sourceInfo.dataSetInfo == null)
			return Collections.emptyList();
		return source.sourceInfo.dataSetInfo.files;
	}

	public static Publication getPublication(Source s) {
		AdminInfo ai = getAdminInfo(s);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(Source s) {
		AdminInfo ai = adminInfo(s);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

	/**
	 * Returns the plain file name of the given file reference (the unescaped
	 * last part of the URI in the given file reference).
	 */
	public static String getFileName(FileRef ref) {
		if (ref == null || ref.uri == null)
			return null;
		try {
			String s = ref.uri.trim().replace('\\', '/');
			if (s.isEmpty())
				return null;
			int pos = s.lastIndexOf('/');
			if (pos != -1) {
				s = s.substring(pos + 1);
			}
			return URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Sources.class);
			log.error("could not get file name from " + ref.uri, e);
			return null;
		}
	}

}
