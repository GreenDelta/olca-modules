package org.openlca.git.find;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectStream;
import org.openlca.git.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Datasets {

	private static final Logger log = LoggerFactory.getLogger(Datasets.class);
	private final FileRepository repo;

	public static Datasets of(FileRepository repo) {
		return new Datasets(repo);
	}

	private Datasets(FileRepository repo) {
		this.repo = repo;
	}

	public String getName(ObjectId id) {
		var data = parse(id, "name");
		var name = data.get("name");
		return name != null ? name.toString() : null;
	}

	public Map<String, Object> parse(ObjectId id, String... fields) {
		var stream = stream(id);
		if (stream == null)
			return new HashMap<>();
		return MetaDataParser.parse(stream, fields);
	}

	public Map<String, Object> parse(ObjectId id, List<FieldDefinition> defs) {
		var stream = stream(id);
		if (stream == null)
			return new HashMap<>();
		return MetaDataParser.parse(stream, defs);
	}

	public ObjectStream stream(ObjectId id) {
		if (id == null)
			return null;
		try {
			return repo.getObjectDatabase().newReader().open(id).openStream();
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public byte[] getBytes(ObjectId id) {
		if (id == null)
			return null;
		try {
			return repo.getObjectDatabase().newReader().open(id).getBytes();
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public String get(Reference ref) {
		if (ref == null)
			return null;
		return get(ref.objectId);
	}

	public String get(ObjectId id) {
		var data = getBytes(id);
		if (data == null)
			return null;
		try {
			return new String(data, "utf-8");
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public byte[] getBinary(Reference ref, String filepath) {
		if (ref == null || filepath == null || filepath.isEmpty())
			return null;
		var id = Ids.of(repo).get(ref.getBinariesPath() + "/" + filepath, ref.commitId);
		return getBytes(id);
	}

}
