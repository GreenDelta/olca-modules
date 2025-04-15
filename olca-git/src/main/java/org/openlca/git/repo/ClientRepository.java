package org.openlca.git.repo;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.descriptors.Descriptors;
import org.openlca.core.model.Version;
import org.openlca.git.model.Reference;
import org.openlca.jsonld.Json;

public class ClientRepository extends OlcaRepository {

	public final HeadIndex index;
	public final IDatabase database;
	public final FileStore fileStore;
	public final Descriptors descriptors;

	public ClientRepository(File gitDir, IDatabase database, Descriptors descriptors) throws IOException {
		super(gitDir);
		this.index = HeadIndex.of(this);
		this.database = database;
		this.fileStore = new FileStore(database);
		this.descriptors = descriptors;
	}

	public boolean equalsWorkspace(Reference ref) {
		if (ref == null || ref.isCategory || ObjectId.zeroId().equals(ref.objectId))
			return false;
		var d = descriptors.get(ref);
		if (d == null)
			return false;
		var remoteModel = datasets.parse(ref, "lastChange", "version");
		if (remoteModel == null)
			return false;
		var version = Version.fromString(string(remoteModel, "version")).getValue();
		var lastChange = date(remoteModel, "lastChange");
		var category = descriptors.categoryPaths.pathOf(d.category);
		if (category == null) {
			category = "";
		}
		return version == d.version && lastChange == d.lastChange && category.equals(ref.category);
	}

	private String string(Map<String, Object> map, String field) {
		var value = map.get(field);
		if (value == null)
			return null;
		return value.toString();
	}

	private long date(Map<String, Object> map, String field) {
		var value = map.get(field);
		if (value == null)
			return 0;
		try {
			return Long.parseLong(value.toString());
		} catch (NumberFormatException e) {
			var date = Json.parseDate(value.toString());
			if (date == null)
				return 0;
			return date.getTime();
		}
	}

}
