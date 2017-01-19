package org.openlca.ilcd.util;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Sources {

	private Sources() {
	}

	public static List<FileRef> getFileRefs(Source source) {
		if (source == null || source.sourceInfo == null)
			return Collections.emptyList();
		if (source.sourceInfo.dataSetInfo == null)
			return Collections.emptyList();
		return source.sourceInfo.dataSetInfo.files;
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
