package org.openlca.validation;

import java.io.File;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.util.Dirs;

class SourceCheck implements Runnable {

	private final Validation v;
	private boolean foundIssues = false;

	SourceCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkFileRefs();
			if (!foundIssues && !v.wasCanceled()) {
				v.ok("checked sources");
			}
		} catch (Exception e) {
			v.error("error in source check", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkFileRefs() {
		if (v.wasCanceled())
			return;
		var q = "select " +
				/* 1 */  "id, " +
				/* 2 */  "ref_id, " +
				/* 3 */  "external_file from tbl_sources";
		NativeSql.on(v.db).query(q, r -> {
			long sourceId = r.getLong(1);
			var refId = r.getString(2);
			var file = r.getString(3);
			if (file != null && !fileExists(refId, file)) {
				v.warning(sourceId, ModelType.SOURCE,
						"referenced file does not exists: " + file);
				foundIssues = true;
			}
			return !v.wasCanceled();
		});
	}

	private boolean fileExists(String sourceId, String fileName) {
		if (sourceId == null || fileName == null)
			return false;
		var storeDir = v.db.getFileStorageLocation();
		if (!Dirs.isPresent(storeDir))
			return false;
		var store = new FileStore(storeDir);
		var modelDir = store.getFolder(ModelType.SOURCE, sourceId);
		if (!Dirs.isPresent(modelDir))
			return false;
		var file = new File(modelDir, fileName);
		return file.exists() && file.isFile();
	}
}
